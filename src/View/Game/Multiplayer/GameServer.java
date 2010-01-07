package View.Game.Multiplayer;

import View.Game.Game;
import View.Game.GameConfig;
import World.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.bluetooth.*;
import javax.microedition.io.*;

/**
 * GameServer
 *
 * @author Tim Perry
 */
class GameServer {

  private Game game;
  private LocalDevice device;
  private StreamConnectionNotifier connectionNotifier;
  
  // This is a connection to all our clients. This could be null
  private IndividualGameServer[] individualThreads;
  private int connectedPlayers = 0;

  private final String connectionString;

  // *** Constants for magic numbers ***
  // Number of checksums to buffer at any time.
  private final static byte CHECKSUM_BUFFER_SIZE = 5;

  // In our description we have a delimiter between the game name and the game id.
  public static final String DESCRIPTION_DELIM = "#";

  // The number of ticks so far.
  private long tickCount = 0;
  // A bare minimum object that we use as a lock for notifying threads that the
  // tick has changed.
  private final Object tickLock = new Object();

  // A mapping from times -> checksum values, and an index to indicate the slot to fill
  // in with the next checksum.
  private long[] checksumTimes = new long[CHECKSUM_BUFFER_SIZE];
  private long[] checksumValues = new long[CHECKSUM_BUFFER_SIZE];
  private int nextChecksumIndex = 0;

  // Are we serving, right now?
  private boolean active;

  public GameServer(Game game)
  {
    this.game = game;

    // We're not serving yet.
    active = false;

    // Build a service URL.
    connectionString = "btspp://localhost:"+MultiplayerManager.UUID.toString();

    // We prepare to track streams, up to the number required for this game. This is -1 as we are in the game already
    // and need no slot.
    individualThreads = new IndividualGameServer[game.getGameConfig().getMaxPlayers() - 1];

    // Get the bluetooth device itself prepped.
    try {
      device = LocalDevice.getLocalDevice();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Starts the threads required to serve this game up.
  public void startServer()
  {
    active = true;

    new Timer().scheduleAtFixedRate(new TickTask(), 0, MultiplayerManager.TICK_LENGTH);
    Thread gameProviderThread = new Thread(new GameProvider());

    gameProviderThread.start();
  }

  // Gets the index in indivThreads[] of the first free slot. Returns -1 if threads is full
  private byte getNextFreeSlot()
  {
    for (byte ii = 0; ii < individualThreads.length; ii++)
    {
      if (individualThreads[ii] == null) return ii;
    }
    return -1;
  }

  // We've opened a connection with somebody, and we want to set them up as a new player with the
  // given id. Build them a thread, and get them into the game.
  public void addPlayer(byte threadIndex, StreamConnection connection)
  {
    // Pulled out here to make it nice and explicit. We allocate player ids to match slots in our
    // threads array, atm, although this could change. It needs to be +1 because player 0 should(/could)
    // be the server, and thus not in the serving threads array.
    byte playerId = (byte) (threadIndex + 1);
    individualThreads[threadIndex] = new IndividualGameServer(connection, playerId);
    connectedPlayers++;
    new Thread(individualThreads[threadIndex]).start();
  }

  // For whatever reason (dropped connection, etc), we've lost someone. Open the space up for
  // anyone else who wants it.
  public void removePlayer(int playerId)
  {
    individualThreads[playerId] = null;
    connectedPlayers--;
    individualThreads.notify();
  }
  
  // Allows clients to connect, and sets them up with individual game server threads.
  private class GameProvider implements Runnable
  {

    public void run()
    {
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

      try
      {
        connectionNotifier = (StreamConnectionNotifier) Connector.open(connectionString);

        // Fill out our service record, so people can see who we are and what game this is.
        DataElement name = new DataElement(DataElement.STRING, "ZoM");
        DataElement description = new DataElement(DataElement.STRING, game.getGameConfig().getGameName() + DESCRIPTION_DELIM + game.getGameConfig().getGameId());

        ServiceRecord ourServiceRecord = device.getRecord(connectionNotifier);
        ourServiceRecord.setAttributeValue(0x100, name);
        ourServiceRecord.setAttributeValue(0x101, description);

        // Show us to the world.
        device.setDiscoverable(DiscoveryAgent.GIAC);

        // While we're serving and we have spaces, keep offering connections. 
        while (active && connectedPlayers < game.getGameConfig().getMaxPlayers() - 1)
        {
          // If we have a free slot, wait for another connection, and start it in a new thread.
          byte nextSlot = getNextFreeSlot();

          if (nextSlot != -1)
          {
            // This blocks until a new player appears, ready to rock.
            addPlayer(nextSlot, connectionNotifier.acceptAndOpen());            
          }

          // If the slots are now full, wait for somebody to tell us one of them is empty.
          while (connectedPlayers >= game.getGameConfig().getMaxPlayers() - 1)
          {
            System.out.println("Server is full");
            // TODO - NEED TO FIX THE MONITOR STUFF HERE
            individualThreads.wait();
          }
        }
      }
      catch (Exception ex)
      {
        System.out.println("Uh oh, main provider thread crashed. SHOULD NOT HAPPEN.");
        ex.printStackTrace();
      }
    }
    
  }

  // One single Task that manages general stuff for the serving of the game - atm just
  // generating checksums and managing the game ticks. This is run every TICK_LENGTH ms.
  private class TickTask extends TimerTask
  {

    public TickTask()
    {
      tickCount = 0;
    }

    public void run()
    {
      if (active)
      {
        // Calculate checksum for this tick, and save it.
        checksumValues[nextChecksumIndex] = game.checksum();
        checksumTimes[nextChecksumIndex] = tickCount;
        nextChecksumIndex = (nextChecksumIndex + 1) % CHECKSUM_BUFFER_SIZE;

        tickCount++;

        synchronized(tickLock)
        {
          tickLock.notifyAll();
        }
      }
      else
      {
        cancel();
      }
    }

  }

  // Every time somebody connects, we create another of these to manage that connection.
  private class IndividualGameServer implements Runnable
  {

    private Connection conn;

    private final byte playerId;

    // Build some streams on that connection.
    public IndividualGameServer(StreamConnection connection, byte playerId)
    {
      this.playerId = playerId;
      try
      {
        conn = new Connection(new ZomDataInputStream(connection.openInputStream()),
                              new ZomDataOutputStream(connection.openOutputStream()));
      }
      catch (IOException e) { System.out.println("Couldn't get server streams"); }
    }

    // Go, go, go! This should match the data expected by MultiplayerManager->JoinGame, followed by
    // GameClient->Run.
    public void run()
    {
      try
      {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY-1);
        
        sendGameConfig();
        conn.writeByte(playerId);

        // This blocks until the client begins actively running, starting with a full sync.
        sync(true);
        conn.flush();

        long lastTickCount;

        while(conn.isConnected())
        {
          lastTickCount = tickCount;
          recieveChanges();
          sync(false);

          conn.flush();

          // Wait until the tick is updated.
          synchronized(tickLock)
          {
            while (lastTickCount == tickCount)
            {
              tickLock.wait(MultiplayerManager.TICK_LENGTH);
            }
          }
        }
      }
      // TODO If anything goes wrong, ever, we just give up. Could be worth reconsidering this one day.
      catch (Exception e) {
        System.out.println("Exception while serving, drop player!");
        dropConnection();
      }
    }

    private void dropConnection()
    {
      System.out.println("Connection lost");

      // Try and properly close down.
      conn.close();

      // Take this thread, and this player, out of the game.
      removePlayer(playerId);
    }

    protected void sendGameConfig() throws IOException
    {
      GameConfig config = game.getGameConfig();
      conn.write(config);
    }

    // Syncs every runtime value - that is, the positions of everything that might've moved etc,
    // not things like the map background.
    // updateRemotePlayer specifies whether we should give the client their own player's
    // details - this would override where they thought they were if, for example, they were wrong.
    protected void sync(boolean updateRemotePlayer) throws IOException
    {
      Thing t;
      Vector things = game.getWorld().getThings();

      game.getWorld().lockForRead();

      // If we don't want to forcibly update the remote player, and we do know about them
      // already then we're updating one less thing than we know about.
      int thingSize = game.getWorld().getThingCount();
      if (!updateRemotePlayer) thingSize--;

      System.out.println("writing thing count "+thingSize);
      conn.writeInt(thingSize);

      for (int ii = 0; ii < game.getWorld().getThingCount(); ii++)
      {
        // If we don't want to forcibly update the remote player, don't give them details about them.
        if (!updateRemotePlayer && ii == playerId) continue;
        t = (Thing) things.elementAt(ii);
        conn.writeInt(t.getThingId());
        conn.write(t);
      }

      game.getWorld().unlock();

      // Make sure they know the current tick status
      conn.writeLong(tickCount);
    }

    protected void recieveChanges() throws IOException
    {      
      Thing remotePlayer = game.getWorld().getThing(playerId);

      if (remotePlayer != null)
      {
        conn.readAndUpdate(remotePlayer);
      }
      else
      {
        try
        {
          remotePlayer = (Player) conn.readAndBuild();
          remotePlayer.setThingId(playerId);

          game.getWorld().lockForWrite();
          game.getWorld().addThing(remotePlayer);
          game.getWorld().forceAdd();
          game.getWorld().unlock();
        }
        catch (InstantiationException ex)
        {
          ex.printStackTrace();
        }
      }
    }

  }


}

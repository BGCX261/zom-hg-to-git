package com.zom.view.game.multiplayer.server;

import com.zom.view.game.Game;
import com.zom.view.game.GameConfig;
import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.MultiplayerManager;
import com.zom.world.Player;
import com.zom.world.Thing;
import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.io.StreamConnection;

class IndividualGameServer implements Runnable
{
  private Connection conn;
  private final byte playerId;
  private int latency = 0;

  private boolean active = false;

  // Singleton to represent the local game server thread.
  protected static final IndividualGameServer localThread = new IndividualGameServer();

  GameServer mainServer;

  // This is only for creating a stub individual game server that we use to represent the local player's thread, to make other
  // representations make more sense. The created server does _nothing_.
  private IndividualGameServer()
  {
    playerId = 0;
  }

  public IndividualGameServer(StreamConnection streamConn, byte playerId, GameServer mainServer)
  {
    this.mainServer = mainServer;
    this.playerId = playerId;
    
    try
    {
      conn = new Connection(streamConn);
    }
    catch (IOException e)
    {
      System.out.println("Couldn\'t get server streams");
    }
  }

  public void start()
  {
    active = true;
    new Thread(this).start();
  }

  public void stop()
  {
    active = false;
  }

  // Go, go, go! This should match the data expected by MultiplayerManager->JoinGame, followed by GameClient->Run.
  public void run()
  {
    if (playerId == 0) return;

    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

    try
    {
      sendGameConfig();
      conn.writeByte(playerId);

      // This blocks until the client begins actively running (in GameClient->Run), and then works out the latency for us, for later.
      latency = conn.ping(3);
      sync(true);
      conn.flush();

      long lastTickCount;

      while (conn.isConnected() && active)
      {
        lastTickCount = mainServer.getTickCount();

        recieveChanges();
        sync(false);
        conn.flush();

        // Wait until the tick is updated.
        synchronized(mainServer.tickLock)
        {
          while (lastTickCount == mainServer.getTickCount())
          {
            mainServer.tickLock.wait(MultiplayerManager.TICK_LENGTH);
          }
        }
      }
    } // TODO If anything goes wrong, ever, we just give up. Could be worth reconsidering this one day.
    catch (Exception e)
    {
      System.out.println("Exception while serving: ");
      e.printStackTrace();

      // Take this thread, and this player, out of the game.
      mainServer.removePlayer(playerId);
    }

    dropConnection();
  }

  protected void dropConnection()
  {
    System.out.println("Connection dropped");

    // Try and properly close down.
    conn.close();

    conn = null;
  }

  protected void sendGameConfig() throws IOException
  {
    GameConfig config = mainServer.game.getGameConfig();
    conn.write(config);
  }

  // Syncs every runtime value - that is, the positions of everything that might've moved etc,
  // not things like the map background.
  // updateRemotePlayer specifies whether we should give the client their own player's
  // details - this would override where they thought they were if, for example, they were wrong.
  protected void sync(boolean updateRemotePlayer) throws IOException
  {
    Thing t;

    mainServer.game.getWorld().lockForRead();

    // If we don't want to forcibly update the remote player, and we do know about them
    // already then we're updating one less thing than we know about.
    int thingSize = mainServer.game.getWorld().getThingCount();
    if (!updateRemotePlayer) thingSize--;

    conn.writeInt(thingSize);
    for (Enumeration things = mainServer.game.getWorld().getThings(); things.hasMoreElements();)
    {
      t = (Thing) things.nextElement();
      // If we don't want to forcibly update the remote player, don't give them details about them.
      if (!updateRemotePlayer && t.getThingId() == playerId) continue;
      conn.writeInt(t.getThingId());
      conn.write(t);
    }

    mainServer.game.getWorld().unlock();

    // Make sure they know the current tick status
    conn.writeLong(mainServer.getTickCount());
  }

  protected void recieveChanges() throws IOException
  {
    Thing remotePlayer = mainServer.game.getWorld().getThing(playerId);
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
        mainServer.game.getWorld().lockForWrite();
        mainServer.game.getWorld().addThing(remotePlayer);
        mainServer.game.getWorld().forceAdd();
        mainServer.game.getWorld().unlock();
      }
      catch (InstantiationException ex)
      {
        ex.printStackTrace();
      }
    }
    
    // How out of date the data we're being given is.
    long lastPhysicsTime = -(latency + conn.readInt()) + System.currentTimeMillis();

    int newThings = conn.readInt();
    if (newThings > 0)
    {
      mainServer.game.getWorld().lockForWrite();
      for (int ii = 0; ii < newThings; ii++)
      {
        try
        {
          Thing t = (Thing) conn.readAndBuild();

          mainServer.game.getWorld().addThing(t);
          mainServer.game.getWorld().forceAdd();
          
          conn.writeInt(t.getThingId());

          // Catch the position of this thing up with where it should be. (REPORT)
          t.calculateMoves(mainServer.game.getWorld(), (System.currentTimeMillis() - lastPhysicsTime) / Game.TICK_LENGTH);
          t.makeMoves(mainServer.game.getWorld());
        }
        // If we fail to build the thing the client wants to build, tell them we're not adding it.
        catch (InstantiationException e)
        {
          conn.writeInt(-1);
        }
      }
      mainServer.game.getWorld().unlock();
    }
    int deadThings = conn.readInt();
    if (deadThings > 0)
    {
      mainServer.game.getWorld().lockForWrite();
      for (int ii = 0; ii < deadThings; ii++)
      {
        mainServer.game.getWorld().removeThing(conn.readInt());
      }
      mainServer.game.getWorld().unlock();
    }
  }
}

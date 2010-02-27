package com.zom.view.game.multiplayer.server;

import com.zom.util.Queue;
import com.zom.view.game.Game;
import com.zom.view.game.GameConfig;
import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.MultiplayerManager;
import com.zom.world.Player;
import com.zom.world.Thing;
import com.zom.world.ThingLifeListener;
import com.zom.world.World;
import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.io.StreamConnection;

class IndividualGameServer implements Runnable, ThingLifeListener
{
  private Connection conn;
  private final byte playerId;
  private int latency = 0;

  private boolean active = false;

  // Singleton to represent the local game server thread.
  protected static final IndividualGameServer localThread = new IndividualGameServer();

  private GameServer mainServer;
  private World world;
  
  private final Queue deadThings = new Queue();

  // This is only for creating a stub individual game server that we use to represent the local player's thread, to make other
  // representations make more sense. The created server does _nothing_.
  private IndividualGameServer()
  {
    playerId = 0;
  }

  public IndividualGameServer(StreamConnection streamConn, byte playerId, GameServer mainServer)
  {
    this.mainServer = mainServer;
    world = mainServer.game.getWorld();
    world.addThingLifeListener(this);
    this.playerId = playerId;
    
    try
    {
      conn = new Connection(streamConn);
    }
    catch (IOException e)
    {
      throw new Error("Couldn\'t get server streams");
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

    // If their worldbuilder is not ready, with the given information provided, send more.
    if (conn.readBool() == false)
    {
      // Use the WorldBuilder class to get the full and complete information
      conn.write(config.getWorldBuilder().getFullSyncId(), config.getWorldBuilder().getFullData());
    }
    
    conn.writeByte(playerId);
  }

  // Syncs every runtime value - that is, the positions of everything that might've moved etc,
  // not things like the map background.
  // updateRemotePlayer specifies whether we should give the client their own player's
  // details - this would override where they thought they were if, for example, they were wrong.
  protected void sync(boolean updateRemotePlayer) throws IOException
  {
    Thing t;

    world.lockForRead();

    // If we don't want to forcibly update the remote player, and we do know about them
    // already then we're updating one less thing than we know about.
    int thingSize = world.getThingCount();
    if (!updateRemotePlayer) thingSize--;

    conn.writeByte((byte) thingSize);
    for (Enumeration things = world.getThings(); things.hasMoreElements();)
    {
      t = (Thing) things.nextElement();

      // If we don't want to forcibly update the remote player, don't give them details about them.
      if (!updateRemotePlayer && t.getThingId() == playerId) continue;
      conn.writeByte((byte) t.getThingId());
      conn.write(t);
    }

    world.unlock();
    
    synchronized (deadThings)
    {
      conn.writeInt(deadThings.size());

      while (!deadThings.empty())
      {
        t = (Thing) deadThings.dequeue();
        conn.writeInt(t.getThingId());
      }
    }

    // Make sure they know the current tick status
    conn.writeLong(mainServer.getTickCount());
  }

  protected void recieveChanges() throws IOException
  {
    Thing remotePlayer = world.getThing(playerId);

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
        world.lockForWrite();
        world.addThing(remotePlayer);
        world.forceUpdate();
        world.unlock();
      }
      catch (InstantiationException ex)
      {
        ex.printStackTrace();
      }
    }

    // This gives us the time that the object we have were last updated.
    long lastPhysicsTime = -(latency + conn.readInt()) + System.currentTimeMillis();

    int birthCount = conn.readInt();
    if (birthCount > 0)
    {
      world.lockForWrite();
      for (int ii = 0; ii < birthCount; ii++)
      {
        try
        {
          Thing t = (Thing) conn.readAndBuild();

          world.addThing(t);
          world.forceUpdate();
          
          conn.writeInt(t.getThingId());

          // Catch the position of this thing up with where it should be. (REPORT)
          t.calculateMoves(world, (System.currentTimeMillis() - lastPhysicsTime) / Game.TICK_LENGTH);
          t.makeMoves(world);
        }
        // If we fail to build the thing the client wants to build, tell them we're not adding it.
        catch (InstantiationException e)
        {
          conn.writeInt(-1);
        }
      }
      world.unlock();
    }
    
    int deathCount = conn.readInt();
    if (deathCount > 0)
    {
      for (int ii = 0; ii < deathCount; ii++)
      {
        world.removeThing(conn.readInt());
      }

      // We lock the world so we can write the deaths to it, and we while we do that we avoid listening to deaths; it'd be pointless
      // because since we have the write lock nobody else can cause any deaths, so we'd just be hearing about things we already know about,
      // and don't want to.
      world.lockForWrite();
      world.removeThingLifeListener(this);

      world.forceUpdate();

      world.addThingLifeListener(this);
      world.unlock();
    }
  }

  // We absolutely totally do not care about births, sync handles them fine without being informed explicitly.
  public void thingBorn(Thing t) { }

  // We register deaths, so we can inform our client.
  public void thingDied(Thing t)
  {
    synchronized (deadThings)
    {
      deadThings.enqueue(t);
    }
  }
}

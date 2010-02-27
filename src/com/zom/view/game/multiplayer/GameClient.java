package com.zom.view.game.multiplayer;

import com.zom.view.game.*;
import com.zom.world.LocalPlayer;
import com.zom.world.Thing;
import com.zom.world.ThingLifeListener;
import com.zom.util.Queue;
import java.io.IOException;

/**
 * GameClient
 *
 * @author Tim Perry
 */
class GameClient implements Runnable, ThingLifeListener {

  private Game game;
  private Connection conn;

  private final Queue newThings = new Queue();
  private final Queue deadThings = new Queue();

  private boolean running = false;

  // Every sync of a game is a tick, so that tickCount * tickLength = the length of the game so far in milliseconds.
  private long tickCount;

  public GameClient(Game g, Connection conn)
  {
    this.game = g;
    this.conn = conn;
  }

  public void run()
  {
    game.getWorld().addThingLifeListener(this);

    running = true;

    try
    {
      conn.listenForPing(3);

      // Before we start, get into sync.
      sync();

      conn.lock();
      while (running)
      {
        sendChanges();
        sync();
        
        conn.flush();

        tickCount++;
      }
      conn.unlock();
    }
    catch (Exception e) { e.printStackTrace(); }
  }

  public void sync() throws IOException
  {
    try
    {
      // REPORT - say why the lockForWrite can't go here!
      byte thingCount = conn.readByte();

      byte thingId;
      Thing localThing;

      // TODO - Try moving the lock inside the loop, and see if that in anyway improves things.
      game.getWorld().lockForWrite();
      for (int ii = 0; ii < thingCount; ii++)
      {
        thingId = conn.readByte();
        localThing = game.getWorld().getThing(thingId);

        // If we already have this thing then we update it. If we don't then we add it.
        if (localThing != null)
        {
          conn.readAndUpdate(localThing);
        }
        else
        {
          localThing = (Thing) conn.readAndBuild();
          if (localThing == null) continue;
          
          localThing.setThingId(thingId);
          game.getWorld().addThing(localThing);
        }
      }

      int deathCount = conn.readInt();

      for (int ii = 0; ii < deathCount; ii++)
      {
        game.getWorld().removeThing(conn.readInt());
      }

      // We avoid listening to deaths; it'd be pointless because since we have the write lock nobody else can cause any
      // deaths, so we'd just be hearing about the deaths we're causing, which we don't want to do.
      game.getWorld().removeThingLifeListener(this);
      game.getWorld().forceUpdate();
      game.getWorld().addThingLifeListener(this);
      
      game.getWorld().unlock();

      // Get our ticks in sync.
      tickCount = conn.readLong();
    }
    catch (InstantiationException e)
    { e.printStackTrace(); }
  }

  public void sendChanges() throws IOException
  {
    game.getWorld().lockForWrite();

    conn.write(LocalPlayer.getLocalPlayer());

    synchronized (newThings)
    {
      // Tell the server how out of date this information is.
      conn.writeInt((int) (System.currentTimeMillis() - game.getWorld().getLastTickTime()));

      // Tell the server how many things we've added, give it each of them, and ask it what thingId we should be using.
      conn.writeInt(newThings.size());
      
      while (!newThings.empty())
      {
        Thing t = (Thing) newThings.dequeue();
        conn.write(t);

        // The server might not like the id. They'll either tell us what id they're going to use, so we can use it too,
        // or they'll tell us no (-1) and we'll delete our thing.
        int newThingId = conn.readInt();

        // The server can tell us no by returning -1.
        if (newThingId == -1) game.getWorld().removeThing(t.getThingId());

        // Alternatively, the server can change the id.
        else game.getWorld().changeThingId(t, newThingId);
      }
    }

    synchronized (deadThings)
    {
      // Tell the server about everything we have that has died.
      conn.writeInt(deadThings.size());

      while (!deadThings.empty())
      {
        Thing t = (Thing) deadThings.dequeue();

        // TODO - What happens if we added something to the world and then removed it, and then the server said no?
        conn.writeInt(t.getThingId());
      }
    }
    game.getWorld().unlock();
  }

  public void thingBorn(Thing t)
  {
    synchronized(newThings)
    {
      newThings.enqueue(t);
    }
  }

  public void thingDied(Thing t)
  {
    synchronized(deadThings)
    {
      deadThings.enqueue(t);
    }
  }

}

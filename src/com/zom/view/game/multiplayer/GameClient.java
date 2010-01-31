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

  // *** Constants for magic numbers ***
  // Number of checksums to buffer at any time.
  private final static byte CHECKSUM_BUFFER_SIZE = 5;

  // A mapping from times -> checksum values, and an index to indicate the slot to fill
  // in with the next checksum.
  private long[] checksumTimes = new long[CHECKSUM_BUFFER_SIZE];
  private long[] checksumValues = new long[CHECKSUM_BUFFER_SIZE];
  private int nextChecksumIndex = 0;

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
    game.getWorld().setThingLifeListener(this);

    running = true;

    try
    {
      conn.listenForPing(3);

      // Before we start, get into sync.
      sync();

      conn.lock();
      while (running)
      {
        // Work out our current checksum at the start of this tick.
        //updateChecksum();
        sendChanges();
        sync();
        
        conn.flush();

        tickCount++;
      }
      conn.unlock();
    }
    catch (Exception e) { e.printStackTrace(); }
  }

  // Calculate checksum for this tick, and save it.
  public void updateChecksum()
  {
    checksumValues[nextChecksumIndex] = game.checksum();
    checksumTimes[nextChecksumIndex] = tickCount;
    nextChecksumIndex = (nextChecksumIndex + 1) % CHECKSUM_BUFFER_SIZE;
  }

  // Reads two longs from the server - one tickstamp and one checksum.
  // If they're consistent with our data, we return true, otherwise false.
  public boolean checkSync() throws IOException
  {
    /* TODO - Implement checksums so we can uncomment this.
    long serverChecksumTime = conn.readLong();
    long serverChecksum = conn.readLong();
    
    // See if we have a checksum for this tick. If we do, see if it's the same.
    // If it is, we're good. If it's not, or we don't have a checksum, we're not good.
    for (int ii = 0; ii < checksumTimes.length; ii++){
      if (checksumTimes[ii] == serverChecksumTime)
      {
        if (checksumValues[ii] == serverChecksum) return true;
        else return false;
      }
    }
     */
    return false;
  }

  public void sync() throws IOException
  {
    try
    {
      // REPORT - say why the lockForWrite can't go here!
      int thingCount = conn.readInt();

      int thingId;
      Thing localThing;

      game.getWorld().lockForWrite();
      for (int ii = 0; ii < thingCount; ii++)
      {
        thingId = conn.readInt();
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

        if (newThingId == -1) game.getWorld().removeThing(t.getThingId());
        else game.getWorld().changeThingId(t.getThingId(), newThingId);
      }
    }

    synchronized (deadThings)
    {
      // Tell the server about everything we have that has died.
      conn.writeInt(deadThings.size());
      while (!deadThings.empty())
      {
        // TODO - What happens if we added something to the world and then removed it, and then the server said no?
        conn.writeInt(((Thing) deadThings.dequeue()).getThingId());
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

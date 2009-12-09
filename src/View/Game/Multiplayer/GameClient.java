package View.Game.Multiplayer;

import View.Game.*;
import World.LocalPlayer;
import World.Player;
import World.Thing;
import java.io.IOException;

/**
 * GameClient
 *
 * @author Tim Perry
 */
class GameClient implements Runnable {

  // Constants to define message types.
  public static final byte EOF = 0;
  public static final byte ERROR = 1;

  // *** Constants for magic numbers ***
  // Number of checksums to buffer at any time.
  private final static byte CHECKSUM_BUFFER_SIZE = 5;

  private Game game;
  private Connection conn;

  private boolean running = false;

  // A mapping from times -> checksum values, and an index to indicate the slot to fill
  // in with the next checksum.
  private long[] checksumTimes = new long[CHECKSUM_BUFFER_SIZE];
  private long[] checksumValues = new long[CHECKSUM_BUFFER_SIZE];
  private int nextChecksumIndex = 0;

  // Every sync of a game is a tick, so that tickCount * tickLength = the length of the game so far
  // in milliseconds.
  private long tickCount;

  public GameClient(Game g, Connection conn)
  {
    this.game = g;

    this.conn = conn;
  }

  public void run()
  {    
    running = true;

    try
    {
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
    try {
      int thingCount = conn.readInt();

      int thingId;
      Thing localThing;

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
          localThing.setThingId(thingId);
          game.getWorld().addThing(localThing);
        }
      }

      // Get our ticks in sync.
      tickCount = conn.readLong();
    }
    catch (InstantiationException e)
    { e.printStackTrace(); }
  }

  public void sendChanges() throws IOException
  {
    conn.write(LocalPlayer.getLocalPlayer());
  }

}

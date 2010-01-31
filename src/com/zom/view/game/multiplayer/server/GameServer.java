package com.zom.view.game.multiplayer.server;

import com.zom.view.game.multiplayer.*;
import com.zom.view.game.Game;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.io.StreamConnection;

/**
 * GameServer
 *
 * @author Tim Perry
 */
public class GameServer {

  public final Game game;
  
  // This is a connection to all our clients. The index maps to the player id.
  private final IndividualGameServer[] playerThreads;
  private int playerCount = 1;

  // This is our thread that waits for connections and gets everything set up.
  private final GameProvider gameProvider;

  // *** Constants for magic numbers ***
  // Number of checksums to buffer at any time.
  private final static byte CHECKSUM_BUFFER_SIZE = 5;

  // In our description we have a delimiter between the game name and the game id.
  public static final String DESCRIPTION_DELIM = "#";

  // The number of ticks so far.
  private long tickCount = 0;
  // A bare minimum object that we use as a lock for notifying threads that the tick has changed.
  protected final Object tickLock = new Object();

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

    gameProvider = new GameProvider(this, game.getGameConfig());

    // We prepare to track player threads, up to the number required for this game.
    playerThreads = new IndividualGameServer[game.getGameConfig().getMaxPlayers()];

    // This is purely to make the organisation nicer - we have a thread that is merely a stub here to represent the
    // fact that we are in the game, so that quite a lot of the rest of the code is more intuitive.
    playerThreads[0] = IndividualGameServer.localThread;
  }

  public long getTickCount()
  {
    return tickCount;
  }

  // Gets the index in indivThreads[] of the first free slot. Returns -1 if we are full
  protected byte getNextFreeSlot()
  {
    for (byte ii = 0; ii < playerThreads.length; ii++)
    {
      if (playerThreads[ii] == null) return ii;
    }
    return -1;
  }

  public int getPlayerCount()
  {
    return playerCount;
  }

  // We've opened a connection with somebody, and we want to set them up as a new player with the
  // given id. Build them a thread, and get them into the game.
  public void addPlayer(byte playerId, StreamConnection connection)
  {
    playerThreads[playerId] = new IndividualGameServer(connection, playerId, this);
    playerCount++;
    
    playerThreads[playerId].start();
  }

  // For whatever reason (dropped connection, etc), we've lost someone. Open the space up for
  // anyone else who wants it.
  public void removePlayer(int playerId)
  {
    if (playerId == 0) throw new Error("Trying to remove local server player - SHOULD NEVER HAPPEN");

    playerThreads[playerId] = null;
    playerCount--;

    synchronized (gameProvider)
    {
      gameProvider.notify();
    }
  }

  // Starts the threads required to serve this game up.
  public void startServer()
  {
    active = true;

    new Timer().scheduleAtFixedRate(new TickTask(), 0, MultiplayerManager.TICK_LENGTH);
    gameProvider.start();
  }

  public void stopServer()
  {
    active = false;

    for (int ii = 0; ii < playerThreads.length; ii++)
    {
      if (playerThreads[ii] != null) playerThreads[ii].stop();
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

}

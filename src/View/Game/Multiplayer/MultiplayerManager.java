package View.Game.Multiplayer;

import View.Game.*;
import World.*;
import World.StaticWorldBuilder;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * MultiplayerManager
 *
 * All networking should be managed through this class.
 *
 * @author Tim Perry
 */
public class MultiplayerManager {

  // Length of multiplayer ticks, in milliseconds.
  public final static int TICK_LENGTH = 50;

  private GameClient client = null;
  private GameServer server = null;

  // UUID is ZoM -> Binary representation of ascii -> decimal
  public static final UUID UUID = new UUID(5926733);

  // This registers every object that we will want to sync with the Connection class,
  // so that it knows what datatypes it needs to send.
  static
  {
    GameConfig.registerForSync();
    Player.registerForSync();
    StaticWorldBuilder.registerForSync();
    Bullet.registerForSync();
  }
  
  // Manage the given game as a client to the given server, and also start serving it to anybody else who might want it (Serving not yet included - TODO)
  public MultiplayerManager(Game game, GameConfig config)
  {
    if (config.getConnectionToServer() != null)
    {
      client = new GameClient(game, config.getConnectionToServer());
    }
    else
    {
      server = new GameServer(game);
    }
  }

  // Try and start the relevant multiplayer services for this game.
  public void start()
  {
    System.out.println("Multiplayer started");

    if (client != null)
    {
      System.out.println("Client started");
      new Thread(client).start();
    }
    
    if (server != null)
    {
      System.out.println("Server started");
      server.startServer();
    }
  }

  // Starts up a GameJoiner thread, with these parameters.
  public static void joinGame(String connectionString, GameSearchListener listener)
  {
    GameJoiner joiner = new GameJoiner(connectionString, listener);
    new Thread(joiner).start();
  }

  // A class that runs our basic game joining code in another thread, so that the JoinGameMenu doesn't have to
  // block for it.
  // This Builds the streams to connect us to them, makes GameConfig build itself over those streams, and then builds
  // a game from that config. We then build a GameClient to manage that game, and hand the game back to the caller
  private static class GameJoiner implements Runnable
  {

    private String connectionString;
    private GameSearchListener listener;

    public GameJoiner(String connectionString, GameSearchListener listener)
    {
      this.connectionString = connectionString;
      this.listener = listener;
    }

    public void run()
    {
      Game game;
      Connection conn = null;

      try {
        StreamConnection connection = ((StreamConnection) Connector.open(connectionString));
        conn = new Connection(
          new ZomDataInputStream(connection.openInputStream()),
          new ZomDataOutputStream(connection.openOutputStream())
        );

        // Lock these streams. It's talking time for us now.
        conn.lock();

        // Build a config for that game from the server.
        GameConfig gameConfig = (GameConfig) conn.readAndBuild();
        
        gameConfig.setPlayerId(conn.readByte());        
        gameConfig.setConnectionToServer(conn);

        // Set up the game
        game = new Game(gameConfig);

        // We're done talking.
        conn.unlock();

        listener.joinSucceeded(game);
      }
      catch (Exception e)
      {
        e.printStackTrace();

        // We need to make sure we release the locks if all's gone wrong.
        if (conn != null) conn.unlock();

        listener.joinFailed();
        return;
      }
    }

  }

}

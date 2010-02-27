package com.zom.view.game;

import com.zom.view.game.multiplayer.*;
import com.zom.view.menu.Config;

/**
 * GameConfig
 *
 * Basically just a struct for handing back and forth when choosing game setup options, This will eventually be
 * passed into Game's constructor, and used to build a Game.
 *
 * @author Tim Perry
 */
public class GameConfig implements Syncable {

  private byte maxPlayers = 4;
  private WorldBuilder worldBuilder;
  private String gameName = "Unnamed Game";
  private final Config mainConfig = Config.getInstance();
  private byte difficulty = 1;

  // An id unique to the game, shared across all participants.
  private int id;

  // An id unique to us within this game.
  private byte playerId;

  // Stores an id used to identify us over connection, if/when we sync across the network
  private static int syncId = -1;

  private Connection conn;

  private static GameConfigFactory factory = new GameConfigFactory();

  // Generate a unique id for this game
  public GameConfig()
  {
    // Mod to make sure it's within the integer bounds - +1 is so that we know 0 is not a valid id, which is nice for checking things later.
    id = (int) (System.currentTimeMillis() % 30000) + 1;
    playerId = 0;
  }

  // Take an id - useful for when the game was built elsewhere
  public GameConfig(int id, byte playerId)
  {
    this.id = id;
    this.playerId = playerId;
  }

  public void setGameId(int id)
  {
    this.id = id;
  }

  public int getGameId()
  {
    return id;
  }

  public String getGameName()
  {
    return gameName;
  }

  public byte getMaxPlayers()
  {
    return maxPlayers;
  }

  public void setGameName(String gameName)
  {
    this.gameName = gameName;
  }

  public void setMaxPlayers(int maxPlayers)
  {
    this.maxPlayers = (byte) maxPlayers;
  }

  public void setDifficulty(byte difficulty)
  {
    if (difficulty < 0 || difficulty > 2) throw new Error("Attempted to set invalid difficulty (" + difficulty + ")");
    this.difficulty = difficulty;
    if (getWorldBuilder() != null) getWorldBuilder().setDifficulty(difficulty);
  }

  public byte getDifficulty()
  {
    return difficulty;
  }

  public void setWorldBuilder(WorldBuilder worldBuilder)
  {
    this.worldBuilder = worldBuilder;
    worldBuilder.setDifficulty(difficulty);
  }

  public WorldBuilder getWorldBuilder()
  {
    return worldBuilder;
  }

  public int getControlScheme()
  {
    return mainConfig.getControlScheme();
  }

  public void setPlayerId(byte playerId)
  {
    this.playerId = playerId;
  }

  public byte getPlayerId()
  {
    return playerId;
  }

  public Connection getConnectionToServer()
  {
    return conn;
  }

  public void setConnectionToServer(Connection conn)
  {
    this.conn = conn;
  }

  // GameConfigs are equal if they have the same unique id.
  public boolean equals(Object obj)
  {
    if (obj.getClass() != this.getClass()) return false;
    // If this is another gameconfig, then these are equal if the id is the same, and the name is the same.
    else return (((GameConfig)obj).getGameId() == getGameId() && ((GameConfig)obj).getGameName().equals(getGameName()));
  }

  // Similiarly, the hashcode for a gameconfig is its id
  public int hashCode()
  {
    return getGameId();
  }

  public int getSyncId()
  {
    return syncId;
  }
  
  public static void registerForSync()
  {
    factory.register();
  }

  public Object[] getData()
  {
    return new Object[]
    {
      new Integer(getGameId()),
      getGameName(),
      getWorldBuilder(),
      new Byte(getMaxPlayers()),
      new Byte(getDifficulty())
    };
  }

  public void updateWithData(Object[] data)
  {
    setGameId(((Integer)data[0]).intValue());
    setGameName((String)data[1]);
    setWorldBuilder((WorldBuilder)data[2]);
    setMaxPlayers(((Byte)data[3]).byteValue());
    setDifficulty(((Byte)data[4]).byteValue());
  }

  private static class GameConfigFactory implements SyncableFactory
  {
    public void register()
    {
      syncId = Connection.register(new byte[]
      {
        Connection.INT_TYPE, // Game UUID
        Connection.STRING_TYPE, // Game name
        Connection.SYNCABLE_TYPE, // WorldBuilder
        Connection.BYTE_TYPE, // Max Players
        Connection.BYTE_TYPE  // Difficulty
      },
      this);
    }

    public Syncable buildFromData(Object[] data)
    {
      GameConfig gc = new GameConfig();
      gc.updateWithData(data);
      return gc;
    }

  }

}

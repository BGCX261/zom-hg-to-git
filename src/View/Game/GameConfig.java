package View.Game;

import World.*;

/**
 * GameConfig
 *
 * Basically just a struct for handing back and forth when choosing game setup options, This will eventually be
 * passed into Game's constructor, and used to build a Game.
 *
 * @author Tim Perry
 */
public class GameConfig {

  private int maxPlayers = 1;
  private WorldBuilder worldBuilder;
  private String gameName = "";

  public String getGameName()
  {
    return gameName;
  }

  public int getMaxPlayers()
  {
    return maxPlayers;
  }

  public WorldBuilder getWorldBuilder()
  {
    return worldBuilder;
  }

  public void setGameName(String gameName)
  {
    this.gameName = gameName;
  }

  public void setMaxPlayers(int maxPlayers)
  {
    this.maxPlayers = maxPlayers;
  }

  public void setWorldBuilder(WorldBuilder worldBuilder)
  {
    this.worldBuilder = worldBuilder;
  }



}

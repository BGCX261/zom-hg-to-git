package View.Menu;

import World.Player;

/**
 * Config
 *
 * @author Tim Perry
 */
public class Config {

  private int controlScheme = Player.ABSOLUTE_CONTROLS;
  
  private static Config instance;

  private Config() { }

  public static Config getInstance()
  {
    if (instance == null) instance = new Config();
    return instance;
  }

  public void setControlScheme(int controlScheme)
  {
    this.controlScheme = controlScheme;
  }

  public int getControlScheme()
  {
    return controlScheme;
  }

}

package World;

import javax.microedition.lcdui.game.GameCanvas;

/**
 * Player
 *
 * @author Tim Perry
 */
public class Player extends Thing {

  // We have a singleton local player, that represents this phone's player.
  private static Player localPlayer;
  private int playerId;
  private int lastKeyState;
  

  private static final int FORWARD_KEY = GameCanvas.UP_PRESSED;
  private static final int BACKWARD_KEY = GameCanvas.DOWN_PRESSED;
  private static final int LEFT_KEY = GameCanvas.LEFT_PRESSED;
  private static final int RIGHT_KEY = GameCanvas.RIGHT_PRESSED;
  private static final int FIRE_KEY = GameCanvas.FIRE_PRESSED;

  private static final int PLAYER_SPEED = 3;
  private static final int TURNING_DELAY = 10;

  public Player(int playerId)
  {
    super(5,5);
    
    this.playerId = playerId;

    setAngle(3);
  }
  
  public void setKeys(int keyState)
  {
    lastKeyState = keyState;
  }

  public void calculateMoves(World w)
  {
    if ((lastKeyState & FORWARD_KEY) != 0)
    {
      planMove(PLAYER_SPEED);
    }
    if ((lastKeyState & BACKWARD_KEY) != 0)
    {
      planMove( - PLAYER_SPEED);
    }
    if ((lastKeyState & LEFT_KEY) != 0)
    {
      setAngle(getAngle()+0.1);
    }
    if ((lastKeyState & RIGHT_KEY) != 0)
    {
      setAngle(getAngle()-0.1);
    }
  }

  // Manage singleton-ness of the local player.
  public static Player getLocalPlayer()
  {
    if (localPlayer == null)
    {
      localPlayer = new Player(0);
    }
    return localPlayer;
  }

}

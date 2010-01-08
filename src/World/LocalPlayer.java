package World;

import javax.microedition.lcdui.game.GameCanvas;

/**
 * LocalPlayer
 *
 * Note - A singleton LocalPlayer is created at the first call to either createLocalPlayer
 * or getLocalPlayer. This means that they are never created across the network, and as such
 * they use RemotePlayer's syncId. This means if you ever attempt to send a LocalPlayer,
 * they are sent as a RemotePlayer, and rebuilt as such on the other side. This sounds
 * a little idiosyncratic, but is in fact wonderful.
 *
 * @author Tim Perry
 */
public class LocalPlayer extends Player {

  private int lastKeyState;
  private long lastTurnTime;

  public static final byte RELATIVE_CONTROLS = 0;
  public static final byte ABSOLUTE_CONTROLS = 1;

  private static final byte UP = GameCanvas.UP_PRESSED;
  private static final byte DOWN = GameCanvas.DOWN_PRESSED;
  private static final byte LEFT = GameCanvas.LEFT_PRESSED;
  private static final byte RIGHT = GameCanvas.RIGHT_PRESSED;
  
  private static final byte UP_RIGHT = UP | RIGHT;
  private static final byte UP_LEFT = UP | LEFT;
  private static final byte DOWN_RIGHT = DOWN | RIGHT;
  private static final byte DOWN_LEFT = DOWN | LEFT;

  private static final byte DIRECTION_MASK = UP | DOWN | LEFT | RIGHT;

  private static final int FIRE_KEY = GameCanvas.FIRE_PRESSED;

  private static byte controlType = ABSOLUTE_CONTROLS;

  private static LocalPlayer singleton;

  private LocalPlayer(byte playerId) {
    super(playerId);
  }

  public static LocalPlayer getLocalPlayer()
  {
    if (singleton == null) createLocalPlayer(0);
    return singleton;
  }

  public static void createLocalPlayer(int playerId)
  {
    if (singleton == null) singleton = new LocalPlayer((byte)playerId);
  }

  public void setKeys(int keyState)
  {
    lastKeyState = keyState;
  }

 public void setControlScheme(int controlType)
  {
    LocalPlayer.controlType = (byte) controlType;
  }

  public void calculateMoves(World w)
  {
    if (lastKeyState == 0) return;

    if (controlType == ABSOLUTE_CONTROLS)
    {
      if ((lastKeyState & DIRECTION_MASK) != 0)
      {
        switch((lastKeyState & DIRECTION_MASK))
        {
          case UP:
            planSetAngle(0);
            break;
          case UP_RIGHT:
            planSetAngle(2);
            break;
          case RIGHT:
            planSetAngle(4);
            break;
          case DOWN_RIGHT:
            planSetAngle(6);
            break;
          case DOWN:
            planSetAngle(8);
            break;
          case DOWN_LEFT:
            planSetAngle(10);
            break;
          case LEFT:
            planSetAngle(12);
            break;
          case UP_LEFT:
            planSetAngle(14);
            break;
        }

        // If any direction key was pressed, move in the direction we're now facing.
        planMove(SPEED);
      }
    }
    else if (controlType == RELATIVE_CONTROLS)
    {
      if ((lastKeyState & UP) != 0)
      {
        planMove(SPEED);
      }
      if ((lastKeyState & DOWN) != 0)
      {
        planMove( - SPEED);
      }

      // We enforce delay on turning moves because otherwise it's way too fast to be easily controllable.
      if ((lastKeyState & LEFT) != 0 && System.currentTimeMillis() - lastTurnTime > TURNING_DELAY)
      {
        lastTurnTime = System.currentTimeMillis();
        planTurn(-1);
      }
      if ((lastKeyState & RIGHT) != 0 && System.currentTimeMillis() - lastTurnTime > TURNING_DELAY)
      {
        lastTurnTime = System.currentTimeMillis();
        planTurn(1);
      }
    }

    if ((lastKeyState & FIRE_KEY) != 0)
    {
      planFire();
    }
  }

  public int getSyncId()
  {
    return syncId;
  }

}

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

  private static final byte FORWARD_KEY = GameCanvas.UP_PRESSED;
  private static final byte BACKWARD_KEY = GameCanvas.DOWN_PRESSED;
  private static final byte LEFT_KEY = GameCanvas.LEFT_PRESSED;
  private static final byte RIGHT_KEY = GameCanvas.RIGHT_PRESSED;
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
    { // TODO - don't move if turning?
      if ((lastKeyState & FORWARD_KEY) != 0)
      {
        planSetAngle(0);
        planMove(SPEED);
      }
      if ((lastKeyState & BACKWARD_KEY) != 0)
      {
        planSetAngle(8);
        planMove(SPEED);
      }
      if ((lastKeyState & LEFT_KEY) != 0)
      {
        planSetAngle(12);
        planMove(SPEED);
      }
      if ((lastKeyState & RIGHT_KEY) != 0)
      {
        planSetAngle(4);
        planMove(SPEED);
      }
      if ((lastKeyState & FIRE_KEY) != 0)
      {
        planFire();
      }
    }
    else if (controlType == RELATIVE_CONTROLS)
    {
      if ((lastKeyState & FORWARD_KEY) != 0)
      {
        planMove(SPEED);
      }
      if ((lastKeyState & BACKWARD_KEY) != 0)
      {
        planMove( - SPEED);
      }

      // We enforce delay on turning moves because otherwise it's way too fast to be easily controllable.
      if ((lastKeyState & LEFT_KEY) != 0 && System.currentTimeMillis() - lastTurnTime > TURNING_DELAY)
      {
        lastTurnTime = System.currentTimeMillis();
        planTurn(-1);
      }
      if ((lastKeyState & RIGHT_KEY) != 0 && System.currentTimeMillis() - lastTurnTime > TURNING_DELAY)
      {
        lastTurnTime = System.currentTimeMillis();
        planTurn(1);
      }
    }
  }

  public int getSyncId()
  {
    return syncId;
  }

}

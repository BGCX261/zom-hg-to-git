package World;

import View.Game.Multiplayer.Connection;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * Player
 *
 * @author Tim Perry
 */
public class Player extends Thing {

  // We have a singleton local player, that represents this phone's player.
  private static Player localPlayer;

  // An array of images of the player, one for each angle (0 -> 15)
  private static Image[] playerImages = null;

  private int lastKeyState;
  private long lastTurnTime;
  private long lastMoveTime;

  public static final byte RELATIVE_CONTROLS = 0;
  public static final byte ABSOLUTE_CONTROLS = 1;

  private static final byte FORWARD_KEY = GameCanvas.UP_PRESSED;
  private static final byte BACKWARD_KEY = GameCanvas.DOWN_PRESSED;
  private static final byte LEFT_KEY = GameCanvas.LEFT_PRESSED;
  private static final byte RIGHT_KEY = GameCanvas.RIGHT_PRESSED;

  private static final int SPEED = 3;
  private static final int MOVE_DELAY = 10;
  private static final int TURNING_DELAY = 100;

  private static byte controlType = ABSOLUTE_CONTROLS;

  // Stores an id used to identify us over connection, if/when we sync across the network
  private static int syncId = -1;
  
  public Player() {
    super(7, -1);
    try {
      if (playerImages == null)
      {
        loadPlayerImages();
      }
    }
    catch (IOException e) { System.out.println("Couldn't load player images"); }

    setAngle(8);
  }

  public Player(byte playerId)
  {
    this();
    setThingId(playerId);
  }

  public static void createLocalPlayer(byte id)
  {
    if (localPlayer == null)
    {
      localPlayer = new Player(id);
    }
  }

  // Manage singleton-ness of the local player.
  public static Player getLocalPlayer()
  {
    if (localPlayer == null)
    {
      createLocalPlayer((byte) 0);
    }
    return localPlayer;
  }

  // Load the images for players.
  public static void loadPlayerImages() throws IOException
  {
    playerImages = new Image[16];
    
    for (int ii = 0; ii < 16; ii++)
    {
      playerImages[ii] = Image.createImage("/sprites/player/player - angle"+ii+".png");
    }
  }
  
  public void setKeys(int keyState)
  {
    lastKeyState = keyState;
  }

  public void setControlScheme(int controlType)
  {
    Player.controlType = (byte) controlType;
  }

  public void calculateMoves(World w)
  {
    if (lastKeyState == 0) return;

    if (controlType == ABSOLUTE_CONTROLS)
    { // TODO - don't move if turning?
      if (System.currentTimeMillis() - lastMoveTime <= MOVE_DELAY) return;

      if ((lastKeyState & FORWARD_KEY) != 0)
      {
        lastMoveTime = System.currentTimeMillis();
        planSetAngle(0);
        planMove(SPEED);
      }
      if ((lastKeyState & BACKWARD_KEY) != 0)
      {
        lastMoveTime = System.currentTimeMillis();
        planSetAngle(8);
        planMove(SPEED);
      }
      if ((lastKeyState & LEFT_KEY) != 0)
      {
        lastMoveTime = System.currentTimeMillis();
        planSetAngle(12);
        planMove(SPEED);
      }
      if ((lastKeyState & RIGHT_KEY) != 0)
      {
        lastMoveTime = System.currentTimeMillis();
        planSetAngle(4);
        planMove(SPEED);
      }
    }
    else if (controlType == RELATIVE_CONTROLS)
    {
      if ((lastKeyState & FORWARD_KEY) != 0 && System.currentTimeMillis() - lastMoveTime > MOVE_DELAY)
      {
        lastMoveTime = System.currentTimeMillis();
        planMove(SPEED);
      }
      if ((lastKeyState & BACKWARD_KEY) != 0 && System.currentTimeMillis() - lastMoveTime > MOVE_DELAY)
      {
        lastMoveTime = System.currentTimeMillis();
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

  public void draw(Graphics g)
  {
    g.drawImage(playerImages[getAngle()], getX(), getY(), Graphics.HCENTER|Graphics.VCENTER);
  }

  public int getSyncId()
  {
    return syncId;
  }

  private static byte[] syncTypes = new byte[]
  {
    Connection.INT_TYPE, // Radius
    Connection.INT_TYPE, // X
    Connection.INT_TYPE, // Y
    Connection.INT_TYPE, // Angle
    Connection.BOOL_TYPE, // Solid
  };

  public static void registerForSync()
  {
    syncId = Connection.registerSyncTypes(syncTypes, Player.class);
  }

  private Object[] dataArray = new Object[]
  {
    new Integer(0),
    new Integer(0),
    new Integer(0),
    new Integer(0),
    new Boolean(true),
  };

  public Object[] getData()
  {
    if (((Integer)dataArray[0]).intValue() != getRadius()) dataArray[0] = new Integer(getRadius());
    if (((Integer)dataArray[1]).intValue() != getX()) dataArray[1] = new Integer(getX());
    if (((Integer)dataArray[2]).intValue() != getY()) dataArray[2] = new Integer(getY());
    if (((Integer)dataArray[3]).intValue() != getAngle()) dataArray[3] = new Integer(getAngle());
    if (((Boolean)dataArray[4]).booleanValue() != isSolid()) dataArray[4] = new Boolean(isSolid());
    return dataArray;
  }

  public void loadFromData(Object[] data)
  {
    setRadius(((Integer)data[0]).intValue());
    setX(((Integer)data[1]).intValue());
    setY(((Integer)data[2]).intValue());
    setAngle(((Integer)data[3]).intValue());
    setSolid(((Boolean)data[4]).booleanValue());
  }

}

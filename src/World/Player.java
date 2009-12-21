package World;

import View.Game.Multiplayer.Connection;
import View.Game.Multiplayer.Syncable;
import View.Game.Multiplayer.SyncableFactory;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Player
 *
 * @author Tim Perry
 */
public abstract class Player extends Thing {

  protected static final int SPEED = 5;
  protected static final int MOVE_DELAY = 10;
  protected static final int TURNING_DELAY = 100;

  private static PlayerFactory factory = new PlayerFactory();

  // An array of images of the player, one for each angle (0 -> 15)
  private static Image[] playerImages = null;

  // Stores an id used to identify us over connection, if/when we sync across the network
  protected static int syncId = -1;
  
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

  public int getSyncId()
  {
    return syncId;
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
  
  public void draw(Graphics g)
  {
    g.drawImage(playerImages[getAngle()], getX(), getY(), Graphics.HCENTER|Graphics.VCENTER);
  }

  public static void registerForSync()
  {
    factory.register();
  }

  protected static byte[] syncTypes = new byte[]
  {
    Connection.INT_TYPE, // Radius
    Connection.INT_TYPE, // X
    Connection.INT_TYPE, // Y
    Connection.INT_TYPE, // Angle
    Connection.BOOL_TYPE, // Solid
  };

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

  private static class PlayerFactory implements SyncableFactory
  {
    public void register()
    {
      syncId = Connection.register(syncTypes, this);
    }

    public Syncable buildFromData(Object[] data)
    {
      Player p = new RemotePlayer();
      p.loadFromData(data);
      return p;
    }
  }

}

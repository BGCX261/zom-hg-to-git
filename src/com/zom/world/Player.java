package com.zom.world;

import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;
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
  protected static final int TURNING_DELAY = 100;

  private boolean firePlanned = false;

  private static PlayerFactory factory = new PlayerFactory();

  // An array of images of the player, one for each angle (0 -> 15)
  private static Image[] playerImages = null;

  // Stores an id used to identify us over connection, if/when we sync across the network
  protected static int syncId = -1;

  protected Gun gun = new Pistol();
  
  public Player() {
    super(7);
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

  public void planFire()
  {
    firePlanned = true;
  }

  public void makeMoves(World w)
  {
    if (firePlanned)
    {
      gun.fire(w, getX(), getY(), getAngle(), getThingId());
      firePlanned = false;
    }
    
    super.makeMoves(w);
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

  private Object[] dataArray = new Object[]
  {
    new Integer(getX()), // X
    new Integer(getY()), // Y
    new Integer(getAngle()) // Angle
  };

  public Object[] getData()
  {
    if (((Integer)dataArray[0]).intValue() != getX()) dataArray[0] = new Integer(getX());
    if (((Integer)dataArray[1]).intValue() != getY()) dataArray[1] = new Integer(getY());
    if (((Integer)dataArray[2]).intValue() != getAngle()) dataArray[2] = new Integer(getAngle());
    return dataArray;
  }

  public void loadFromData(Object[] data)
  {
    setX(((Integer)data[0]).intValue());
    setY(((Integer)data[1]).intValue());
    setAngle(((Integer)data[2]).intValue());
  }

  private static class PlayerFactory implements SyncableFactory
  {
    protected static final byte[] syncTypes = new byte[]
    {
      Connection.INT_TYPE, // X
      Connection.INT_TYPE, // Y
      Connection.INT_TYPE // Angle
    };

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

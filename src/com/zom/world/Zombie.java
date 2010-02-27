package com.zom.world;

import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Zombie
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class Zombie extends Thing
{
  protected Thing target;

  public static final int ACTIVATION_DISTANCE = 250;
  public static final int ATTACK_DISTANCE = 150;
  private static final byte SPEED = 2;

  // An array of images of the images, one for each angle (0 -> 15)
  private static Image[] zombieImages = null;

  private static int syncId = -1;
  private static ZombieFactory factory = new ZombieFactory();

  public Zombie() {
    super(6);

    try
    {
      if (zombieImages == null)
      {
        loadZombieImages();
      }
    }
    catch (IOException e)
    {
      System.out.println("Couldn't load zombie images");
    }
  }

  public Zombie(int x, int y)
  {
    this();
    setX(x);
    setY(y);
  }

  // Load the images for zombies.
  public static void loadZombieImages() throws IOException
  {
    zombieImages = new Image[16];

    for (int ii = 0; ii < 16; ii++)
    {
      zombieImages[ii] = Image.createImage("/sprites/zombie/zombie - angle"+ii+".png");
    }
  }

  public void draw(Graphics g)
  {
    g.drawImage(zombieImages[getAngle()], getX(), getY(), Graphics.HCENTER|Graphics.VCENTER);
  }

  public void calculateMoves(World w)
  {
    calculateMoves(w, 1);
  }

  public void calculateMoves(World w, double tickDelta)
  {
    // TODO - Combine this for optimizationstrills.
    target = w.getClosestPlayer(getX(), getY());
    byte desiredAngle = Thing.findAngle(target.getX() - getX(), target.getY() - getY());

    byte angleDelta = (byte) (desiredAngle - getAngle());
    if (angleDelta <= -8) planTurn(1);
    else if (angleDelta < 0) planTurn(-1);
    else if (angleDelta < 8) planTurn(1);
    else planTurn(-1);

    if (World.distanceBetween(this, target) <= ATTACK_DISTANCE) planMove(SPEED);
  }

  public int getSyncId()
  {
    return syncId;
  }

  public static void registerForSync()
  {
    factory.register();
  }

  private Object[] dataArray = new Object[]
  {
    new Integer(getX()),   // X
    new Integer(getY()),   // Y
    new Byte(getAngle())   // Angle
  };

  public Object[] getData()
  {
    if (((Integer)dataArray[0]).intValue() != getX()) dataArray[0] = new Integer(getX());
    if (((Integer)dataArray[1]).intValue() != getY()) dataArray[1] = new Integer(getY());
    if (((Byte)dataArray[2]).byteValue() != getAngle()) dataArray[2] = new Byte(getAngle());
    return dataArray;
  }

  public void updateWithData(Object[] data)
  {
    setX(((Integer)data[0]).intValue());
    setY(((Integer)data[1]).intValue());
    setAngle(((Byte)data[2]).byteValue());
  }

  private static class ZombieFactory implements SyncableFactory
  {
    protected static final byte[] syncTypes = new byte[]
    {
      Connection.INT_TYPE, // X
      Connection.INT_TYPE, // Y
      Connection.BYTE_TYPE // Angle
    };

    public void register()
    {
      syncId = Connection.register(syncTypes, this);
    }

    public Syncable buildFromData(Object[] data)
    {
      Zombie z = new Zombie();
      z.updateWithData(data);
      return z;
    }
  }

}

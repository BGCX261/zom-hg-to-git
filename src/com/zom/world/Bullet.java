package com.zom.world;

import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Bullet
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class Bullet extends Thing implements Syncable
{
  private static int syncId = -1;
  private static BulletFactory factory = new BulletFactory();
  protected static final byte SPEED = 5;

  // Private because bullets should all come from the bullet factory, possibly via makeBullet()
  private Bullet()
  {
    super(2);
  }

  public static Bullet makeBullet(int x, int y, int angle)
  {
    return (Bullet) factory.buildFromData(new Object[]
    {
      new Integer(x),
      new Integer(y),
      new Integer(angle)
    });
  }

  public void draw(Graphics g)
  {
    g.setColor(0, 0, 0);
    g.fillRect(getX(), getY(), getRadius(), getRadius());
  }

  public void calculateMoves(World w)
  {
    planMove(SPEED);
  }

  public void calculateMoves(World w, double tickDelta)
  {
    planMove((int) (SPEED * tickDelta));
  }

  public static void registerForSync()
  {
    factory.register();
  }

  public int getSyncId()
  {
    return syncId;
  }

  private Object[] dataArray = new Object[]
  {
    new Integer(getX()),
    new Integer(getY()),
    new Integer(getAngle())
  };

  public Object[] getData()
  {
    dataArray[0] = new Integer(getX());
    dataArray[1] = new Integer(getY());
    if (((Integer)dataArray[2]).intValue() != getAngle()) dataArray[2] = new Integer(getAngle());

    return dataArray;
  }

  public void loadFromData(Object[] data)
  {
    setX(((Integer)data[0]).intValue());
    setY(((Integer)data[1]).intValue());
    setAngle(((Integer)data[2]).intValue());
  }

  private static class BulletFactory implements SyncableFactory
  {
    protected static final byte[] syncTypes = new byte[]
    {
      Connection.INT_TYPE, // X
      Connection.INT_TYPE, // Y
      Connection.INT_TYPE, // Angle
    };

    public void register()
    {
      syncId = Connection.register(syncTypes, this);
    }

    private static final int BULLET_POOL_SIZE = 100;

    private Vector bulletPool = new Vector();
    private int nextBullet = 0;

    public BulletFactory()
    {
      // If we expand the pool first then we don't do as much stupid resizing.
      bulletPool.ensureCapacity(BULLET_POOL_SIZE);

      // Fill the pool with bullets
      for (int ii = 0; ii < BULLET_POOL_SIZE; ii++)
      {
        bulletPool.addElement(new Bullet());
      }

      nextBullet = 0;
    }

    // We load bullets from the pool. If we run out we re-use bullets, EVEN IF THEY'RE CURRENTLY IN USE.
    public synchronized Syncable buildFromData(Object[] data)
    {
      Bullet b = (Bullet) bulletPool.elementAt(nextBullet);
      nextBullet = (nextBullet + 1) % BULLET_POOL_SIZE;
      b.loadFromData(data);
      return b;
    }
  }

}

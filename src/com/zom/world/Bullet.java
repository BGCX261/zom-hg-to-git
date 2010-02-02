package com.zom.world;

import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Bullet
 *
 * This is used for bullets fire by PLAYERS ONLY. If you want to make a bullet class for use by non players
 * it'll require a few tweaks - players have reliable consitent ids, so firerPlayerId is fine, but others
 * could have their ids changed when the server finds out about them, so this mechanism would break. If you want
 * to do this create a firer reference, and update the id if and when the reference changes (definitely before
 * every outward sync).
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class Bullet extends Thing implements Syncable
{
  private static int syncId = -1;
  private static BulletFactory factory = new BulletFactory();
  protected static final byte SPEED = 10;

  private int firerPlayerId = -1;

  // Private because bullets should all come from the bullet factory, possibly via makeBullet()
  private Bullet()
  {
    super(1);
  }

  public static Bullet makeBullet(int x, int y, int angle, int playerId)
  {
    return (Bullet) factory.buildFromData(new Object[]
    {
      new Integer(x),
      new Integer(y),
      new Integer(angle),
      new Integer(playerId)
    });
  }

  public void draw(Graphics g)
  {
    g.setColor(0, 0, 0);
    g.fillRect(getX(), getY(), getRadius()*2, getRadius()*2);
  }

  public void calculateMoves(World w)
  {
    planMove(SPEED);
  }

  public void calculateMoves(World w, double tickDelta)
  {
    planMove((int) (SPEED * tickDelta));
  }

  public boolean collide(Map map, World w)
  {
    w.removeThing(getThingId());
    return true;
  }

  public boolean collide(Thing t, World w)
  {
    // We don't collide with the Thing that fired us.
    if (t.getThingId() == firerPlayerId) return false;
    else
    {
      w.removeThing(getThingId());
      return true;
    }
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
    new Integer(getAngle()),
    new Integer(firerPlayerId)
  };

  public Object[] getData()
  {
    dataArray[0] = new Integer(getX());
    dataArray[1] = new Integer(getY());
    if (((Integer)dataArray[2]).intValue() != getAngle()) dataArray[2] = new Integer(getAngle());
    if (((Integer)dataArray[3]).intValue() != firerPlayerId) dataArray[3] = new Integer(firerPlayerId);

    return dataArray;
  }

  public void loadFromData(Object[] data)
  {
    setX(((Integer)data[0]).intValue());
    setY(((Integer)data[1]).intValue());
    setAngle(((Integer)data[2]).intValue());
    firerPlayerId = ((Integer) data[3]).intValue();
  }

  private static class BulletFactory implements SyncableFactory
  {
    protected static final byte[] syncTypes = new byte[]
    {
      Connection.INT_TYPE, // X
      Connection.INT_TYPE, // Y
      Connection.INT_TYPE, // Angle
      Connection.INT_TYPE  // Firer ID
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
      b.setThingId(-1);
      return b;
    }
  }

}

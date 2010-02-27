package com.zom.util;

import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;

/**
 * Coord
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class Coord implements Syncable
{
  private static final CoordFactory factory = new CoordFactory();
  private static int syncId = -1;

  public final int x;
  public final int y;

  public Coord(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  public static void registerForSync()
  {
    factory.register();
  }

  public int getSyncId()
  {
    return syncId;
  }

  public Object[] getData()
  {
    return new Object[]
    {
      new Integer(x),
      new Integer(y)
    };
  }

  public void updateWithData(Object[] data)
  {
    throw new UnsupportedOperationException("Coords are immutable, and should NEVER BE UPDATED.");
  }

  private final static class CoordFactory implements SyncableFactory
  {
    public void register()
    {
      syncId = Connection.register(new byte[]
      {
        Connection.INT_TYPE,
        Connection.INT_TYPE
      }, this);
    }

    public Syncable buildFromData(Object[] data)
    {
      return new Coord(((Integer) data[0]).intValue(), ((Integer) data[1]).intValue());
    }

  }
}

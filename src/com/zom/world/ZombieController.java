package com.zom.world;

import com.zom.util.Coord;
import com.zom.view.game.Controller;
import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;
import java.util.Enumeration;
import java.util.Vector;

/**
 * ZombieController
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class ZombieController implements Controller
{
  private static int syncId = -1;
  private static final ZombieControllerFactory factory = new ZombieControllerFactory();

  private Vector spawnPoints;

  public ZombieController(Vector spawnPoints)
  {
    this.spawnPoints = spawnPoints;
  }

  public void run(World world, boolean asSimulation)
  {
    for (Enumeration things = world.getThings(); things.hasMoreElements();)
    {
      Thing t = (Thing) things.nextElement();

      if (World.isPlayerId(t.getThingId()))
      {
        for (int ii = 0; ii < spawnPoints.size(); ii++)
        {
          Coord c = (Coord) spawnPoints.elementAt(ii);
          
          if (World.distanceBetween(c.x, c.y, t.getX(), t.getY()) < Zombie.ACTIVATION_DISTANCE)
          {
            Zombie z = new Zombie(c.x, c.y);
            if (!asSimulation) world.addThing(z);
            spawnPoints.removeElementAt(ii);
            ii--;
          }
        }
      }
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

  private Object[] data = new Object[]
  {
    spawnPoints
  };

  public Object[] getData()
  {
    return data;
  }

  public void updateWithData(Object[] data)
  {
    Vector v = (Vector) data[0];
  }

  private static class ZombieControllerFactory implements SyncableFactory
  {
    public void register()
    {
      syncId = Connection.register(new byte[]
        {
          Connection.VECTOR_OF_SYNCABLE_TYPE // Spawns
        }, null); // Null factory, because ZombieControllers SHOULD NOT BE BUILT OVER THE NETWORK. WorldBuilders make them, the network updates them.
    }

    public Syncable buildFromData(Object[] data)
    {
      throw new UnsupportedOperationException("ZombieControllers should not be build over the network.");
    }
  }
}

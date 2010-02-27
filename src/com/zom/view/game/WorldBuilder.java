package com.zom.view.game;

import com.zom.util.Coord;
import com.zom.view.game.multiplayer.Connection;
import com.zom.world.*;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;
import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 * WorldBuilder
 *
 * This used to be an abstract class, but now isn't. Despite this, it is not intended to be
 * specifically used standalone. When used standalone it requires every parameter stored within
 * it to be set up, and it then builds the world from there. If it doesn't already have all the
 * information required this doesn't do anything but throw an exception. It is to be used entirely
 * as a way to get (and subsequently use) the full and complete details required for building a world,
 * without any extra computation (no looking at files, generating things, etc).
 *
 * @author Tim Perry
 */
public class WorldBuilder implements Syncable
{
  private static int fullSyncId = -1;
  private static WorldBuilderFactory factory = new WorldBuilderFactory();

  // The parameters we need to calculate to build a world.
  protected int width;
  protected int height;
  protected Image mapBackground;
  protected boolean[][] collisionMap;
  protected Vector zombieSpawns;
  protected Coord[] playerSpawns;

  // We keep references to what we build, so that we can serialize ourselves better later.
  protected Map map;
  protected World world;

  // Set parameters for this world, before building it.
  protected void setDifficulty(byte difficulty) { }

  // Using the various details you've be given, generate and return a world.
  public World buildWorld() throws InstantiationException
  {
    if (!ready()) throw new InstantiationException();
    else return constructWorldFromClassVariables();
  }

  protected World constructWorldFromClassVariables()
  {
    return constructWorldFromParameters(width, height, mapBackground, collisionMap, playerSpawns);
  }
  
  protected World constructWorldFromParameters(int width, int height, Image mapBackground, boolean[][] collisionMap, Coord[] playerSpawns)
  {
    map = new Map(width, height, mapBackground, collisionMap, playerSpawns);
    world = new World(map);
    return world;
  }

  // Returns a vector of the controllers required to run the world that we're generating. Subclasses almost certainly want
  // to just use this version, but could (at their own risk) override in special cases. It is generally recommended that you
  // make the call to buildWorld before buildControllers, as data generated in buildWorld could be relevant to get controllers,
  // and it's likely more efficient for the data to be ready already.
  public Vector buildControllers() throws InstantiationException
  {
    Vector v = new Vector();
    v.addElement(new ZombieController(zombieSpawns));
    return v;
  }

  // Do we have all the data we need to produce a world + it's controllers. This is useful because implementations will/should, by default, attempt
  // to send the minimum data required to build the world. Subclasses should override such that if they currently have too little data to
  // create a world in full they return false (more likely, they return false || super.ready(), as the below should be the maximum information
  // required)
  public boolean ready()
  {
    if (width != -1 && height != -1 && mapBackground != null && collisionMap != null && zombieSpawns != null) return true;
    if (world != null && map != null) return true;
    else return false;
  }

  public static void registerForSync()
  {
    factory.register();
  }

  public int getSyncId()
  {
    return getFullSyncId();
  }

  public final int getFullSyncId()
  {
    return fullSyncId;
  }

  public Object[] getData()
  {
    return getFullData();
  }

  public final Object[] getFullData()
  {
    return new Object[]
    {
      new Integer(width),
      new Integer(height),
      mapBackground,
      collisionMap,
      zombieSpawns
    };
  }

  public void updateWithData(Object[] data)
  {
    updateWithFullData(data);
  }

  public final void updateWithFullData(Object[] data)
  {
    width = ((Integer) data[0]).intValue();
    height = ((Integer) data[1]).intValue();
    mapBackground = ((Image) data[2]);
    collisionMap = (boolean[][]) data[3];
    zombieSpawns = (Vector) data[4];
  }

  private final static class WorldBuilderFactory implements SyncableFactory
  {
    public void register()
    {
      fullSyncId = Connection.register(new byte[]
        {
          Connection.INT_TYPE, // Width
          Connection.INT_TYPE, // Height
          Connection.IMAGE_TYPE, // Background
          Connection.BOOL_ARRAY_2D_TYPE, // Collision map
          Connection.VECTOR_OF_SYNCABLE_TYPE // Spawn points
        }, this);
    }

    public Syncable buildFromData(Object[] data)
    {
      WorldBuilder wb = new WorldBuilder();
      wb.updateWithData(data);
      return wb;
    }
  }

}

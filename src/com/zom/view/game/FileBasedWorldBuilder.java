package com.zom.view.game;

import com.zom.util.Coord;
import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;
import com.zom.world.World;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 * FileBasedWorldBuilder
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class FileBasedWorldBuilder extends WorldBuilder
{
  private static int syncId = -1;
  private static final FileBasedWorldBuilderFactory factory = new FileBasedWorldBuilderFactory();

  private byte difficulty = 1;

  private String mapName;
  private String mapFolder;

  // N.B. Although there are some valid reasons to use this, you must remember that until ready() is true,
  // building a world from this WILL FAIL.
  public FileBasedWorldBuilder() { }

  public FileBasedWorldBuilder(String mapName)
  {
    setMap(mapName);
  }

  public void setMap(String mapName)
  {
    System.out.println("Loading map "+mapName);
    this.mapName = mapName;
    mapFolder = "/maps/" + mapName + "/";

    zombieSpawns = null;
    width = 0;
    height = 0;
    mapBackground = null;
    collisionMap = null;
  }

  protected void setDifficulty(byte difficulty)
  {
    this.difficulty = difficulty;
  }

  // Checks whether the basic file (and thus the map folder) exists, or whether we just have all the data required
  // anyway. ASSUMES THAT IF YOU HAVE A /maps/mapname/basics.coord FILE YOU ALSO HAVE THE OTHER REQUIRED FILES
  public boolean ready()
  {
    InputStream basicsInputStream = System.class.getResourceAsStream(mapFolder + "basics.coord");

    return basicsInputStream != null || super.ready();
  }

  public World buildWorld() throws InstantiationException
  {
    try
    {
      InputStream basicsInputStream = System.class.getResourceAsStream(mapFolder + "basics.coord");
      DataInputStream basicsDataStream = new DataInputStream(basicsInputStream);

      // Have we got the files we need?
      if (basicsInputStream == null || basicsDataStream.available() < 8)
      {
        // If we haven't got the files, have we at least already got the data?
        if (!super.ready()) throw new IOException("Couldn't get required details from basics.coord");
      }
      // We've got the files, get our data up to date.
      else
      {
        width = basicsDataStream.readInt();
        height = basicsDataStream.readInt();

        basicsDataStream.close();

        mapBackground = Image.createImage(mapFolder + "bg.png");
        collisionMap = loadCollisionMap(mapFolder);

        Vector playerSpawnVector = loadSpawnPoints(mapFolder, "player", "");
        playerSpawns = new Coord[playerSpawnVector.size()];
        playerSpawnVector.copyInto(playerSpawns);
      }

      // Somehow we have the data we need. Use it to get a world!
      return constructWorldFromClassVariables();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new InstantiationException("Failed to load required files for map. "+e.getMessage());
    }
  }

  public Vector buildControllers() throws InstantiationException
  {
    if (zombieSpawns == null)
    {
      // We try and load the zombie spawns.
      try
      {
        zombieSpawns = loadSpawnPoints(mapFolder, "zombie", difficulty);
      }
      // If we can't, and we don't already have some zombie spawns from somewhere, throw an exception.
      catch (IOException e)
      {
        throw new InstantiationException("Failed to load required files for controllers. "+e.getMessage());
      }
    }
    
    // We've got some spawn points from somewhere. Return them.
    return super.buildControllers();
  }

  // Reads from a file, converts it to a 2d boolean array and returns it
  private static boolean[][] loadCollisionMap(String mapFolder) throws IOException
  {
    InputStream fileStream = System.class.getResourceAsStream(mapFolder + "collisionMap.boolss");
    if (fileStream == null) throw new IOException();

    DataInputStream dataInput = new DataInputStream(fileStream);

    int collWidth = dataInput.readInt();
    int collHeight = dataInput.readInt();
    boolean[][] loadedMap = new boolean[collWidth][collHeight];

    // Load all the collision data into that array.
    for (int x = 0; x < collWidth; x++)
    {
      for (int y = 0; y < collHeight; y++)
      {
        loadedMap[x][y] = dataInput.readBoolean();
      }
    }

    dataInput.close();

    return loadedMap;
  }

  private static Vector loadSpawnPoints(String mapFolder, String type, byte difficulty) throws IOException
  {
    return loadSpawnPoints(mapFolder, type, String.valueOf(difficulty));
  }

  private static Vector loadSpawnPoints(String mapFolder, String type, String difficulty) throws IOException
  {
    InputStream fileStream = System.class.getResourceAsStream(mapFolder + type + "spawns" + difficulty + ".coords");
    if (fileStream == null) throw new IOException();

    DataInputStream dataInput = new DataInputStream(fileStream);

    Vector spawns = new Vector();

    // Load all the collision data into that array.
    while (dataInput.available() >= 8)
    {
      spawns.addElement(new Coord(dataInput.readInt(), dataInput.readInt()));
    }
    
    dataInput.close();
    return spawns;
  }

  public int getSyncId()
  {
    return syncId;
  }

  public static void registerForSync()
  {
    factory.register();
  }

  public Object[] getData()
  {
    if (zombieSpawns == null) try
    {
      loadSpawnPoints(mapFolder, "zombie", difficulty);
    }
    catch (IOException ex)
    {
      System.out.println("Somebody asked us for FBWorldBuilder spawns data, and we have neither a file, nor any data!");
      throw new Error("Somebody asked us for FBWorldBuilder spawns data, and we have neither a file, nor any data!");
    }
    
    return new Object[]
      {
        mapName,
        zombieSpawns
      };
  }

  public void updateWithData(Object[] data)
  {
    // We throw away data[0] since it has been used already by the factory on our constructor.
    zombieSpawns = (Vector) data[1];
  }

  public static class FileBasedWorldBuilderFactory implements SyncableFactory
  {
    public void register()
    {
      syncId = Connection.register(new byte[]
        {
          Connection.STRING_TYPE, // Map name
          Connection.VECTOR_OF_SYNCABLE_TYPE // Spawn points
        }, this);
    }

    public Syncable buildFromData(Object[] data)
    {
      WorldBuilder wb = new FileBasedWorldBuilder((String) data[0]);
      wb.updateWithData(data);
      return wb;
    }
  }

}

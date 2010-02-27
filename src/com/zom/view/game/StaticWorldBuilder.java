package com.zom.view.game;

import com.zom.util.Coord;
import com.zom.world.*;
import com.zom.view.game.multiplayer.Connection;
import com.zom.view.game.multiplayer.Syncable;
import com.zom.view.game.multiplayer.SyncableFactory;
import javax.microedition.lcdui.Image;

/**
 * StaticWorldFactory
 *
 * Syncable, for nice OO, but doesn't actually sync anything - the idea
 * is that this class is always the same, and totally parameterless.
 *
 * @author Tim Perry
 */
public class StaticWorldBuilder extends WorldBuilder {

  // Register us, so that worlds can be sent via Connections.
  private static int syncId;
  private static StaticWorldBuilderFactory factory = new StaticWorldBuilderFactory();

  protected void setDifficulty(byte difficulty) { }

  public World buildWorld()
  {
    // Builds an empty 100x100 map.
    return constructWorldFromParameters(100, 
                                        100,
                                        Image.createImage(100, 100),
                                        new boolean[100][100],
                                        new Coord[] { new Coord(20, 20), new Coord(40, 40), new Coord(20,40), new Coord(40,20) }
    );
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
    return new Object[]
    {
    };
  }

  public void updateWithData(Object[] data)
  {
  }

  public static class StaticWorldBuilderFactory implements SyncableFactory
  {
    public void register()
    {
      syncId = Connection.register(new byte[] { }, this);
    }

    public Syncable buildFromData(Object[] data)
    {
      return new StaticWorldBuilder();
    }
  }

}

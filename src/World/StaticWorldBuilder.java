package World;

import View.Game.Multiplayer.Connection;
import View.Game.Multiplayer.Syncable;
import View.Game.Multiplayer.SyncableFactory;

/**
 * StaticWorldFactory
 *
 * Syncable, for nice OO, but doesn't actually sync anything - the idea
 * is that this class is always the same, and totally parameterless.
 *
 * @author Tim Perry
 */
public class StaticWorldBuilder implements WorldBuilder {

  // Register us, so that worlds can be sent via Connections.
  private static int syncId;
  private static StaticWorldBuilderFactory factory = new StaticWorldBuilderFactory();

  public void setDifficulty() { }

  public World buildWorld()
  {
    Map m = new Map("map1");
    World w = new World(m);

    // Add the local player to the world.
    LocalPlayer.getLocalPlayer().setX(m.getPlayerStartX());
    LocalPlayer.getLocalPlayer().setY(m.getPlayerStartY());

    w.addThing(LocalPlayer.getLocalPlayer());
    
    return w;
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

  public void loadFromData(Object[] data)
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

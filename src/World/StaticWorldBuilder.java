package World;

import View.Game.Multiplayer.Connection;

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

  public void setDifficulty() { }

  public World buildWorld()
  {
    Map m = new Map("map1");
    World w = new World(m);

    // Add the local player to the world.
    Player.getLocalPlayer().setX(m.getPlayerStartX());
    Player.getLocalPlayer().setY(m.getPlayerStartY());

    w.addThing(Player.getLocalPlayer());
    
    return w;
  }

  public int getSyncId()
  {
    return syncId;
  }

  public static void registerForSync()
  {
    syncId = Connection.registerSyncTypes(
      new byte[]
      {
      },
      StaticWorldBuilder.class
    );
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

}

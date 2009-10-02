package World;

/**
 * StaticWorldFactory
 *
 * @author Tim Perry
 */
public class StaticWorldBuilder implements WorldBuilder {

  private int width = 1000;
  private int height = 1000;

  public void setDifficulty()
  {

  }

  public World buildWorld()
  {
    Map m = new Map();
    World w = new World(m);
    return w;
  }

}

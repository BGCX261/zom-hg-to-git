package World;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 * World
 *
 * @author Tim Perry
 */
public class World {

  private final Map map;

  private final Vector dynamicThings;

  public World(Map m)
  {
    map = m;
    dynamicThings = new Vector();
  }

  public Map getMap()
  {
    return map;
  }

  public void addThing(Thing thing)
  {
    dynamicThings.addElement(thing);
  }

  public Vector getThings() {
    return dynamicThings;
  }

  // Returns true if there is no solid Thing or static element of the world at the given coordinates.
  // TODO - high priority - think about how width/height affects this.
  public boolean isEmpty(int x, int y)
  {
    return true;
  }

  public void tick()
  {
    // Go through every non-static thing, ask it what it wants to do, do it.
    // Run every controller.
    for (int i = 0; i<dynamicThings.size(); i++)
    {
      Thing t = (Thing) dynamicThings.elementAt(i);
      t.calculateMoves(this);
      t.makeMoves(this);
    }
  }

}

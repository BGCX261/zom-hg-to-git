package World;

import com.zom.util.Queue;
import java.util.Vector;

/**
 * World
 *
 * This could be syncable, but isn't because of the static bits (map), which make it
 * more sensible to sync a worldbuilder if you want a fresh world, and sync the Things
 * with getThings & setThings if you don't.
 *
 * @author Tim Perry
 */
public class World {

  private final Map map;
  private final Thing[] indexedThings;
  private final Vector thingVector;
  private int nextId = 10;
  private static final int MAX_THINGS = 100;
  private final Queue thingsToAdd = new Queue();

  // Positive indicates number of readers with locks, negative is number
  // of writers with locks (can only be -1), 0 is unlocked.
  private byte lock = 0;

  // Worlds should ONLY BE CONSTRUCTED BY WORLDBUILDERS.
  public World(Map m)
  {
    map = m;
    thingVector = new Vector();
    indexedThings = new Thing[MAX_THINGS];
  }

  public Map getMap()
  {
    return map;
  }

  public void addThing(Thing thing)
  {
    thingsToAdd.enqueue(thing);
  }

  public void forceAdd()
  {
    if (!thingsToAdd.empty()) System.out.println("Forcibly adding");
    while (!thingsToAdd.empty())
    {
      Thing thing = (Thing) thingsToAdd.dequeue();

      if (thing.getThingId() == -1)
      {
        thing.setThingId(nextId);
        nextId++;
      }

      thingVector.addElement(thing);
      indexedThings[thing.getThingId()] = thing;
    }
  }

  public void removeThing(int thingId)
  {
    thingVector.removeElement(indexedThings[thingId]);
    indexedThings[thingId] = null;
  }

  public Vector getThings()
  {
    return thingVector;
  }

  public Thing getThing(int thingId)
  {
    return indexedThings[thingId];
  }

  public int getThingCount()
  {
    return thingVector.size();
  }

  // Returns true if there is no solid Thing or static element of the world at the given coordinates.
  public boolean isEmpty(int x, int y, int radius)
  {
    // TODO - thing to thing collision
    return map.isPositionFree(x, y, radius);
  }

  // Gets every element to work out where it wants to move to, and then moves every element.
  // TODO - Check the collision here isn't going to fall apart now I've moved this about.
  // REPORT - Can talk about refactoring this to allow concurrency better!
  public void tick()
  {
    lockForRead();
    // Go through every thing and have it work out what it wants to do.
    for (int ii = 0; ii < thingVector.size(); ii++)
    {
      Thing thing = (Thing) thingVector.elementAt(ii);
      thing.calculateMoves(this);
    }
    unlock();

    // Before we actually make the moves we need to make sure nobody else is trying
    // to read the world for syncing with other phones, or similar, so we get a lock.
    lockForWrite();
    for (int ii = 0; ii < thingVector.size(); ii++)
    {
      Thing thing = (Thing) thingVector.elementAt(ii);
      thing.makeMoves(this);
    }

    forceAdd();
    unlock();
  }

  public synchronized void lockForRead()
  {
    while (lock < 0)
    {
      try { wait(); }
      catch (Exception e) { }
    }
    lock += 1;
  }

  public synchronized void lockForWrite()
  {
    while (lock != 0)
    {
      try { wait(); }
      catch (Exception e) { }
    }
    lock = -1;
  }

  // Errors if called when world is not locked!
  public synchronized void unlock()
  {
    if (lock > 0) lock--;
    else if (lock < 0) lock++;
    else throw new Error("Unlocking world that is not locked! SHOULD NEVER HAPPEN.");
    
    if (lock == 0) notifyAll();
  }

}

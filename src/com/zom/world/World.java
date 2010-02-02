package com.zom.world;

import com.zom.util.Queue;
import java.util.Enumeration;
import java.util.Hashtable;
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

  private final Hashtable things;
  private int thingCount = 0;
  private int nextId = FIRST_ALLOCATABLE_THING_ID;
  private static final int FIRST_ALLOCATABLE_THING_ID = 10;
  private static final int MAX_THING_ID = 100;
  private final Queue thingsToAdd = new Queue();
  private final Queue thingsToRemove = new Queue();

  private long lastTickTime = 0;

  private final Map map;

  private Vector lifeListeners = new Vector();

  // Positive indicates number of readers with locks, negative is number
  // of writers with locks (can only be -1), 0 is unlocked.
  private byte lock = 0;

  // Worlds should ONLY BE CONSTRUCTED BY WORLDBUILDERS.
  public World(Map m)
  {
    map = m;
    things = new Hashtable();
  }

  public Map getMap()
  {
    return map;
  }

  public void addThingLifeListener(ThingLifeListener l)
  {
    lifeListeners.addElement(l);
  }

  public void removeThingLifeListener(ThingLifeListener l)
  {
    lifeListeners.removeElement(l);
  }

  public int getNextId()
  {
    // In the trivial (expected) case return the next id, and post-increment to the next id.
    if (!things.containsKey(new Integer(nextId))) return nextId++;

    // Iterate (circularly) over all available ids, until we reach the point where we
    // started, or until a spare id is found.
    for (int ii = nextId + 1; ii != nextId; ii++)
    {
      if (!things.containsKey(new Integer(ii))) return ii;
      if (ii == MAX_THING_ID - 1) ii = FIRST_ALLOCATABLE_THING_ID - 1;
    }

    throw new Error("No space for new Thing in World!");
  }

  // Write lock not required for this!
  public void addThing(Thing thing)
  {
    thingsToAdd.enqueue(thing);
  }

  // Write lock not required.
  public void removeThing(int thingId)
  {
    thingsToRemove.enqueue(new Integer(thingId));
  }

  // Before using this you MUST HAVE A WRITE LOCk
  public void forceUpdate()
  {
    // Do we have a birth to announce?
    boolean birth = false;

    while (!thingsToAdd.empty())
    {
      Thing thing = (Thing) thingsToAdd.dequeue();

      if (thing.getThingId() == -1)
      {
        thing.setThingId(getNextId());
        // We only announce births when we've allocated a thing id, i.e. when
        // it wasn't added to a different world already by somebody else.
        birth = true;
      }

      things.put(new Integer(thing.getThingId()), thing);
      thingCount++;
      if (nextId == thing.getThingId()) nextId++;

      // If birth has occured, and somebody cares, tell them.
      if (birth == true)
      {
        announceBirth(thing);
        birth = false;
      }
    }

    while (!thingsToRemove.empty())
    {
      Integer id = (Integer) thingsToRemove.dequeue();

      if (things.containsKey(id))
      {
        Thing t = (Thing) things.get(id);

        things.remove(id);
        thingCount--;

        announceDeath(t);
      }
    }
  }

  // Before using this you MUST HAVE A WRITE LOCK
  public void changeThingId(int oldThingId, int newThingId)
  {
    // Trivial case.
    if (oldThingId == newThingId) return;

    // TODO - Better name!
    Integer oldId = new Integer(oldThingId);

    Thing t = (Thing) things.get(oldId);
    
    things.remove(oldId);

    t.setThingId(newThingId);
    things.put(new Integer(newThingId), t);
  }

  public void announceBirth(Thing t)
  {
    for (int ii = 0; ii < lifeListeners.size(); ii++)
    {
      ((ThingLifeListener) lifeListeners.elementAt(ii)).thingBorn(t);
    }
  }

  public void announceDeath(Thing t)
  {
    for (int ii = 0; ii < lifeListeners.size(); ii++)
    {
      ((ThingLifeListener) lifeListeners.elementAt(ii)).thingDied(t);
    }
  }

  public int getThingCount()
  {
    return thingCount;
  }

  public Enumeration getThings()
  {
    return things.elements();
  }

  public Thing getThing(int thingId)
  {
    return (Thing) things.get(new Integer(thingId));
  }

  // Looks at the planned position of a thing, and checks that it's free of obstacles either in terms of the map, or of other Things.
  // If it looks like there is a collision it asks the relevant parties, via their collide() methods, whether collision should occur.
  // Return value indicates whether a collision occurs (true is yes, false is no).
  public boolean doesPlanHaveCollisions(Thing t)
  {
    if (!map.isPositionFree(t.getPlannedX(), t.getPlannedY(), t.getRadius()) && t.collide(map, this)) return true;

    // Go through every thing, and see if they're on this spot.
    for (Enumeration thingsEnum = getThings(); thingsEnum.hasMoreElements();)
    {
      Thing otherThing = (Thing) thingsEnum.nextElement();
      if (otherThing == t) continue;
      
      int distance = t.getRadius() + otherThing.getRadius();

      // If the things are suitable close to one another, and they both agree that collision should occur, it occurs.
      if (Math.abs(otherThing.getX() - t.getPlannedX()) < distance &&
          Math.abs(otherThing.getY() - t.getPlannedY()) < distance &&
          t.collide(otherThing, this) && otherThing.collide(t, this)) return true;
    }

    return false;
  }

  // Gets every element to work out where it wants to move to, and then moves every element.
  // REPORT - Can talk about refactoring this to allow concurrency better!
  public void tick()
  {
    lockForRead();

    // Go through every thing and have it work out what it wants to do.
    for (Enumeration thingsEnum = getThings(); thingsEnum.hasMoreElements();)
    {
      ((Thing) thingsEnum.nextElement()).calculateMoves(this);
    }

    unlock();

    // Before we actually make the moves we need to make sure nobody else is trying
    // to read the world for syncing with other phones, or similar, so we get a lock.
    lockForWrite();

    for (Enumeration thingsEnum = getThings(); thingsEnum.hasMoreElements();)
    {
      ((Thing) thingsEnum.nextElement()).makeMoves(this);
    }
    lastTickTime = System.currentTimeMillis();

    forceUpdate();
    unlock();
  }

  public long getLastTickTime()
  {
    return lastTickTime;
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

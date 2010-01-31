package com.zom.world;

/**
 * Listener that wants to be informed whenever something is born or died;
 * this is, whenever something is added or removed from the World.
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public interface ThingLifeListener
{

  public void thingBorn(Thing t);

  public void thingDied(Thing t);

}

package com.zom.world;

/**
 * Gun
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public abstract class Gun
{
  public abstract void fire(World w, int x, int y, int angle, int playerId);

  public abstract void reload();
}

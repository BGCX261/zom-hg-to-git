package com.zom.world;

/**
 * Pistol
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class Pistol extends Gun
{
  protected final static int FIRE_DELAY = 500;
  private long lastFireTime = 0;

  public void fire(World w, int x, int y, int angle, int playerId)
  {
    if (System.currentTimeMillis() - lastFireTime > FIRE_DELAY)
    {
      System.out.println("firing, x = "+x+", y = "+y+", angle = "+angle+", playerId = "+playerId);
      Bullet b = Bullet.makeBullet(x, y, angle, playerId);
      w.addThing(b);
      
      lastFireTime = System.currentTimeMillis();
    }
  }

  public void reload() { }
}

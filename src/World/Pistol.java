package World;

/**
 * Pistol
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class Pistol extends Gun
{
  protected final static int FIRE_DELAY = 500;
  private long lastFireTime = 0;

  public void fire(World w, int x, int y, int angle)
  {
    if (System.currentTimeMillis() - lastFireTime > FIRE_DELAY)
    {
      Bullet b = Bullet.makeBullet(x, y, angle);
      w.addThing(b);
      
      lastFireTime = System.currentTimeMillis();
    }
  }

  public void reload() { }
}

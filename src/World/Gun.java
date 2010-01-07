package World;

/**
 * Gun
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public abstract class Gun
{
  public abstract void fire(World w, int x, int y, int angle);

  public abstract void reload();
}

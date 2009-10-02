package World;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.Sprite;

/**
 * Thing
 *
 * @author Tim Perry
 */
public abstract class Thing {

  private int x;
  protected int plannedX;

  private int y;
  protected int plannedY;
  
  // Direction this Thing is facing, in radians.
  private double angle;
  protected double plannedAngle;

  private final int width;
  private final int height;

  // Thing's are only considered for collision if they are solid. If not then other Things can (and will) go straight though them.
  protected boolean solid = true;

  public Thing (int width, int height)
  {
    this.width = width;
    this.height = height;
  }

  public int getHeight()
  {
    return height;
  }

  public int getWidth()
  {
    return width;
  }

  public int getX()
  {
    return x;
  }

  public int getY()
  {
    return y;
  }

  public void setX(int x)
  {
    this.x = x;
    plannedX = x;
  }

  public void setY(int y)
  {
    this.y = y;
    plannedY = y;
  }

  public double getAngle()
  {
    return angle;
  }

  public void setAngle(double angle)
  {
    while (angle < 0) angle += Math.PI*2;
    this.angle = angle % (Math.PI*2);
    
    // We also need to update the plannedAngle - essentially if we've set where we're pointing, we always want to keep
    // pointing that way until something else says otherwise.
    plannedAngle = this.angle;
  }

  // Moves distance forward (or backwards if negative) in the direction this thing is facing.
  public void planMove(int distance)
  {
    plannedX = getX() + (int) (distance * Math.sin(getAngle()));
    plannedY = getY() + (int) (distance * Math.cos(getAngle()));
  }

  // Moves distance perpendicular to the direction this thing is facing (positive = right) TODO
  public void planStrafe(int distance)
  {

  }

  public boolean isSolid()
  {
    return solid;
  }

  public void setSolid(boolean solid)
  {
    this.solid = solid;
  }

  public void draw(Graphics g)
  {
    g.setColor(0,0,0);
    g.fillRect(x, y, width, height);
  }

  // Subclasses should define AI of some sort here to work out where this object should go given
  // the world that it's in.
  public abstract void calculateMoves(World w);

  // Make the moves calculated in calculate moves, if the space we wanted to move into is free of any solid objects, or we aren't solid.
  // Subclasses could override this if they want to perform further actions, such as fire weapons, but in most cases a controller would be better,
  // to fire weapons on click, or event (fire if gunman in range, or similar)
  public void makeMoves(World w)
  {
    setAngle(plannedAngle);
    if (!isSolid() || w.isEmpty(plannedX, plannedY))
    {
      setX(plannedX);
      setY(plannedY);
    }
  }

}

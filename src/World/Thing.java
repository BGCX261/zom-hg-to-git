package World;

import View.Game.Multiplayer.Connection;
import View.Game.Multiplayer.Syncable;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;

/**
 * Thing
 *
 * @author Tim Perry
 */
public abstract class Thing implements Syncable {

  // Lookup tables to avoid us having to convert between our angle system (mod 16) to radians all the time.
  private final static double[] COS_LOOKUP_TABLE = { 1, 0.92, 0.71, 0.38, 0, -0.38, -0.71, -0.92, -1, -0.92, -0.71, -0.38, 0, 0.38, 0.71, 0.92 };
  private final static double[] SIN_LOOKUP_TABLE = { 0, 0.38, 0.71, 0.92, 1, 0.92, 0.71, 0.38, 0, -0.38, -0.71, -0.92, -1, -0.92, -0.71, -0.38 };

  private int x;
  protected int plannedX;

  private int y;
  protected int plannedY;
  
  // Direction this Thing is facing. Angles are measured clockwise, and are integers mod 16, with 0 being straight up
  private int angle;
  protected int plannedAngle;

  // A unique id for a thing within a world. This id is chosen by the world, when the thing is added to the world.
  private int thingId = -1;

  private final int radius;

  // Things are only considered for collision if they are solid. If not then other Things can (and will) go straight though them.
  protected boolean solid = true;

  public Thing(int radius)
  {
    this.radius = radius;
  }

  public Thing(int radius, int thingId)
  {
    this(radius);
    setThingId(thingId);
  }

  public int getRadius()
  {
    return radius;
  }

  public void setThingId(int thingId)
  {
    this.thingId = thingId;
  }

  public int getThingId()
  {
    return thingId;
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

  public int getPlannedX()
  {
    return plannedX;
  }

  public int getPlannedY()
  {
    return plannedY;
  }

  // Angles are measured clockwise, and are integers mod 16, with 0 being straight up
  public int getAngle()
  {
    return angle;
  }

  public int getPlannedAngle()
  {
    return plannedAngle;
  }

  // Angles are measured clockwise, and are integers mod 16, with 0 being straight up
  public void setAngle(int angle)
  {
    this.angle = makeAngle(angle);
    
    // We also need to update the plannedAngle - essentially if we've set where we're pointing, we always want to keep
    // pointing that way until something else says otherwise.
    plannedAngle = this.angle;
  }

  // Plan to set the angle
  public void planSetAngle(int angle)
  {
    this.plannedAngle = makeAngle(angle);
  }

  // Plans a turn, angle steps around. Turns are measured clockwise, and angles are integers mod 16, with 0 being straight up.
  // Negative angles here are fine, and will work as would be expected.
  public void planTurn(int angle)
  {
    this.plannedAngle = makeAngle(getAngle()+angle);
  }
  
  // Converts an int into a valid angle (basically just a mod function)
  public int makeAngle(int angle)
  {
    // Have to push the angle to positive, because java's mod function does not do sensible things with negative numbers.
    while (angle < 0)
    {
      angle += 16;
    }
    return angle % 16;
  }

  // Cos function, defined over our angle system (integers, mod 16)
  protected double cos(int angle)
  {
    return COS_LOOKUP_TABLE[makeAngle(angle)];
  }

  // Sine function, defined over our angle system (integers, mod 16) 
  protected double sin(int angle)
  {
    return SIN_LOOKUP_TABLE[makeAngle(angle)];
  }

  // Moves distance forward (or backwards if negative) in the direction this thing is facing.
  public void planMove(int distance)
  {
    plannedX = getX() + (int) (distance * sin(getPlannedAngle()));
    plannedY = getY() - (int) (distance * cos(getPlannedAngle()));
  }

  public boolean isSolid()
  {
    return solid;
  }

  public void setSolid(boolean solid)
  {
    this.solid = solid;
  }

  public abstract void draw(Graphics g);

  // Subclasses should define AI of some sort here to work out where this object should go given
  // the world that it's in.
  public abstract void calculateMoves(World w);

  // Make the moves calculated in calculate moves, if the space we wanted to move into is free of any solid objects, or we aren't solid.
  // Subclasses could override this if they want to perform further actions, such as fire weapons, but in most cases a controller would be better,
  // to fire weapons on click, or event (fire if gunman in range, or similar)
  public void makeMoves(World w)
  {
    setAngle(plannedAngle);
    if (!isSolid() || w.isEmpty(plannedX, plannedY, radius))
    {
      setX(plannedX);
      setY(plannedY);
    }
  }

  // Fix a collision that exists between you and your planned position. Most Things should reset to their current position, some things will do other things
  // (e.g. bullets cease to exist on contact with walls, or could bounce?)
  public void fixPlannedCollide(Map map) { }

}

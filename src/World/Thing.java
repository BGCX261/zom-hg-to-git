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

  // We can't set this to final, because j2me reflection requires us to have an argumentless constructor to make
  // our reflection in Connection work properly. Instead, we error if nobody has set it by the time it gets used (see getRadius).
  private int radius = -1;

  // Thing's are only considered for collision if they are solid. If not then other Things can (and will) go straight though them.
  protected boolean solid = true;

  // Any uses of this function must immediately be followed by a call to loadData. This should only be used for generating
  // empty Things, ready to be packed with data from (for example) the network.
  public Thing() { }

  public Thing (int radius, int thingId)
  {
    setRadius(radius);
    setThingId(thingId);
  }

  // Set the radius, if it hasn't been set already.
  public void setRadius(int radius)
  {
    if (this.radius < 0 && radius > 0) this.radius = radius;
  }

  // Returns the radius, or 0 if not yet set (hopefully should never happen).
  public int getRadius()
  {
    if (radius == -1) throw new Error("Attempting to use Thing that is not yet initialised!");
    return (radius > 0 ? radius : 0);
  }

  // Sets the thing id, if it hasn't been set already.
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

  // FIx a collision that exists between you and your planned position. Most Things should reset to their current position, some things will do other things
  // (e.g. bullets cease to exist on contact with walls, or could bounce?)
  public void fixPlannedCollide(Map map) { }

}

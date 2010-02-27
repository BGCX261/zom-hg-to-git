package com.zom.world;

/**
 * RemotePlayer
 *
 * @author Tim Perry
 */
public class RemotePlayer extends Player {

  private int lastXUpdate = -1;
  private int lastYUpdate = -1;

  private int previousXUpdate = -1;
  private int previousYUpdate = -1;

  public void calculateMoves(World w)
  {
    calculateMoves(w, 1);
  }

  public void calculateMoves(World w, double timeDelta)
  {
    // If these aren't set yet then we don't really have enough data to extrapolate.
    if (previousXUpdate == -1 || previousYUpdate == -1) return;

    // If we moved last tick, we're probably still moving (we just continue in whatever direction we're facing).
    if (lastXUpdate - previousXUpdate != 0 || lastYUpdate - previousYUpdate != 0)
    {

      // Minus one here because it creates a smoother effect - if it's wrong (likely) it's only a small
      // change, and a network update invisibly fixes it. This also smooths out movement when they have stopped,
      // as we're closer to where we should be.
      planMove(SPEED-1);
    }
  }

  public void updateWithData(Object[] data)
  {
    previousXUpdate = lastXUpdate;
    previousYUpdate = lastYUpdate;
    
    super.updateWithData(data);

    lastXUpdate = getX();
    lastYUpdate = getY();
  }

}

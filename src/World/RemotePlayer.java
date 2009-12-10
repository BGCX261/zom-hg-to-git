package World;

import View.Game.Multiplayer.Connection;

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
  
  long lastMoveTime;

  public static void registerForSync()
  {
    syncId = Connection.registerSyncTypes(syncTypes, RemotePlayer.class);
  }

  public int getSyncId()
  {
    return syncId;
  }

  public void calculateMoves(World w)
  {
    // We allow movement at the same rate as normally controlled movement (see LocalPlayer)
    if (System.currentTimeMillis() - lastMoveTime <= MOVE_DELAY) return;

    // If these aren't set yet then we don't really have enough data to extrapolate.
    if (previousXUpdate == -1 || previousYUpdate == -1) return;

    // If we moved last tick, we're probably still moving (we just continue in whatever direction we're facing).
    if (lastXUpdate - previousXUpdate != 0 || lastYUpdate - previousYUpdate != 0)
    {
      lastMoveTime = System.currentTimeMillis();

      // Minus one here because it creates a smoother effect - if it's wrong (likely) it's only a small
      // change, and a network update invisibly fixes it. This also smooths out movement when they have stopped,
      // as we're closer to where we should be.
      planMove(SPEED-1);
    }
  }

  public void loadFromData(Object[] data)
  {
    previousXUpdate = lastXUpdate;
    previousYUpdate = lastYUpdate;
    
    super.loadFromData(data);

    lastXUpdate = getX();
    lastYUpdate = getY();
  }

}

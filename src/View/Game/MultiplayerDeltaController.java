package View.Game;

import View.Game.Multiplayer.MultiplayerManager;
import World.World;

/**
 * MultiplayerDeltaController
 *
 * @author Tim Perry
 */
public class MultiplayerDeltaController implements Controller {

  private MultiplayerManager multiplayer;

  public void MultiplayerDeltaController(MultiplayerManager multiplayer)
  {
    this.multiplayer = multiplayer;
  }

  public void run(World world)
  {
    
  }

}

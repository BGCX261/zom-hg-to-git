package com.zom.view.game;

import com.zom.view.game.multiplayer.MultiplayerManager;
import com.zom.world.World;

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

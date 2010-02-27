package com.zom.view.game;

import com.zom.view.game.multiplayer.Syncable;
import com.zom.world.World;

/**
 * Controller
 *
 * @author Tim Perry
 */
public interface Controller extends Syncable
{

  // Work on the given world. The simulation boolean allows clients to run controllers
  // to keep controller data up to date, whilst the server's controller does the actual
  // controlling. An example is the zombieController, simulation mode has it just removing
  // spawn points from it's list without creating zombies, so that it knows where
  // zombies don't need to placed if it is forced to take over.
  public void run(World world, boolean simulation);

}

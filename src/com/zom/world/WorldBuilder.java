package com.zom.world;

import com.zom.view.game.multiplayer.Syncable;
import java.io.IOException;

/**
 * WorldFactory
 *
 * @author Tim Perry
 */
public interface WorldBuilder extends Syncable {

  // Server messages
  public static byte SENDING_MAP = 10;
  public static byte SENDING_MAP_NAME = 11;
  public static byte SENDING_COLLISION_MAP = 12;

  // Client messages
  public static byte RECIEVED = 20;
  public static byte DO_NOT_HAVE_MAP = 21;

  // Messages for both - Errors and end of message.
  public static byte ERROR = 30;
  public static byte EOM = 31;

  // Set parameters for this world, before building it.
  public void setDifficulty();

  // Using the various details you've be given, generate and return a world.
  public World buildWorld();

}

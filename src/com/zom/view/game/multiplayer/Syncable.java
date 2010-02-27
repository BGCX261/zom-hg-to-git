package com.zom.view.game.multiplayer;

/**
 *
 * All classes implementing this interface should provide two symmetrical functions, as below, and
 * ensure that the id that is returned by getTypeId has been registered with Connection.registerSyncTypes().
 *
 * The current convention for this is for MultiplayerManager to explicitly call a static function of the class
 * that then registers the class with the relevant sync types (See StaticWorldBuilder for a simple example)
 *
 * @author tim
 */
public interface Syncable {

  public int getSyncId();

  public Object[] getData();
  
  public void updateWithData(Object[] data);

}

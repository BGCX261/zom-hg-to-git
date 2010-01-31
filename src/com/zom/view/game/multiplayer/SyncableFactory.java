package com.zom.view.game.multiplayer;

/**
 * SyncableFactory
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public interface SyncableFactory
{
  public Syncable buildFromData(Object[] data);
}

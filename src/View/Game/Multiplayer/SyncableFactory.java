package View.Game.Multiplayer;

/**
 * SyncableFactory
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public interface SyncableFactory
{
  public Syncable buildFromData(Object[] data);
}

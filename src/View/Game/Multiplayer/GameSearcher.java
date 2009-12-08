package View.Game.Multiplayer;

import java.util.Hashtable;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

/**
 * GameSearcher
 *
 * @author Tim Perry
 */
public class GameSearcher implements DiscoveryListener {

  protected final GameSearchListener listener;
  protected DiscoveryAgent agent;
  protected LocalDevice device;

  protected final Hashtable foundGames;

  // Set of UUIDs for the services we want to detect.
  protected static final UUID[] uuidSet = new UUID[] { MultiplayerManager.UUID };
  protected static final int[] attrSet = { 0x100, 0x101 };

  public GameSearcher(GameSearchListener listener)
  {
    this.listener = listener;
    foundGames = new Hashtable();
  }

  public void search()
  {
    try 
    {
      device = LocalDevice.getLocalDevice();
      agent = device.getDiscoveryAgent();
      agent.startInquiry(DiscoveryAgent.GIAC, this);
    }
    catch (Exception e) {
    }
  }

  // If we find a device, we try and find a service running on that device with our game on it.
  public void deviceDiscovered(RemoteDevice btDevice, DeviceClass devClass)
  {
    try {
      agent.searchServices(attrSet, uuidSet, btDevice, this);
    }
    catch (Exception e) { }
  }

  // We've found a service. Find out if it's a ZoM server, and if it's a game we haven't already seen.
  // If so, add it to the game list, and inform the boss.
  public void servicesDiscovered(int transID, ServiceRecord[] records)
  {
    String serviceName;
    String connectionString;
    String serviceDescription;
    String gameName;
    int gameId;

    // Go through each service we've found.
    for (int ii = 0; ii < records.length; ii++)
    {
      connectionString = "";
      try
      {
        serviceName = (String) records[ii].getAttributeValue(0x100).getValue();
      }
      catch (Exception e) { serviceName = ""; }
      try
      {
        serviceDescription = (String) records[ii].getAttributeValue(0x101).getValue();

        // Split the description on # and store the first part as the name, and the second as the id - e.g. Game#101 becomes name = Game, id = (int) 101.
        int splitPoint = serviceDescription.indexOf(GameServer.DESCRIPTION_DELIM);
        gameName = serviceDescription.substring(0, splitPoint);
        gameId = Integer.parseInt(serviceDescription.substring(splitPoint+1));
      }
      catch (Exception e) 
      {
        gameName = "";
        gameId = 0;
        serviceDescription = "";
      }

      // If we found a name for this service, is it a ZoM game?
      if (serviceName != null && serviceName.equals("ZoM") && gameId != 0)
      {
        // Remember the connection string for this service, so we can find it later.
        connectionString = records[ii].getConnectionURL(0,false);

        // Update our connection string with this, and remember what it was before - TODO only update this if we're sure that's correct (is it the server for a previously found game?)
        Object prevValue = foundGames.put(new Integer(gameId), connectionString);

        // If we haven't seen them before then we also have to inform the client.
        if (prevValue == null) listener.gameFound(gameName, gameId);
      }
    }
  }

  // We don't care about this - ignore.
  public void serviceSearchCompleted(int transID, int respCode) { }

  public void inquiryCompleted(int discType)
  {
    listener.searchDone();
  }

  // Returns the string required to connect to the game specified by the given GameConfig.
  public String getConnectionString(int selectedGameId)
  {
    return (String) foundGames.get(new Integer(selectedGameId));
  }

}

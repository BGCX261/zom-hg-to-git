package com.zom.view.game.multiplayer.server;

import com.zom.view.game.GameConfig;
import com.zom.view.game.multiplayer.MultiplayerManager;
import java.io.IOException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;

class GameProvider implements Runnable
{
  GameServer mainServer;

  private StreamConnectionNotifier connectionNotifier;
  private final String connectionString;
  private LocalDevice device;

  private GameConfig gameConfig;

  // Are we serving, right now?
  private boolean active;

  public GameProvider(GameServer mainServer, GameConfig gameConfig)
  {
    // Build a service URL.
    connectionString = "btspp://localhost:"+MultiplayerManager.UUID.toString();

    // Get the bluetooth device itself prepped.
    try
    {
      device = LocalDevice.getLocalDevice();
    }
    catch (Exception e) { }

    active = false;
    this.gameConfig = gameConfig;
    this.mainServer = mainServer;
  }

  public void start()
  {
    new Thread(this).start();
  }

  public void stop()
  {
    active = false;
  }

  public void run()
  {
    active = true;

    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

    try
    {
      setupServing();

      // While we're serving and we have spaces, keep offering connections.
      while (active && mainServer.getPlayerCount() < gameConfig.getMaxPlayers())
      {
        // If we have a free slot, wait for another connection, and start it in a new thread.
        byte nextSlot = mainServer.getNextFreeSlot();

        if (nextSlot != -1)
        {
          // This blocks until a new player appears, ready to rock.
          mainServer.addPlayer(nextSlot, connectionNotifier.acceptAndOpen());
        }
        // If the slots are now full, wait for somebody to tell us one of them is empty.
        while (mainServer.getPlayerCount() >= gameConfig.getMaxPlayers())
        {
          System.out.println("Server is full!");

          synchronized (this)
          {
            wait();
          }
        }
      }
    }
    catch (Exception ex)
    {
      System.out.println("Uh oh, main provider thread crashed. SHOULD NOT HAPPEN.");
      ex.printStackTrace();
    }
  }

  public void setupServing() throws IOException
  {
    connectionNotifier = (StreamConnectionNotifier) Connector.open(connectionString);

    // Fill out our service record, so people can see who we are and what game this is.
    DataElement name = new DataElement(DataElement.STRING, "ZoM");
    DataElement description = new DataElement(DataElement.STRING,
                                 mainServer.game.getGameConfig().getGameName() +
                                 GameServer.DESCRIPTION_DELIM +
                                 mainServer.game.getGameConfig().getGameId());

    ServiceRecord ourServiceRecord = device.getRecord(connectionNotifier);
    ourServiceRecord.setAttributeValue(256, name);
    ourServiceRecord.setAttributeValue(257, description);

    // Show us to the world.
    device.setDiscoverable(DiscoveryAgent.GIAC);
  }

}

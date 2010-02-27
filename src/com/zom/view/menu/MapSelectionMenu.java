package com.zom.view.menu;

import com.zom.main.MIDlet;
import com.zom.view.View;
import com.zom.view.game.FileBasedWorldBuilder;
import com.zom.view.game.GameConfig;
import com.zom.view.game.RandomWorldBuilder;
import com.zom.view.game.WorldBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 * MapSelectionMenu
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class MapSelectionMenu extends Menu implements CommandListener, View {

  private static final String RANDOM_MAP_STRING = "Randomly Generated Map";
  private GameConfig gameConfig;
  private Display display;

  public MapSelectionMenu(MIDlet midlet, GameConfig gameconfig)
  {
    super(midlet, "Select a Map:", "Select");

    this.gameConfig = gameconfig;

    // Search for maps, and add them to our list. Maps CANNOT be given the same name as our RANDOM_MAP_STRING.
    for (Enumeration maps = getMapsList(); maps.hasMoreElements();)
    {
      String mapName = (String) maps.nextElement();

      if (mapName.equals(RANDOM_MAP_STRING)) throw new Error("Map name conflicts with static RANDOM_MAP_STRING!");
      
      append(mapName, null);
    }

    append(RANDOM_MAP_STRING, null);
  }

  public Enumeration getMapsList()
  {
    Vector maps = new Vector();
    try
    {
      InputStream is = System.class.getResourceAsStream("/maps/mapList");
      if (is == null) throw new IOException("Map listing file not found");
      
      InputStreamReader stream = new InputStreamReader(is);

      final int CHAR_ARRAY_LENGTH = 64;
      char[] charArray = new char[CHAR_ARRAY_LENGTH];

      int result = stream.read();
      for (int ii = 0; result != -1; ii++, result = stream.read())
      {
        if (ii >= CHAR_ARRAY_LENGTH) throw new Error("Map name too long!");

        charArray[ii] = (char) result;
        if (charArray[ii] == '\n')
        {
          maps.addElement(new String(charArray, 0, ii));
          ii = -1;
        }
      }
    }
    catch (IOException e)
    {
      System.out.println("Exception reading map list:");
      e.printStackTrace();
    }
    return maps.elements();
  }

  public void commandAction(Command c, Displayable d)
  {
    // We should always always be called from a list, since we are one - no exceptions (N.B. not a pun).
    List l = (List) d;
    
    if (c == select) {
      WorldBuilder currentWB = gameConfig.getWorldBuilder();

      // If they want to make a random map, and they're not already doing that, we build them a worldbuilder and set it up.
      if (getString(getSelectedIndex()).equals(RANDOM_MAP_STRING))
      {
        if (currentWB == null || !(currentWB instanceof RandomWorldBuilder))
        {
          gameConfig.setWorldBuilder(new RandomWorldBuilder());
        }
      }
      // If they want to load a map we either create a worldbuilder that will load that map, or change the current worldbuilder so that it will
      // load that map
      else
      {
        if (currentWB == null || !(currentWB instanceof FileBasedWorldBuilder))
        {
          gameConfig.setWorldBuilder(new FileBasedWorldBuilder(getString(getSelectedIndex())));
        }
        else if (currentWB instanceof FileBasedWorldBuilder)
        {
          ((FileBasedWorldBuilder) currentWB).setMap(getString(getSelectedIndex()));
        }
        
        if (!currentWB.ready())
        {
          display.setCurrent(new Alert("Couldn't load map.", "Sorry, but we couldn't load '"+getString(getSelectedIndex())+"', please try another.",
                                         null, AlertType.ERROR), this);
          gameConfig.setWorldBuilder(null);
          return;
        }
      }

      midlet.popMenu();
    }
    else super.commandAction(c, d);
  }

  public void giveDisplay(Display display)
  {
    this.display = display;
    display.setCurrent(this);
  }

}

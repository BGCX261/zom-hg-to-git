package com.zom.view.menu;

import com.zom.main.MIDlet;
import java.io.IOException;
import javax.microedition.lcdui.*;
import com.zom.view.game.*;
/**
 * NewGameMenu
 *
 * @author Tim Perry
 */
public class NewGameMenu extends Menu implements CommandListener {

  public final static int START = 0;
  public final static int OPTIONS = 1;
  public final static int BACK = 2;

  private static final String DEFAULT_MAP = "map1";

  private Display display;

  private GameConfig gameConfig;

  public NewGameMenu(MIDlet midlet)
  {
    super(midlet, "Create New Game", "Select");

    gameConfig = new GameConfig();
    gameConfig.setMaxPlayers(4);

    gameConfig.setWorldBuilder(new FileBasedWorldBuilder(DEFAULT_MAP));
    gameConfig.setDifficulty((byte) 1);
    
    if (!gameConfig.getWorldBuilder().ready())
    {
      System.out.println("Couldn't build default worldbuilder (Using DEFAULT_MAP: "+DEFAULT_MAP+")");
    }

    // Fill our menu with crunchy wonderful options
    append("Start Game", null);
    append("Options", null);
    append("Back", null);
  }

  public void commandAction(Command c, Displayable d)
  {
    // We should always always be called from a list, since we are one - no exceptions (N.B. not a pun).
    List l = (List) d;
    if (c == select) {
      switch (l.getSelectedIndex()) {
        case START:
          // Build a game and give it our settings.
          try
          {
            Game g = new Game(gameConfig);
            midlet.showGame(g);
          }
          catch (InstantiationException e)
          {
            display.setCurrent(new Alert("Couldn't start game.",
                                        "Sorry, but we failed to start this game with the given settings. The error was: "+e.getMessage(),
                                         null, AlertType.ERROR), this);
          }
          break;
        case OPTIONS:
          midlet.pushMenu(new NewGameOptionsMenu(midlet, gameConfig));
          break;
        case BACK:
          // Go back to the main menu
          midlet.popMenu();
          break;
        default:
          System.out.println("Imaginary main menu option selected");
          break;
      }
    }
    else super.commandAction(c, d);
  }

  public void giveDisplay(Display display)
  {
    this.display = display;
    display.setCurrent(this);
  }

}

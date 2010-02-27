package com.zom.view.menu;

import com.zom.main.MIDlet;
import com.zom.view.View;
import com.zom.view.game.DifficultyMenu;
import com.zom.view.game.GameConfig;
import javax.microedition.lcdui.*;

/**
 * NewGameOptionsMenu
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class NewGameOptionsMenu extends Menu implements CommandListener, View {

  private static final int CHANGE_GAME_NAME = 0;
  private static final int CHANGE_MAP = 1;
  private static final int CHANGE_DIFFICULTY = 2;
  private static final int TOGGLE_MULTIPLAYER = 3;
  private static final int BACK = 4;

  private GameConfig gameConfig;

  public NewGameOptionsMenu(MIDlet midlet, GameConfig gameConfig)
  {
    super(midlet, "New Game Options", "Change");

    this.gameConfig = gameConfig;

    append("Change Game Name", null);
    append("Choose Map", null);
    append("Set Difficulty", null);
    if (gameConfig.getMaxPlayers() == 1) append("Turn Multiplayer On", null);
    else append("Turn Multiplayer Off", null);
    append("Back", null);
  }

  public void commandAction(Command c, Displayable d)
  {
    // We should always always be called from a list, since we are one - no exceptions (N.B. not a pun).
    List l = (List) d;
    if (c == select) {
      switch (l.getSelectedIndex()) {
        case CHANGE_GAME_NAME:
          midlet.pushMenu(new NewGameNamer(midlet, gameConfig));
          break;
        case CHANGE_MAP:
          midlet.pushMenu(new MapSelectionMenu(midlet, gameConfig));
          break;
        case CHANGE_DIFFICULTY:
          midlet.pushMenu(new DifficultyMenu(midlet, gameConfig));
          break;
        case TOGGLE_MULTIPLAYER:
          if (gameConfig.getMaxPlayers() == 1)
          {
            gameConfig.setMaxPlayers(4);
            set(TOGGLE_MULTIPLAYER, "Turn Multiplayer Off", null);
          }
          else
          {
            gameConfig.setMaxPlayers(1);
            set(TOGGLE_MULTIPLAYER, "Turn Multiplayer On", null);
          }
          break;
        case BACK:
          midlet.popMenu();
          break;
        default:
          System.out.println("Imaginary main menu option selected");
          break;
      }
    }
    else super.commandAction(c, d);
  }

  public void giveDisplay(Display d)
  {
    d.setCurrent(this);
  }

}

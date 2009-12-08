package View.Menu;

import Main.MIDlet;
import javax.microedition.lcdui.*;
import View.Game.*;
import World.StaticWorldBuilder;
/**
 * NewGameMenu
 *
 * @author Tim Perry
 */
public class NewGameMenu extends Menu implements CommandListener {

  public final static int START = 0;
  public final static int OPTIONS = 1;
  public final static int BACK = 2;

  public NewGameMenu(MIDlet midlet)
  {
    super(midlet, "Create New Game", "Select");

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
          GameConfig config = new GameConfig();
          config.setWorldBuilder(new StaticWorldBuilder());
          config.setMaxPlayers(4);
          Game g = new Game(config);
          midlet.showGame(g);
          // Put it on the screen.
          break;
        case OPTIONS:
          // TODO - GAME OPTIONS
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

  public void giveDisplay(Display d)
  {
    d.setCurrent(this);
  }

}

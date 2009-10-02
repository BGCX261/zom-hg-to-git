package View.Menu;

import Main.MIDlet;
import javax.microedition.lcdui.*;
import View.Game.*;
import World.StaticWorldBuilder;
import World.World;
import World.WorldBuilder;

/**
 * NewGameMenu
 *
 * @author Tim Perry
 */
public class NewGameMenu extends Menu implements CommandListener {

    private Command select;

    private MIDlet midlet;

    public final static int START = 0;
    public final static int OPTIONS = 1;
    public final static int BACK = 2;

    public NewGameMenu(MIDlet midlet)
    {
      this.midlet = midlet;

      // Fill our menu with crunchy wonderful options
      append("Start Game", null);
      append("Options", null);
      append("Back", null);

      // Add controls to select those items
      select = new Command("Select",Command.SCREEN, 1);
      addCommand(select);
      setSelectCommand(select);
      setCommandListener(this);
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
            config.setMaxPlayers(1);
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
    }

  public void giveDisplay(Display d)
  {
    d.setCurrent(this);
  }

}

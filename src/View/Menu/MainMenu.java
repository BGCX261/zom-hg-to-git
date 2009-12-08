package View.Menu;

import Main.MIDlet;
import javax.microedition.lcdui.*;

/**
 * MainMenu
 *
 * @author Tim Perry
 */
public class MainMenu extends Menu implements CommandListener {

    public final static int NEW_GAME = 0;
    public final static int JOIN_GAME = 1;
    public final static int SETTINGS = 2;
    public final static int HELP = 3;
    public final static int EXIT = 4;

    public MainMenu(MIDlet m)
    {
      super(m, "ZoM", "Select");

      // Fill our menu with crunchy wonderful options
      append("New Game", null);
      append("Join Game", null);
      append("Settings", null);
      append("Help", null);
      append("Exit", null);     

      // You can't go back from the main menu.
      removeCommand(back);
    }

    public void commandAction(Command c, Displayable d)
    {
      // We should always always be called from a list, since we are one - no exceptions (N.B. not a pun).
      List l = (List) d;
      if (c == select) {
        switch (l.getSelectedIndex()) {
          case NEW_GAME:
            midlet.pushMenu(new NewGameMenu(midlet));
            break;
          case JOIN_GAME:
            midlet.pushMenu(new JoinGameMenu(midlet));
            break;
          case SETTINGS:
            midlet.pushMenu(new SettingsMenu(midlet));
            break;
          case HELP:
            midlet.pushMenu(new NewGameMenu(midlet));
            break;
          case EXIT:
            midlet.exit();
            break;
          default:
            System.out.println("Imaginary main menu option selected");
            midlet.exit();
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

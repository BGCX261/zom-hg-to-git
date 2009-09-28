package Main;

import View.Menu.MainMenu;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Main
 *
 * @author Tim Perry
 */
public class MIDlet extends javax.microedition.midlet.MIDlet  {

  protected void startApp() throws MIDletStateChangeException {
    Display display = Display.getDisplay(this);
    MainMenu menu = new MainMenu(this);
    menu.giveDisplay(display);
  }

  protected void pauseApp() {
  }

  protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
  }

  public void exit() {
    try {
      destroyApp(false);
      notifyDestroyed();
    } catch (Exception e) { }
  }

}

package Main;

import View.Game.Game;
import View.Game.GameConfig;
import View.Menu.*;
import View.View;
import java.util.Stack;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Screen;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Main
 *
 * @author Tim Perry
 */
public class MIDlet extends javax.microedition.midlet.MIDlet  {

  private Stack menuStack = new Stack();
  private Game game;
  private Display display;

  protected void startApp() throws MIDletStateChangeException {
    display = Display.getDisplay(this);
    resetMenu();
    showMenu();
  }

  protected void pauseApp() {
  }

  protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
  }

  public void resetMenu()
  {
    menuStack.empty();
    menuStack.push(new MainMenu(this));
  }
  
  public void pushMenu(Screen m)
  {
    menuStack.push(m);
    showMenu();
  }

  public void popMenu() {
    // Don't empty the stack, or we won't have anything to show.
    if (menuStack.size() > 1) menuStack.pop();
    showMenu();
  }

  public void showMenu() {
    ((View)menuStack.peek()).giveDisplay(display);
  }

  public void showGame(Game g) {
    resetMenu();
    game = g;
    game.giveDisplay(display);
  }

  public void exit() {
    try {
      destroyApp(false);
      notifyDestroyed();
    } catch (Exception e) { }
  }

}

package Main;

import View.Game.Game;
import View.Menu.*;
import java.util.Stack;
import javax.microedition.lcdui.Display;
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
  
  public void pushMenu(Menu m)
  {
    menuStack.push(m);
    showMenu();
  }

  public void popMenu() {
    menuStack.pop();
    showMenu();
  }

  public void showMenu() {
    ((Menu)menuStack.peek()).giveDisplay(display);
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

package View.Menu;

import View.*;
import javax.microedition.lcdui.List;

/**
 * Menu
 *
 * @author Tim Perry
 */
public abstract class Menu extends List implements View {

  protected static final String menuTitle = "";

  public Menu() {
    super(menuTitle, IMPLICIT);
  }

}

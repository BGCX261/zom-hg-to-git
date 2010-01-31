package com.zom.view.menu;

import com.zom.main.MIDlet;
import com.zom.view.*;
import javax.microedition.lcdui.*;
/**
 * Menu
 *
 * @author Tim Perry
 */
public abstract class Menu extends List implements View, CommandListener {

  protected final Command back;
  protected final Command select;
  protected final MIDlet midlet;

  public Menu(MIDlet midlet, String title, String selectLabel) {
    super(title, IMPLICIT);

    this.midlet = midlet;

    back = new Command("Back", Command.BACK, 2);
    addCommand(back);

    // Add controls to select items from this menu.
    select = new Command(selectLabel,Command.SCREEN, 1);
    addCommand(select);
    setSelectCommand(select);

    setCommandListener(this);
  }

  // Deal with whatever basic commands we decree that all menus should have
  public void commandAction(Command c, Displayable d)
  {
    if (c == back) {
      midlet.popMenu();
    }
  }

}

package com.zom.view.game;

import com.zom.main.MIDlet;
import com.zom.view.View;
import com.zom.view.menu.Menu;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 * DifficultyMenu
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class DifficultyMenu extends Menu implements CommandListener, View {

  private GameConfig gameConfig;

  public DifficultyMenu(MIDlet midlet, GameConfig gameConfig)
  {
    super(midlet, "Set the difficulty level", "Select");

    append("Easy", null);
    append("Medium", null);
    append("Hard", null);

    this.gameConfig = gameConfig;
  }

  public void commandAction(Command c, Displayable d)
  {
     // We should always always be called from a list, since we are one - no exceptions (N.B. not a pun).
    List l = (List) d;
    if (c == select) {
      gameConfig.setDifficulty((byte) l.getSelectedIndex());
      midlet.popMenu();
    }
    else super.commandAction(c, d);
  }

  public void giveDisplay(Display d)
  {
    d.setCurrent(this);
  }

}

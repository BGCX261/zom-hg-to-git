package com.zom.view.menu;

import com.zom.main.MIDlet;
import com.zom.view.View;
import com.zom.view.game.GameConfig;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * NewGameNamer
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class NewGameNamer extends Form implements CommandListener, View
{
  private Command save;
  private Command cancel;
  private MIDlet midlet;

  private GameConfig gameConfig;

  private TextField nameInput;

  public NewGameNamer(MIDlet midlet, GameConfig gameConfig)
  {
    super("Choose Game Name");

    this.midlet = midlet;
    this.gameConfig = gameConfig;

    nameInput = new TextField("", gameConfig.getGameName(), 128, TextField.ANY);

    this.append(nameInput);

    // Add controls to select those items
    save = new Command("Save", Command.OK, 1);
    cancel = new Command("Cancel", Command.CANCEL, 1);

    addCommand(save);
    addCommand(cancel);

    setCommandListener(this);
  }

  public void commandAction(Command c, Displayable d)
  {
    if (c == save)
    {
      gameConfig.setGameName(nameInput.getString());
      midlet.popMenu();
    }
    else if (c == cancel)
    {
      midlet.popMenu();
    }
  }

  public void giveDisplay(Display d)
  {
    d.setCurrent(this);
  }
}


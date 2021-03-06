package com.zom.view.menu;

import com.zom.main.MIDlet;
import com.zom.view.View;
import com.zom.world.LocalPlayer;
import javax.microedition.lcdui.*;

/**
 * MainMenu
 *
 * @author Tim Perry
 */
public class SettingsMenu extends Form implements CommandListener, View
{
  private final int ABSOLUTE_CONTROLS = 0;
  private final int RELATIVE_CONTROLS = 1;

  private Command save;
  private Command cancel;
  private MIDlet midlet;
  private ChoiceGroup controls;
  private Config config;


  public SettingsMenu(MIDlet m)
  {
    super("Settings");

    midlet = m;

    config = Config.getInstance();

    // Fill our menu with crunchy wonderful options
    controls = new ChoiceGroup("Select control scheme:", Choice.EXCLUSIVE);

    controls.append("Absolute", null);
    controls.append("Relative", null);

    if (config.getControlScheme() == LocalPlayer.ABSOLUTE_CONTROLS) controls.setSelectedIndex(ABSOLUTE_CONTROLS, true);
    if (config.getControlScheme() == LocalPlayer.RELATIVE_CONTROLS) controls.setSelectedIndex(RELATIVE_CONTROLS, true);

    this.append(controls);

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
      switch (controls.getSelectedIndex())
      {
        case ABSOLUTE_CONTROLS:
          config.setControlScheme(LocalPlayer.ABSOLUTE_CONTROLS);
          break;
        case RELATIVE_CONTROLS:
          config.setControlScheme(LocalPlayer.RELATIVE_CONTROLS);
          break;
        default:
          break;
      }
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

package View.Menu;

import Main.MIDlet;
import View.Game.*;
import View.Game.Multiplayer.*;
import View.View;
import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * JoinGameMenu
 *
 * @author Tim Perry
 */
class JoinGameMenu extends Menu implements View, GameSearchListener, CommandListener {

  private Display display;
  private Vector gameList = new Vector();
  private GameSearcher searcher;

  // REPORT - Could talk about this as my first impact with concurrent bugs?
  private boolean joining = false;

  public JoinGameMenu(MIDlet midlet)
  {
    super(midlet, "Searching for games...", "Join");

    searcher = new GameSearcher(this);
    searcher.search();
  }

  public void giveDisplay(Display d)
  {
    this.display = d;
    d.setCurrent(this);
  }

  public void gameFound(String gameName, int gameId)
  {
    append(gameName, null);
    gameList.addElement(new Integer(gameId));
    if (this.size() == 1) setTitle(this.size() + " Game Found");
    else setTitle(this.size() + " Games Found");
  }

  // Might be worth looking at this at some point, but on the emulator at least it doesn't appear to actually only turn up on the end
  // of the search - we keep getting more results afterwards - so ignoring it for now.
  public void searchDone() { }

  // If they press a button, do things. Join button joins the selected game, back button goes back.
  public void commandAction(Command c, Displayable d)
  {
    if (c == select) {
      // If we're already in the process of joining one server, don't try and join another.
      if (joining == true) return;
      List l = (List) d;
      if (l.getSelectedIndex() == -1) return;

      // Work out what which game we have selected, and ask the searcher what the connection string for that game is.
      int selectedGameId = ((Integer) gameList.elementAt(l.getSelectedIndex())).intValue();
      String connectionString = searcher.getConnectionString(selectedGameId);      

      // Go and try to get the game from the server. This calls us back with a response, to avoid locking the ui.
      joining = true;
      MultiplayerManager.joinGame(connectionString, this);
    }
    else super.commandAction(c, d);
  }

  // Call back if we successfully join a game - if we do, we put it up on the screen.
  public void joinSucceeded(Game g)
  {
    joining = false;
    midlet.showGame(g);
  }

  // Call back if we fail to join a game - throw up an error message, and then come back to here.
  public void joinFailed()
  {
    joining = false;
    display.setCurrent(new Alert("Couldn't connect.", "Failed to join the selected game.", null, AlertType.ERROR), this);
  }

}

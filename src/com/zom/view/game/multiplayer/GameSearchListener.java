package com.zom.view.game.multiplayer;

import com.zom.view.game.Game;

/**
 *
 * @author tim
 */
public interface GameSearchListener {

  public void gameFound(String gameName, int gameId);

  public void searchDone();

  public void joinSucceeded(Game g);

  public void joinFailed();

}

package com.zom.view.game;

import com.zom.view.game.multiplayer.MultiplayerManager;
import com.zom.view.View;
import com.zom.world.*;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * Game
 *
 * @author Tim Perry
 */
public class Game extends GameCanvas implements View {

  private final World world;
  private final GameConfig config;
  private final LocalPlayer localPlayer;

  private MultiplayerManager multiplayerManager;
  
  private final Vector controllers;

  private int cameraX;
  private int cameraY;

  public static final int TICK_LENGTH = 35;

  private boolean running;
  private Graphics g;

  // Build ourselves from the spec given.
  public Game(GameConfig config) throws InstantiationException
  {
    // We run the constructor for GameCanvas with key events suppressed - no keyPressed etc calls will be made.
    super(true);
    
    running = false;

    // Store this config
    this.config = config;

    // Build our player for this game.
    LocalPlayer.createLocalPlayer(config.getPlayerId());
    localPlayer = LocalPlayer.getLocalPlayer();
    localPlayer.setControlScheme(config.getControlScheme());

    // Let there be light (build us a world from our config - can take time)
    world = config.getWorldBuilder().buildWorld();
    controllers = config.getWorldBuilder().buildControllers();

    // Add the local player to the world.
    LocalPlayer.getLocalPlayer().setX(world.getMap().getPlayerStartX(config.getPlayerId()));
    LocalPlayer.getLocalPlayer().setY(world.getMap().getPlayerStartY(config.getPlayerId()));

    world.addThing(LocalPlayer.getLocalPlayer());

    // Set up multiplayer, if required.
    if (config.getMaxPlayers() > 1)
    {
      multiplayerManager = new MultiplayerManager(this, config);
    }

    g = getGraphics();
  }

  // Most of the time you want to use the accessor functions like getMaxPlayers, but sometimes you want more detail. This is for that.
  public GameConfig getGameConfig()
  {
    return config;
  }

  // As soon as the game is given a display to play with, it sets up, and then spawns another thread to actually run the game.
  public void giveDisplay(Display d)
  {
    System.out.println("Game has display");
    d.setCurrent(this);
    setFullScreenMode(true);
    world.getMap().prepBackgroundForScreen(getWidth(), getHeight());
    
    GameLoop gameLoop = new GameLoop();
    gameLoop.prepare();
    new Timer().scheduleAtFixedRate(gameLoop, 0, TICK_LENGTH);
  }  

  public int getCameraX()
  {
    return cameraX;
  }

  public int getCameraY()
  {
    return cameraY;
  }

  public void updateCameraCoords()
  {
    cameraX = Math.min(Math.max(0, localPlayer.getX() - getWidth()/2), world.getMap().getWidth() - this.getWidth());
    cameraY = Math.min(Math.max(0, localPlayer.getY() - getHeight()/2), world.getMap().getHeight() - this.getHeight());
  }

  public World getWorld()
  {
    return world;
  }

  private class GameLoop extends TimerTask
  {
    public void prepare()
    {
      running = true;

      if (multiplayerManager != null) multiplayerManager.start();
    }

    public void run()
    {
      //long startTime = System.currentTimeMillis();
      if (!running) cancel();

      // If we're controlling, run our controllers.
      for (int ii = 0; ii < controllers.size(); ii++)
      {
        // If we're not running everything (isMaster) then we run in simulation mode.
        ((Controller) controllers.elementAt(ii)).run(getWorld(), !isMaster());
      }
      
      // Look at player input, control the local player
      localPlayer.setKeys(getKeyStates());

      // Update the world (players + AI)
      world.tick();

      updateCameraCoords();

      // Draw everything out.
      draw();
      //System.out.println("Tick time: "+(System.currentTimeMillis() - startTime));
    }

  }

  public boolean isMaster()
  {
    return config.getConnectionToServer() == null;
  }

  // Draw the view from the camera's coordinates to the screen.
  protected void draw()
  {
    // Reset the screen
    g.setColor(255,255,255);
    g.fillRect(0, 0, getWidth(), getHeight());

    g.translate(-getCameraX() - g.getTranslateX(), -getCameraY() - g.getTranslateY());

    // Ask the map to draw the relevant background.
    world.getMap().drawBackground(g, getCameraX(), getCameraY(), getWidth(), getHeight());

    // TODO - would it be quicker to do this ourselves?
    getWorld().lockForRead();

    // Iterate through every element of the world and draw it.
    for (Enumeration things = world.getThings(); things.hasMoreElements();)
    {
      ((Thing) things.nextElement()).draw(g);
    }

    getWorld().unlock();

    // Draw this to the screen.
    flushGraphics();
  }

}

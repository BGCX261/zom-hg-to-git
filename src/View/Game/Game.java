package View.Game;

import View.Game.Multiplayer.MultiplayerManager;
import View.View;
import World.Player;
import World.Thing;
import World.World;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * Game
 *
 * @author Tim Perry
 */
public class Game extends GameCanvas implements View, Runnable {

  private final World world;
  private final GameConfig config;
  private final Player localPlayer;

  private MultiplayerManager multiplayerManager;
  
  private final Vector controllers;

  private int cameraX;
  private int cameraY;

  private boolean running;
  private Graphics g;

  // Build ourselves from the spec given.
  public Game(GameConfig config)
  {
    // We run the constructor for GameCanvas with key events suppressed - no keyPressed etc calls will be made.
    super(true);

    // Store this config
    this.config = config;

    // Build our player for this game.
    System.out.println("Config pId = "+config.getPlayerId());
    Player.createLocalPlayer(config.getPlayerId());
    localPlayer = Player.getLocalPlayer();
    localPlayer.setControlScheme(config.getControlScheme());

    // Let there be light (build us a world from our config - can take time)
    world = config.getWorldBuilder().buildWorld();

    // Set up multiplayer, if required.
    if (config.getMaxPlayers() > 1)
    {
      multiplayerManager = new MultiplayerManager(this, config);
    }

    controllers = new Vector();
    g = getGraphics();
    running = false;
  }

  // Most of the time you want to use the accessor functions like getMaxPlayers, but sometimes you want more detail. This is for that.
  public GameConfig getGameConfig()
  {
    return config;
  }

  public void giveDisplay(Display d)
  {
    System.out.println("Game has display");
    d.setCurrent(this);
    world.getMap().prepBackgroundForScreen(getWidth(), getHeight());
    new Thread(this).start();
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

  public void run()
  {
    running = true;
    
    multiplayerManager.start();


    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    // Game loop.
    while (running)
    {
      // Look at player input, control the local player
      localPlayer.setKeys(getKeyStates());

      // Update the world (players + AI)
      world.tick();

      // TODO - Run every controller.
      for (int ii = 0; ii < controllers.size(); ii++)
      {
        Controller c = (Controller) controllers.elementAt(ii);
        c.run(world);
      }

      updateCameraCoords();

      // Draw everything out.
      draw();

      try { Thread.sleep(1); } catch (Exception e) { System.out.println("Sleep failed"); }
    }
  }

  // TODO - Checksum everything in the game into one long. Might be challenging...
  public long checksum()
  {
    return 0;
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
    Vector things = getWorld().getThings();
    for (int ii = 0; ii < things.size(); ii++)
    {
      ((Thing)things.elementAt(ii)).draw(g);
    }

    getWorld().unlock();

    // Draw this to the screen.
    flushGraphics();
  }

}

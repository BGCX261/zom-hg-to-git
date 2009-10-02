package View.Game;

import View.View;
import World.Map;
import World.Player;
import World.Thing;
import World.World;
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
  private final Player[] players;
  private final int maxPlayers;
  private final String gameName;
  
  private final Vector controllers;

  private int x;
  private int y;

  private boolean running;
  private Graphics g;

  // Build ourselves from the spec given.
  public Game(GameConfig config)
  {
    // We run the constructor for GameCanvas with key events suppressed - no keyPressed etc calls will be made.
    super(true);

    // Get our setup from the config
    world = config.getWorldBuilder().buildWorld();
    maxPlayers = config.getMaxPlayers();
    gameName = config.getGameName();

    // Set up the local player in the world
    Map map = world.getMap();
    players = new Player[maxPlayers];
    players[0] = Player.getLocalPlayer();

    players[0].setX(map.getPlayerStartX());
    players[0].setY(map.getPlayerStartY());

    world.addThing(players[0]);

    controllers = new Vector();
    g = getGraphics();
    running = false;
  }

  public void giveDisplay(Display d)
  {
    d.setCurrent(this);    
  }

  public void showNotify()
  {
    new Thread(this).start();
  }

  public void run()
  {
    running = true;

    while (running)
    {
      // TODO - Manage multiplayer stuff - add new clients, drop dead ones etc
      // TODO - Get all relevant clients in sync
      // TODO - Look at player input, control the local player
      players[0].setKeys(getKeyStates());

      // Update the world (players + AI)
      world.tick();

      // TODO - Run every controller.
      for (int ii = 0; ii < controllers.size(); ii++)
      {
        Controller c = (Controller) controllers.elementAt(ii);
        c.run(world);
      }

      // Camera follows our player
      x = players[0].getX();
      y = players[0].getY();

      // Draw everything out.
      draw(x,y);
      
      try { Thread.sleep(10); } catch (Exception e) { System.out.println("wait failed"); }
    }
  }

  // Draw the view from the given coordinates to the screen.
  protected void draw(int x, int y) {
    // Reset the screen
    g.setColor(255,255,255);
    g.fillRect(0, 0, getWidth(), getHeight());

    // Get the world's background and draw it out
    Image background = world.getMap().getBackground();
    if (background != null)
    {
      g.drawImage(background, 0, 0, Graphics.TOP | Graphics.LEFT);
    }

    // TODO - would it be quicker to do this ourselves?
    Vector things = world.getThings();
    for (int ii = 0; ii < things.size(); ii++) {
      Thing t = (Thing) things.elementAt(ii);
      t.draw(g);
    }

    // Draw this to the screen.
    flushGraphics();
  }

}

package com.zom.world;

import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Map
 *
 * @author Tim Perry
 */
public class Map {

  private Image backgroundTile;
  private int tileWidth;
  private int tileHeight;

  private int width;
  private int height;

  private boolean[][] collisionMap;
  private int collisionMapScaleFactor;
  
  public Map(String mapName)
  {
    String mapFolder = "/"+mapName;
    try {
      //  TODO - Read from the map settings file
      width = 579;
      height = 336;

      // Load the map's background image
      setBackgroundTile(Image.createImage(mapFolder+"/bg.png"));
      // Load the map's collision map
      loadCollisionMap(mapFolder+"/staticObjects.collMap");
    }
    catch (IOException e) {
      System.out.println("Resources not found");
      setBackgroundTile(Image.createImage(300,500));
    }
  }

  public Map(boolean[][] collisionMap, Image backgroundTile)
  {
    setBackgroundTile(backgroundTile);
    setCollisionMap(collisionMap);
  }

  // TODO - Does a map with the given name exist? Possible extension - hash to check that it's the right map.
  public static boolean exists(String mapName)
  {
    return true;
  }

  public int getWidth()
  {
    return width;
  }

  public int getHeight()
  {
    return height;
  } 

  // Optimise the background tile for the given screen width/height - REPORT
  public void prepBackgroundForScreen(int screenWidth, int screenHeight)
  {
    // We optimise by pre-repeating the tiles, generating an image approx twice the size of the screen,
    // and filling that with the repeated tiles, so that later draws of the background image only require one drawImage, not 100s.
    if (tileWidth < screenWidth/2 || tileHeight < screenHeight/2)
    {
      int newWidth = screenWidth*2;
      int newHeight = screenHeight*2;
      // Round newWidth/newHeight up to the next multiple of tileWidth/tileHeight. This ensures that we show a whole number of tiles.
      newWidth = tileWidth * (((newWidth-1) / tileWidth)+1);
      newHeight = tileHeight * (((newHeight-1) / tileHeight)+1);

      // Build a new image with the previous tile already repeated, at a size greater than double the screen, so we never need to repeat it again.
      Image newBackground = Image.createImage(newWidth, newHeight);
      drawBackground(newBackground.getGraphics(), 0, 0, newWidth, newHeight);
      setBackgroundTile(newBackground);
    }
  }

  public void setBackgroundTile(Image i) {
    backgroundTile = i;
    tileWidth = i.getWidth();
    tileHeight = i.getHeight();
  }

  public void drawBackground(Graphics target, int x, int y, int width, int height)
  {
    for (int x2 = x/tileWidth; x2 < x+width; x2 += tileWidth)
    {
      for (int y2 = x/tileHeight; y2 < y+height; y2 += tileHeight)
      {
        target.drawImage(backgroundTile, x2, y2, Graphics.TOP|Graphics.LEFT);
      }
    }
  }

  // Reads from a file, converts it to a 2d boolean array and stores it as our collision map.
  public void loadCollisionMap(String filename) throws IOException
  {
    DataInputStream dataInput = new DataInputStream(getClass().getResourceAsStream(filename));
    if (dataInput.available() == 0) throw new IOException();

    int collWidth = dataInput.readInt();
    int collHeight = dataInput.readInt();
    boolean[][] loadedMap = new boolean[collWidth][collHeight];

    // Load all the collision data into that array.
    for (int x = 0; x < collWidth; x++)
    {
      for (int y = 0; y < collHeight; y++)
      {
        loadedMap[x][y] = dataInput.readBoolean();
      }
    }

    setCollisionMap(loadedMap);
  }

  // Give us a collision map (2d boolean array) to use. If it's less than the size of this
  // map, repeat it. This works much much quicker if the map fits the screen properly already.
  // If you use jagged arrays, strange things may well happen! TODO - There are bugs here that need fixing!
  public void setCollisionMap(boolean[][] map)
  {
    // Shortcut for empty arrays that makes the next bit a little less worrisome.
    if (map.length == 0)
    {
      collisionMap = new boolean[width][height];
    }
    // Else, set it up and work out how we're scaling it for later.
    else
    {
      collisionMapScaleFactor = width / map.length + 1;
      collisionMap = map;
    }
  }

  // Checks whether the given coordinate is free on our map. Coords in for the center of a circle of radius radius.
  public boolean isPositionFree(int x, int y, int radius)
  {
    // Places that are not on the map are not free.
    if (x - radius <= 0 || x + radius >= width || y - radius <= 0 || y + radius >= height) return false;

    // This makes things much quicker, particulary on largely scaled maps / small objects.
    if (radius / collisionMapScaleFactor < 1) return !collisionMap[x / collisionMapScaleFactor][y / collisionMapScaleFactor];

    // TODO - Collision detection seriously needs tightening up, eventually.

    // Work out the boundaries (non-inclusive) of this object, scaled to match the collision map.
    int minX = (x - radius) / collisionMapScaleFactor;
    int maxX = (x + radius) / collisionMapScaleFactor;
    int minY = (y - radius) / collisionMapScaleFactor + 1;
    int maxY = (y + radius) / collisionMapScaleFactor + 1;

    // If anything solid is in those boundaries, we collide.
    for (int x2 = minX; x2 < maxX; x2++)
    {
      for (int y2 = minY; y2 < maxY; y2++)
      {
        if (collisionMap[x2][y2]) return false;
      }
    }

    // Nothing solid means position is free.
    return true;
  }

  public int getPlayerStartX()
  {
    return 20;
  }

  public int getPlayerStartY()
  {
    return 20;
  }

}

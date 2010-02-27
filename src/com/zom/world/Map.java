package com.zom.world;

import com.zom.util.Coord;
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

  private Coord[] playerSpawns;

  private boolean[][] collisionMap;
  private double collisionMapVerticalScaleFactor = 1;
  private double collisionMapHorizontalScaleFactor = 1;

  public Map(int width, int height, Image backgroundTile, boolean[][] collisionMap, Coord[] playerSpawns)
  {
    setSize(width, height);
    setBackgroundTile(backgroundTile);
    setCollisionMap(collisionMap);
    this.playerSpawns = playerSpawns;
  }

  public void setSize(int width, int height)
  {
    this.width = width;
    this.height = height;
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

  // Give us a collision map (2d boolean array) to use. If it's less than the size of this
  // map, repeat it. This works much much quicker if the map fits the screen properly already.
  // This assumes square arrays.
  public void setCollisionMap(boolean[][] map)
  {
    // Unless we specify otherwise, the collision map is sized to perfectly match the actual map.
    collisionMapVerticalScaleFactor = 1;
    collisionMapHorizontalScaleFactor = 1;

    // If we're given an empty collision map, we just make one that says there are no solid things anywhere.
    if (map == null || map.length == 0 || map[0].length == 0) collisionMap = new boolean[getWidth()][getHeight()];

    // If the map fits perfectly, we're done.
    else if (map.length == getWidth() && map[0].length == getHeight())
    {
      collisionMap = map;
    }
    // There is some map data, but it doesn't fit quite right.
    else
    {
      collisionMapHorizontalScaleFactor = (double) getWidth() / (double) map.length;
      collisionMapVerticalScaleFactor = (double) getHeight() / (double) map[0].length;

      collisionMap = map;
    }
  }

  // Checks whether the given coordinate is free on our map. Coords in for the center of a circle of diameter diameter.
  public boolean isPositionFree(double x, double y, int diameter)
  {
    // Places that are not on the map are never free.
    if (x - diameter/2 <= 0 || x + diameter/2 >= width || y - diameter/2 <= 0 || y + diameter/2 >= height) return false;

    // Rescale our parameters so that they match the collision map.
    x = x / collisionMapHorizontalScaleFactor;
    y = y / collisionMapVerticalScaleFactor;

    double xRadius = diameter / collisionMapHorizontalScaleFactor / 2;
    double yRadius = diameter / collisionMapVerticalScaleFactor / 2;

    // Work out the boundaries (non-inclusive) of this object, scaled to match the collision map.
    int minX = (int) Math.ceil(x - xRadius);
    int maxX = (int) Math.ceil(x + xRadius);
    int minY = (int) Math.ceil(y - yRadius);
    int maxY = (int) Math.ceil(y + yRadius);

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

  public int getPlayerStartX(byte playerId)
  {
    return playerSpawns[playerId].x;
  }

  public int getPlayerStartY(byte playerId)
  {
    return playerSpawns[playerId].y;
  }

}

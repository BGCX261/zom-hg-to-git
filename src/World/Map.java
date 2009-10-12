package World;

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
  
  public Map(String mapName)
  {
    try {
      setBackgroundTile(Image.createImage("/"+mapName+"/bg.png"));
    }
    catch (IOException e) {
      System.out.println("Image not found");
      setBackgroundTile(Image.createImage(300,500));
    }
  }

  // Optimise the background tile for the given screen width/height
  public void prepBackgroundForScreen(int width, int height)
  {
    width = 249;
    if (tileWidth < width/2 || tileHeight < height/2)
    {
      int newWidth = width*2;
      int newHeight = height*2;
      // Round newWidth/newHeight up to the next multiple of tileWidth/tileHeight. This ensures that we show a whole number of tiles.
      newWidth = tileWidth * (((newWidth-1) / tileWidth)+1);
      System.out.println("width*2 = "+width*2+"; tileWidth = "+tileWidth+"; newWidth = "+newWidth);
      newHeight = tileHeight * (((newHeight-1) / tileHeight)+1);
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
      System.out.println(x2);
      for (int y2 = x/tileHeight; y2 < y+height; y2 += tileHeight)
      {
        System.out.println(y2);
        target.drawImage(backgroundTile, x2, y2, Graphics.TOP|Graphics.LEFT);
      }
    }
  }

  public int getPlayerStartX()
  {
    return 10;
  }

  public int getPlayerStartY()
  {
    return 10;
  }

}

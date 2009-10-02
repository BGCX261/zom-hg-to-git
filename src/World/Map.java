package World;

import javax.microedition.lcdui.Image;

/**
 * Map
 *
 * @author Tim Perry
 */
public class Map {

  private Image background;
  
  public Map()
  {
    background = Image.createImage(1024,768);
  }

  // TODO - consider trimming this here, rather than later?
  public Image getBackground()
  {
    return background;
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

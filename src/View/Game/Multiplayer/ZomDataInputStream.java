package View.Game.Multiplayer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Image;

/**
 * DataInputStream
 *
 * A wrapper around DataInputStream that adds an efficient method to recieve boolean arrays
 * and images. Without this, both of those are very annoying.
 *
 * @author Tim Perry
 */
class ZomDataInputStream extends DataInputStream  {

  public ZomDataInputStream(InputStream s)
  {
    super(s);
  }
  
  public boolean[] readBoolArray() throws IOException
  {
    int length = readInt();
    return readBoolArray(length);
  }

  // When bool arrays are written, they are packed as signed bytes. This
  // unpacks that. Worth noting that we can't easily write datatypes that
  // are less than a byte, so if you're calling this with length < 8, you're
  // probably doing something wrong.
  public boolean[] readBoolArray(int length) throws IOException
  {
    boolean[] array = new boolean[length];
    byte lastRead;
    int total = 0;

    // While we have more than a byte left of bools coming, load whole bytes at a time and
    // do them nice and quick, like this.
    for (int ii = 0; ii <= length - 8; ii += 8) {
      lastRead = readByte();
      // Check each bit, and put the result in the array.
      array[ii] = ((lastRead & 0x1) != 0);
      array[ii+1] = ((lastRead & 0x2) != 0);
      array[ii+2] = ((lastRead & 0x4) != 0);
      array[ii+3] = ((lastRead & 0x8) != 0);
      array[ii+4] = ((lastRead & 0x10) != 0);
      array[ii+5] = ((lastRead & 0x20) != 0);
      array[ii+6] = ((lastRead & 0x40) != 0);
      array[ii+7] = ((lastRead & 0x80) != 0);

      // Remember the total so far - needed for the next loop
      total = ii + 8;
    }
    // There are 7 bits left that we want (max), so we read in bit by bit, and take only what we want.
    lastRead = readByte();
    for (int ii = total; ii < length; ii++)
    {
      array[ii] = ((lastRead & 0x1) != 0);
      lastRead = (byte) (lastRead >> 1);
    }
    return array;
  }

  // Reads a 2d boolean array.
  // isJagged should be false if the array is being written as int n, (int x, bool[x])*n
  // and true if the array is being written as int x, int y, bool[x][y] - (i.e. it's a
  // rectangular array)
  public boolean[][] read2dBoolArray(boolean isJagged) throws IOException
  {
    boolean[][] array;
    if (isJagged)
    {
      int width = readInt();
      array = new boolean[width][];

      for (int ii = 0; ii < width; ii++)
      {
        array[ii] = readBoolArray();
      }
    }
    else
    {
      int width = readInt();
      int height = readInt();
      array = new boolean[width][];
      for (int ii = 0; ii < width; ii++)
      {
        array[ii] = readBoolArray(height);
      }
    }
    return array;
  }

  // Read an int for width, and int for height, and then w*h ints of argb data.
  public Image readImage() throws IOException
  {
    int width = readInt();
    int height = readInt();
    int[] rgbData = new int[width*height];
    for (int ii = 0; ii < rgbData.length; ii++)
    {
      rgbData[ii] = readInt();
    }
    return Image.createRGBImage(rgbData, width, height, true);
  }

}

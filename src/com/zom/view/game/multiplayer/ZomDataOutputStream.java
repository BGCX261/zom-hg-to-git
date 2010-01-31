package com.zom.view.game.multiplayer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.lcdui.Image;

/**
 * DataOutputStream
 *
 * Adds boolean array (1d and 2d) writing functions to DataOutputStream, improving the
 * performance on boolean arrays by up to 8x, and an image writing function for convenience.
 *
 * @author Tim Perry
 */
class ZomDataOutputStream extends DataOutputStream {

  public ZomDataOutputStream(OutputStream s)
  {
    super(s);
  }

  // Writes a boolean array bs to the stream. Array is packed into bytes.
  // If writeLength is true the length of the array is sent first (as an int)
  public void writeBoolArray(boolean[] bs, boolean writeLength) throws IOException
  {
    if (writeLength) writeInt(bs.length);

    int total = 0;

    // We could use int here, and eliminate a lot of casts, but if we do that then
    // working out what happens with the highest bit when we cast from int to byte,
    // to write everything out, is quite complicated.
    byte b;

    // Write out byte by byte, while we have at least a byte's worth left.
    for (int ii = 0; ii <= bs.length - 8; ii += 8)
    {
      b = 0;

      // Loop unrolled so we don't have to keep doing bitshifting. This is quicker and nicer!
      if (bs[ii]) b = (byte) (b | 0x1);
      if (bs[ii+1]) b = (byte) (b | 0x2);
      if (bs[ii+2]) b = (byte) (b | 0x4);
      if (bs[ii+3]) b = (byte) (b | 0x8);
      if (bs[ii+4]) b = (byte) (b | 0x10);
      if (bs[ii+5]) b = (byte) (b | 0x20);
      if (bs[ii+6]) b = (byte) (b | 0x40);
      if (bs[ii+7]) b = (byte) (b | 0x80);

      total = ii;
      writeByte(b);
    }

    // For the last 7 bits (max) we work it out bit by bit, and use shifting.
    b = 0;
    for (int ii = total; ii < bs.length; ii++)
    {
      if (bs[ii]) b = (byte) (b | 0x1);
      b = (byte) (b << 1);
    }
    writeByte(b);
  }

  public void write2dBoolArray(boolean[][] bs, boolean isJagged) throws IOException
  {
    // This makes later bits easier.
    if (bs.length == 0) return;

    // For a jagged array write the width, then write the height for each column,
    // followed by the respective column.
    if (isJagged)
    {
      writeInt(bs.length);

      // Send the arrays, with each of their lengths.
      for (int ii = 0; ii < bs.length; ii++)
      {
        writeBoolArray(bs[ii], true);
      }
    }
    else
    {
      // Tell the client the size of this array.
      writeInt(bs.length);
      writeInt(bs[0].length);

      // Send the array. Don't need to send individual lengths, because we've just done that.
      for (int ii = 0; ii < bs.length; ii++)
      {
        writeBoolArray(bs[ii], false);
      }
    }
  }

  // Write two ints for width & height, then send the image data
  // as argb (0xAARRGGBB) ints.
  public void writeImage(Image img) throws IOException
  {
    int width = img.getWidth();
    int height = img.getHeight();
    writeInt(width);
    writeInt(height);

    // Get an array full of rgb data, for us to send.
    int[] rgbData = new int[width*height];
    img.getRGB(rgbData, 0, width, 0, 0, width, height);

    for (int ii = 0; ii < rgbData.length; ii++)
    {
      writeInt(rgbData[ii]);
    }
  }

}
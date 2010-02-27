package com.zom.view.game.multiplayer;

import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Image;

/**
 * Connection
 *
 * @author Tim Perry
 */
public class Connection {

  public static final boolean DEBUG = false;

  // Data type identifiers - To add anything add it here, in read(syncId) and write(syncId, data)
  public static final byte INT_TYPE = 0;
  public static final byte BYTE_TYPE = 1;
  public static final byte STRING_TYPE = 2;
  public static final byte LONG_TYPE = 3;
  public static final byte IMAGE_TYPE = 4;
  public static final byte BOOL_ARRAY_TYPE = 5;
  public static final byte BOOL_ARRAY_2D_TYPE = 6;
  public static final byte BOOL_TYPE = 7;
  public static final byte SYNCABLE_TYPE = 8;
  // Every element of any vectors to be sync'd MUST BE SYNCABLE.
  public static final byte VECTOR_OF_SYNCABLE_TYPE = 10;

  public static final byte PING_VALUE = (byte) 0;

  private ZomDataInputStream in;
  private ZomDataOutputStream out;

  private boolean locked = false;

  // Maps syncIds (here array indexes) to byte[]s of type identifiers (INT_TYPE etc)
  private static Vector types = new Vector();
  // Maps the same syncIds to SyncableFactories for building instances;
  private static Vector factories = new Vector();

  public Connection(StreamConnection s) throws IOException
  {
    this(new ZomDataInputStream(s.openDataInputStream()), new ZomDataOutputStream(s.openOutputStream()));
  }

  // For now we only want Connections to be made for multiplayer. This might change
  // later, but I'll have to think about access modifiers for the streams first (TODO)
  Connection(ZomDataInputStream in, ZomDataOutputStream out)
  {
    this.in = in;
    this.out = out;
    // TODO - Probably want to think about making sure our sync registrations are
    // the same as on the other end...
  }

  public synchronized void lock()
  {
    while (locked)
    {
      try { wait(); }
      catch (Exception e) { }
    }
    locked = true;
  }

  public synchronized void unlock()
  {
    if (!locked) throw new Error("Unlocking connection that was not locked! SHOULD NEVER HAPPEN");
    locked = false;
    notify();
  }

  // Registers the datatypes that a certain object might want to sync.
  // Byte type identifiers should match one of the static final type ids in Connection
  // Clients can provide null for factory, iff they wish to be impossible to instantiate.
  // Returns an id for all instances of that object to use when they sync.
  public static int register(byte[] argumentTypes, SyncableFactory factory)
  {
    types.addElement(argumentTypes);
    int syncId = types.indexOf(argumentTypes);

    factories.addElement(factory);

    // Assertion - since this is the only place we insert, the indexes should always be the same!
    // Could remove this if we used hashtables instead, but vectors are preferable.
    if (syncId != factories.indexOf(factory)) throw new Error("Inconsistent sync registration data when registering "+factory.toString());

    return syncId;
  }

  // Returns an array of bytes, each matching to a datatype, as defined in Connection
  public static byte[] getTypes(int syncId)
  {
    if (syncId == -1)
    {
      if (DEBUG) System.out.println("Conn: WARNING - Getting types of element with syncId -1");
      return new byte[0];
    }
    return (byte[]) types.elementAt(syncId);
  }

  // Returns a syncableFactory for the given sync id.
  public static SyncableFactory getFactory(int syncId)
  {
    return (SyncableFactory) factories.elementAt(syncId);
  }

  public Syncable buildInstance(int syncId, Object[] data) throws InstantiationException
  {
    if (syncId == -1) return null;
    if (getFactory(syncId) == null) throw new InstantiationException("Tried to instantiate uninstantiable class, sync id: "+syncId);
    return getFactory(syncId).buildFromData(data);
  }

  // Works out latency on this connection, and returns it (in ms)
  public int ping() throws IOException
  {
    long startTime, endTime;

    // This just blocks to make sure they're in the listenForPing() function, and waiting.
    readByte();

    startTime = System.currentTimeMillis();
    writeByte(PING_VALUE);
    readByte();
    endTime = System.currentTimeMillis();

    if (DEBUG) System.out.println("Conn: Latency is " + (endTime - startTime));

    return (int) (endTime - startTime)/2;
  }

  // Does the above ping function multiple times, and averages.
  public int ping(int times) throws IOException
  {
    int sum = 0;

    for (int ii = 0; ii < times; ii++)
    {
      sum += ping();
    }

    if (DEBUG) System.out.println("Conn: After "+times+" tests, average latency is " + (sum / times));

    return sum / times;
  }

  // The inverse of ping. If your opposite is going to ping you, you should run this first.
  public void listenForPing() throws IOException
  {
    if (DEBUG) System.out.println("Conn: Waiting for ping");

    // Unblock sender, and go, go, go!
    writeByte(PING_VALUE);

    // Quick read/write for timing.
    readByte();
    writeByte(PING_VALUE);

    if (DEBUG) System.out.println("Conn: Was pinged.");
  }

  // Listens for multiple pings.
  public void listenForPing(int times) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Expecting "+times+" pings");

    for (int ii = 0; ii < times; ii++)
    {
      listenForPing();
    }

    if (DEBUG) System.out.println("Conn: Recieved all expected pings");
  }

  public void write(Syncable s) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing syncable, getting id");
    int id = s.getSyncId();
    if (DEBUG) System.out.println("Conn: Got id for write - "+id);
    Object[] data = s.getData();
    if (DEBUG) System.out.println("Conn: Got data for write, writing!");
    write(id, data);
  }

  // Write some data to the stream. Data should be in the correct formats for the given
  // type (according to syncTypes).
  public void write(int syncId, Object[] data) throws IOException
  {
    byte[] datatypes = getTypes(syncId);

    if (DEBUG)
    {
      System.out.println("Conn: Writing object - type "+syncId);
      for (int ii = 0; ii < data.length; ii++)
      {
        System.out.println("Conn:   "+ii+" = "+data[ii].toString());
      }
      System.out.println("Conn: End of object");
    }

    // Tell the reciever what datatype we're sending
    writeInt(syncId);

    // Steps through all the data and unpacks it according to the types array.
    // Worth noting - all arrays have lengths sent with them, could be worth reconsidering this? (TODO)
    // Also, all 2d arrays are not considered jagged.
    for (int ii = 0; ii < datatypes.length; ii++)
    {
      switch (datatypes[ii]) {
        case INT_TYPE:
          writeInt(((Integer)data[ii]).intValue());
          break;
        case BYTE_TYPE:
          writeByte(((Byte)data[ii]).byteValue());
          break;
        case LONG_TYPE:
          writeLong(((Long)data[ii]).longValue());
          break;
        case STRING_TYPE:
          writeString((String)data[ii]);
          break;
        case IMAGE_TYPE:
          writeImage((Image)data[ii]);
          break;
        case BOOL_ARRAY_TYPE:
          writeBoolArray((boolean[])data[ii], true);
          break;
        case BOOL_ARRAY_2D_TYPE:
          write2dBoolArray((boolean[][])data[ii], false);
          break;
        case BOOL_TYPE:
          writeBool(((Boolean)data[ii]).booleanValue());
          break;
        case SYNCABLE_TYPE:
          write((Syncable)data[ii]);
          break;
        case VECTOR_OF_SYNCABLE_TYPE:
          Vector v = (Vector) data[ii];
          if (DEBUG) System.out.println("Conn: Writing vector of length "+v.size());
          writeInt(v.size());
          for (int jj = 0; jj < v.size(); jj++)
          {
            write((Syncable)v.elementAt(jj));
          }
      }
    }
  }

  public Object[] read() throws IOException
  {
    if (DEBUG) System.out.println("Conn: Reading sync id");
    int syncId = readInt();
    return read(syncId);
  }

  // Load a full object's data, and then build one of those objects with the relevant syncable factory
  public Syncable readAndBuild() throws IOException, InstantiationException
  {
    if (DEBUG) System.out.println("Conn: Read (+build) getting sync id");
    int syncId = readInt();
    Object[] data = read(syncId);
    
    return buildInstance(syncId, data);
  }

  // Loads a full object's data, and then updates the given object to use that data.
  public void readAndUpdate(Syncable s) throws IOException, ClassCastException
  {
    if (DEBUG) System.out.println("Conn: Read (+update) getting sync id");
    int syncId = readInt();
    if (syncId != s.getSyncId()) throw new ClassCastException();
    Object[] data = read(syncId);

    s.updateWithData(data);
  }

  // Reads in the data for the next object from the stream, according to the type defined
  // by the next integer in the stream. All data is packed into object wrappers (Integer etc.)
  private Object[] read(int syncId) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Reading object of type - "+syncId);

    byte[] datatypes = getTypes(syncId);
    Object[] data = new Object[datatypes.length];

    for (int ii = 0; ii < datatypes.length; ii++)
    {
      switch (datatypes[ii])
      {
        case INT_TYPE:
          data[ii] = new Integer(readInt());
          break;
        case BYTE_TYPE:
          data[ii] = new Byte(readByte());
          break;
        case LONG_TYPE:
          data[ii] = new Long(readLong());
          break;
        case STRING_TYPE:
          data[ii] = readString();
          break;
        case IMAGE_TYPE:
          data[ii] = readImage();
          break;
        case BOOL_ARRAY_TYPE:
          data[ii] = readBoolArray();
          break;
        case BOOL_ARRAY_2D_TYPE:
          data[ii] = read2dBoolArray(false);
          break;
        case BOOL_TYPE:
          data[ii] = new Boolean(readBool());
          break;
        case SYNCABLE_TYPE:
          try
          {
            data[ii] = readAndBuild();
          }
          catch (Exception e)
          {
            System.out.println("Error building contained syncable: "+e.getMessage());
            e.printStackTrace();
            data[ii] = null;
          }
          break;
        case VECTOR_OF_SYNCABLE_TYPE:
          Vector v = new Vector();
          int vectorSize = readInt();
          System.out.println("Conn: Reading vector of size "+vectorSize);
          for (int jj = 0; jj < vectorSize; jj++)
          {
            try
            {
              Object o = readAndBuild();
              System.out.println("Read vector element "+jj+": "+o.toString());
              v.addElement(o);
            }
            catch (Exception e) { System.out.println("Conn: Trying to read bad Vector."); }
          }
          data[ii] = v;
          System.out.println("Conn: Read vector");
          break;
      }
    }

    if (DEBUG)
    {
      System.out.println("Conn: Object data (type "+syncId+") read:");
      for (int ii = 0; ii < data.length; ii++)
      {
        System.out.print("Conn:   "+ii);
        System.out.println(" = "+data[ii]);
      }
      System.out.println("Conn: End of object");
    }

    return data;
  }

  // The below is a long list of wrapper functions around our ZomData(Input/Output)Streams.
  // The only notable part is that all strings are sent as UTF8.
  public void writeInt(int i) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing int - "+i);
    out.writeInt(i);
  }

  public int readInt() throws IOException
  {
    if (DEBUG)  
    {
      System.out.print("Conn: Expecting int - ");
      int i = in.readInt();
      System.out.println("got "+i);
      return i;
    }
    else return in.readInt();
  }

  public void writeByte(byte b) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing byte - "+b);
    out.writeByte(b);
  }

  public byte readByte() throws IOException
  {
    if (DEBUG)
    {
      System.out.print("Conn: Expecting byte - ");
      byte b = in.readByte();
      System.out.println("got "+b);
      return b;
    }
    else return in.readByte();
  }

  public void writeLong(long l) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing long - "+l);
    out.writeLong(l);
  }

  public long readLong() throws IOException
  {
    if (DEBUG)
    {
      System.out.print("Conn: Expecting long - ");
      long l = in.readLong();
      System.out.println("got "+l);
      return l;
    }
    else return in.readLong();
  }

  public void writeDouble(double d) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing double - "+d);
    out.writeDouble(d);
  }

  public double readDouble() throws IOException
  {
    if (DEBUG)
    {
      System.out.print("Conn: Expecting double - ");
      double d = in.readDouble();
      System.out.println("got "+d);
      return d;
    }
    else return in.readDouble();
  }

  public void writeFloat(float f) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing float - "+f);
    out.writeFloat(f);
  }

  public float readFloat() throws IOException
  {
    if (DEBUG)
    {
      System.out.print("Conn: Expecting float - ");
      float f = in.readFloat();
      System.out.println("got "+f);
      return f;
    }
    else return in.readFloat();
  }

  public void writeString(String s) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing string - "+s);
    out.writeUTF(s);
  }

  public String readString() throws IOException
  {
    if (DEBUG)
    {
      System.out.print("Conn: Expecting string - ");
      String s = in.readUTF();
      System.out.println("got "+s);
      return s;
    }
    else return in.readUTF();
  }

  public void writeImage(Image i) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing image - "+i.toString());
    out.writeImage(i);
  }

  public Image readImage() throws IOException
  {
    if (DEBUG) System.out.println("Conn: Expecting image");
    return in.readImage();
  }

  public void writeBoolArray(boolean[] bs, boolean writeLength) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing boolean array - "+bs.toString());
    out.writeBoolArray(bs, writeLength);
  }

  public boolean[] readBoolArray() throws IOException
  {
    if (DEBUG) System.out.println("Conn: Expecting bool array");
    return in.readBoolArray();
  }

  public boolean[] readBoolArray(int l) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Expecting bool array of length "+l);
    return in.readBoolArray(l);
  }

  public void write2dBoolArray(boolean[][] bss, boolean isJagged) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing 2d boolean array - "+bss.toString());
    out.write2dBoolArray(bss, isJagged);
  }

  public boolean[][] read2dBoolArray(boolean isJagged) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Expecting 2d bool array");
    return in.read2dBoolArray(isJagged);
  }

  public void writeBool(boolean b) throws IOException
  {
    if (DEBUG) System.out.println("Conn: Writing boolean - "+b);
    out.writeBoolean(b);
  }

  public boolean readBool() throws IOException
  {
    if (DEBUG)
    {
      System.out.print("Conn: Expecting boolean - ");
      boolean b = in.readBoolean();
      System.out.println("got "+b);
      return b;
    }
    else return in.readBoolean();
  }

  public void flush() throws IOException
  {
    if (DEBUG) System.out.println("Conn: Flushing");
    out.flush();
  }

  public void close()
  {
    System.out.println("Conn: Explicitly closing connection");
    try
    {
      in.close();
      out.close();
    }

    catch (IOException e) { }

    finally
    {
      in = null;
      out = null;
    }
  }

  public boolean isConnected()
  {
    return in != null && out != null;
  }


}

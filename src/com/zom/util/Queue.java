package com.zom.util;

/**
 * Queue
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class Queue
{
  private Object[] a = new Object[10];
  // The index of the next empty space.
  private int back = 0;
  // The index of the first element
  private int front = 0;
  // The number of elements in the array.
  private int size = 0;
  private final Object frontLock = new Object();
  private final Object backLock = new Object();

  public void enqueue(Object o)
  {
    synchronized(frontLock)
    {
      if (size == a.length) expand();

      a[back] = o;
      back = (back + 1) % a.length;
      size++;
    }
  }

  public Object dequeue()
  {
    synchronized(backLock)
    {
      if (empty()) return null;

      Object o = a[front];
      front = (front + 1) % a.length;
      size--;

      if (size < a.length / 4) shrink();

      return o;
    }
  }

  public boolean empty()
  {
    return size == 0;
  }

  public int size()
  {
    return size;
  }

  private void expand()
  {
    synchronized(frontLock)
    {
      synchronized(backLock)
      {
        Object[] b = new Object[a.length * 2];

        if (size != a.length)
        {
          return;
        }
        else
        {
          System.arraycopy(a, front, b, 0, a.length - front);
          System.arraycopy(a, 0, b, a.length - front, front);
        }

        front = 0;
        back = size;
        a = b;
      }
    }
  }

  private void shrink()
  {
    synchronized(frontLock)
    {
      synchronized(backLock)
      {
        if (size > a.length / 2) return;

        Object[] b = new Object[a.length / 2];

        // Contiguous block
        if (front <= back)
        {
          System.arraycopy(a, front, b, 0, size);
        }
        // Non-contiguous
        else
        {
          System.arraycopy(a, front, b, 0, a.length - front);
          System.arraycopy(a, 0, b, a.length - front, back);
        }

        a = b;
        front = 0;
        back = size;
      }
    }
  }
}

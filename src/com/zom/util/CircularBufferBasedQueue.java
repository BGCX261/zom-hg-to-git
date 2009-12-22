package com.zom.util;

/**
 * QueueFromScratch
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class CircularBufferBasedQueue implements Queue
{
  private Object[] a = new Object[10];
  // The index of the next empty space.
  private int end = 0;
  // The index of the first element
  private int front = 0;
  // The number of elements in the array.
  private int size = 0;

  public void enqueue(Object o)
  {

    if (size == a.length) expand();

    a[end] = o;
    end = (end + 1) % a.length;
    size++;
  }

  public Object dequeue()
  {

    if (empty()) return null;

    Object o = a[front];
    front = (front + 1) % a.length;
    size--;

    if (size < a.length / 4) shrink();

    return o;
  }

  public boolean empty()
  {
    return size == 0;
  }

  private void expand()
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
    end = size;
    a = b;
  }

  private void shrink()
  {
    if (size > a.length / 2) return;

    Object[] b = new Object[a.length / 2];

    // Contiguous block
    if (front <= end)
    {
      System.arraycopy(a, front, b, 0, size);
    }
    // Non-contiguous
    else
    {
      System.arraycopy(a, front, b, 0, a.length - front);
      System.arraycopy(a, 0, b, a.length - front, end);
    }

    a = b;
    front = 0;
    end = size;
  }
}

package com.zom.util;

/**
 * LinkedListBasedQueue
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class LinkedListBasedQueue implements Queue
{
  // This is the terminator of our linked list. Useful to have as an object
  // so we can lock on it.
  //private final LinkedList nullList = new LinkedList(null, null);

  private LinkedList end = null;
  private LinkedList front = null;

  public void enqueue(Object o)
  {
    LinkedList temp = new LinkedList(o);

    if (front == null)
    {
      front = temp;
    }
    else
    {
      end.next = temp;
    }

    end = temp;
  }

  public Object dequeue()
  {
    if (end == null) return null;

    LinkedList temp = front;
    front = temp.next;

    return temp.o;
  }

  public boolean empty()
  {
    return end == null;
  }

  private class LinkedList
  {
    public LinkedList next;
    public final Object o;

    public LinkedList(Object o)
    {
      this.o = o;
    }
  }
}

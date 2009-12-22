package com.zom.util;

/**
 * LinkedListBasedQueue
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class LinkedListBasedQueue implements Queue
{
  private LinkedList end = null;
  private LinkedList front = null;

  public void enqueue(Object o)
  {
    end = new LinkedList(end, o);

    if (front == null)
    {
      front = end;
    }
    else
    {
      end.previous.next = end;
    }
  }

  public Object dequeue()
  {
    if (end == null) return null;

    LinkedList temp = front;
    front = temp.next;

    if (front == null) end = null;
    else front.previous = null;

    return temp.o;
  }

  public boolean empty()
  {
    return end == null;
  }

  private class LinkedList
  {
    public LinkedList previous;
    public LinkedList next;
    public final Object o;

    public LinkedList(LinkedList l, Object o)
    {
      this.previous = l;
      this.o = o;
    }
  }
}

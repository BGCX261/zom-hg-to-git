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
  private static final LinkedList nullList = new LinkedList(null);

  // New elements are added to the end of the queue.
  private LinkedList end = nullList;
  // Elements are taken from the front of the queue.
  private LinkedList front = nullList;

  public void enqueue(Object o)
  {
    LinkedList temp = new LinkedList(o);

    synchronized(end)
    {
      if (front == nullList)
      {
        synchronized(front)
        {
          if (front == nullList)
          {
            front = temp;
          }
        }
      }
      else
      {
        end.next = temp;
      }

      end = temp;
    }
  }

  public Object dequeue()
  {
    if (empty()) return null;

    LinkedList temp;

    synchronized(front)
    {
      temp = front;
      front = temp.next;
    }

    return temp.o;
  }

  public boolean empty()
  {
    return front == nullList;
  }

  private static class LinkedList
  {
    public LinkedList next = nullList;
    public final Object o;

    public LinkedList(Object o)
    {
      this.o = o;
    }

  }
}

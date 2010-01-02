package com.zom.util;

import java.util.Stack;

/**
 * Queue
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class StackBasedQueue implements Queue
{
  private Stack a = new Stack();
  private Stack b = new Stack();
  private Stack currentFront = a;
  private Stack currentBack = b;

  private final Object frontLock = new Object();
  private final Object backLock = new Object();

  /**
   * Returns null if queue is empty.
   * @return
   */
  public Object dequeue()
  {
    synchronized (frontLock)
    {
      if (currentFront.empty())
      {
        synchronized (backLock)
        {
          if (currentBack.empty()) return null;
          else swap();
        }
      }

      return currentFront.pop();
    }
  }

  public void enqueue(Object o)
  {
    synchronized (backLock)
    {
      currentBack.push(o);
    }
  }

  protected synchronized void swap()
  {
    synchronized (backLock)
    {
      synchronized (frontLock)
      {
        Stack temp = currentFront;
        currentFront = currentBack;
        currentBack = temp;

        reverse(currentFront);
      }
    }
  }

  private static void reverse(Stack s)
  {
    Object temp;

    for (int ii = 0; ii < s.size() / 2; ii++)
    {
      temp = s.elementAt(ii);
      s.setElementAt(s.elementAt(s.size() - 1 - ii), ii);
      s.setElementAt(temp, s.size() - 1 - ii);
    }
  }

  public boolean empty()
  {
    return a.empty() && b.empty();
  }
}

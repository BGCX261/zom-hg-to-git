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

  /**
   * Returns null if queue is empty.
   * @return
   */
  public Object dequeue()
  {
    if (currentFront.empty())
    {
      if (currentBack.empty()) return null;
      else swap();
    }

    return currentFront.pop();
  }

  public void enqueue(Object o)
  {
    currentBack.push(o);
  }

  protected void swap()
  {
    Stack temp = currentFront;
    currentFront = currentBack;
    currentBack = temp;

    reverse(currentFront);
  }

  private static void reverse(Stack s)
  {
    Object temp;

    // < might need to be <= because of rounding. (tests should show that though)
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

/*
 * QueueTester.java
 *
 * Created on 22-Dec-2009, 16:08:15
 */
package com.zom.tests;

import com.zom.util.*;
import jmunit.framework.cldc10.TestCase;

/**
 * @author tim
 */
public class QueueTester extends TestCase
{
  // Within each fill/empty of the queue, fill the queue with this many elements.
  public static final int TEST_LENGTH = 40000;

  // Within each queue's test, fill and empty the queue this many times.
  public static final int TEST_COUNT = 3;

  // The entire test is run this many times, for each queue.
  public static final int TEST_RUNS = 20;

  public QueueTester()
  {
    super(3, "QueueTester");
  }

  public void test(int arg0) throws Throwable
  {
    long[] times = new long[TEST_RUNS];

    for (int ii = 0; ii < TEST_RUNS; ii++)
    {
      long start = System.currentTimeMillis();
      
      switch (arg0)
      {
        case 0:
          testQueue(new StackBasedQueue(), TEST_LENGTH, TEST_COUNT);
          break;
        case 1:
          testQueue(new CircularBufferBasedQueue(), TEST_LENGTH, TEST_COUNT);
          break;
        case 2:
          testQueue(new LinkedListBasedQueue(), TEST_LENGTH, TEST_COUNT);
          break;
      }
      System.gc();

      long end = System.currentTimeMillis();

      times[ii] = end - start;
    }

    //Thread.sleep(5000); // Makes it easier to spot things on the memory monitor
    
    System.out.print("Times for " + arg0 + ": ");
    
    long total = 0;
    for (int ii = 0; ii < times.length; ii++)
    {
      total += times[ii];
      System.out.print(times[ii]+"ms ");
    }
    System.out.println("- Average: "+(total/times.length));
  }

  public void testQueue(Queue q, int length, int tests)
  {
    for (int ii = 0; ii < tests; ii++)
    {
      fillQueue(q, length);
      emptyQueue(q, length);
    }
  }

  public void fillQueue(Queue q, int length)
  {
    for (int ii = 0; ii < length; ii++)
    {
      q.enqueue(new Integer(ii));
    }
  }

  public void emptyQueue(Queue q, int length)
  {
    Integer j = new Integer(-1);
    Integer i;
    for (int ii = 0; ii < length; ii++)
    {
      i = (Integer) q.dequeue();

      assertEquals(i.intValue(), j.intValue() + 1);

      j = i;
    }
  }
}

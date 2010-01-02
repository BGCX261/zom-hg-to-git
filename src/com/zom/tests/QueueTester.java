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
  public static final int TEST_LENGTH = 3;

  // Within each queue's test, fill and empty the queue this many times.
  public static final int TEST_COUNT = 3000;

  // The entire test is run this many times, for each queue.
  public static final int TEST_RUNS = 20;

  public QueueTester()
  {
    super(3, "QueueTester");
  }

  public void test(int arg0)
  {
    long[] times = new long[TEST_RUNS];

    for (int ii = 0; ii < TEST_RUNS; ii++)
    {
      long start = System.currentTimeMillis();
      
      switch (arg0)
      {
        case 0:
          testQueueConcurrently(new StackBasedQueue(), TEST_LENGTH, TEST_COUNT);
          break;
        case 1:
          testQueueConcurrently(new CircularBufferBasedQueue(), TEST_LENGTH, TEST_COUNT);
          break;
        case 2:
          testQueueConcurrently(new LinkedListBasedQueue(), TEST_LENGTH, TEST_COUNT);
          break;
      }
      System.gc();

      long end = System.currentTimeMillis();

      times[ii] = end - start;
    }
    
    try
    {
      Thread.sleep(2000); // Makes it easier to spot things on the memory monitor
    }
    catch (InterruptedException ex) { }

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
      new Filler(q, length).run();
      new Emptier(q, length).run();
    }
  }

  public void testQueueConcurrently(final Queue q, final int length, int tests)
  {
    Filler filler = new Filler(q, length);
    Emptier emptier = new Emptier(q, length);

    for (int ii = 0; ii < tests; ii++)
    {
      new Thread(filler).start();      
      new Thread(emptier).start();
      
      while (!filler.done || !emptier.done)
      {
        try
        {
          Thread.sleep(10);
        }
        catch (InterruptedException ex) { }
      }
    }
  }

  public static class Filler implements Runnable
  {
    public boolean done = false;
    
    public Queue q;
    public int length;

    public Filler(Queue q, int length)
    {
      this.q = q;
      this.length = length;
    }

    public synchronized void run()
    {
      done = false;
      for (int ii = 0; ii < length; ii++)
      {
        q.enqueue(new Integer(ii));
      }
      done = true;
    }
  }

  public static class Emptier implements Runnable
  {
    public boolean done = false;

    public Queue q;
    public int length;

    public Emptier(Queue q, int length)
    {
      this.q = q;
      this.length = length;
    }

    public synchronized void run()
    {
      try
      {
        done = false;

        Integer j = new Integer(-1);
        Integer i;
        for (int ii = 0; ii < length; ii++)
        {
          while (q.empty())
          {
            try
            {
              Thread.sleep(10);
            }
            catch (Exception ex) { }
          }

          i = (Integer) q.dequeue();

          if (i.intValue() != j.intValue()+1) throw new Error("i ("+i.intValue()+") != j+1 ("+(j.intValue()+1)+")");

          j = i;
        }
        done = true;
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

}

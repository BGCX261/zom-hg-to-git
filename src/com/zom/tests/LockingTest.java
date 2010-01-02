package com.zom.tests;

import jmunit.framework.cldc10.TestCase;

/**
 * LockingTest
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class LockingTest extends TestCase
{
  private Object lock = new Integer(0);

  public LockingTest()
  {
    super(1, "LockingTest");
  }

  public void test(int arg0) throws Throwable
  {
    System.out.println("starting a");
    new Thread(new ThreadA()).start();
    System.out.println("starting b");
    new Thread(new ThreadB()).start();
  }

  private class ThreadA implements Runnable
  {
    public void run()
    {
      try
      {
        System.out.println("a started");
        synchronized(lock)
        {
          System.out.println("a has lock");
          Thread.sleep(2000);
          System.out.println("a resets lock");
          lock = new Integer(1);
          
          Thread.sleep(5000);
          System.out.println("a done");
        }
      }
      catch (Exception e)
      {
        System.out.println("Interrupted");
        fail();
      }
    }
  }

  private class ThreadB implements Runnable
  {
    public void run()
    {
      try
      {
        System.out.println("b started");
        Thread.sleep(1000);
        System.out.println("b looking for lock");
        synchronized(lock)
        {
          System.out.println("b gets lock");
        }
      }
      catch (Exception e)
      {
        System.out.println("Interrupted");
        fail();
      }
    }
  }

}

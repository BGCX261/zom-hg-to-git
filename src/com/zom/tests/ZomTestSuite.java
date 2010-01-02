package com.zom.tests;

import jmunit.framework.cldc10.TestSuite;

/**
 * ZomTestSuite
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public class ZomTestSuite extends TestSuite
{
  public ZomTestSuite()
  {
    super("All tests");
    add(new QueueTester());
    //add(new LockingTest());
  }

}

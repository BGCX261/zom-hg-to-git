/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.zom.util;

/**
 *
 * @author Tim Perry (tim@tim-perry.co.uk)
 */
public interface Queue
{

  public void enqueue(Object o);

  public Object dequeue();

  public boolean empty();

}

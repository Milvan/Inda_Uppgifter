/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kth.csc.inda;

/**
 * This counter holds a value that can be ticked and reset. Counts from 0 to Integer max value.
 * @author Marcus
 */
public class Counter {
    private int value;
    
     /**
         * Increase the counter value by 1.
         */
        public void count(){
            value++;
        }
        /**
         * Reset the counter value to 0.
         */
        public void resetCounter(){
            value=0;
        }

        /**
         * Will return the value that the counter holds.
         * @return value of the counter.
         */
    public int getValue() {
        return value;
    }
    
        
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.raspberry.demo.server.test;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bitmathe
 */
public class GPIOTest {
    
    public static void main(String[] args) {
        if(args.length != 2){
            System.out.println("Please follow the usage GPIOTest <blinks> <interval>");
        }
        int numBlinks=Integer.parseInt(args[0]);
        int interval=Integer.parseInt(args[1]);
        GpioPinDigitalOutput myLed;
        GpioController controller=GpioFactory.getInstance();
        myLed=controller.provisionDigitalOutputPin(RaspiPin.GPIO_07);
        for(int i=0;i<numBlinks;i++)
        {
            try {
                myLed.setState(true);
                Thread.sleep(interval);
                myLed.setState(false);
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                Logger.getLogger(GPIOTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
        
    }
    
}

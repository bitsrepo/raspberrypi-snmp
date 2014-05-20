package com.raspberry.demo.server.sensor;


/*
 * Java Embedded Raspberry Pi GPIO app
 */

import java.io.FileWriter;
import java.io.File;


public class PortAccess {
    
    static final String GPIO_OUT = "out";
    static final String GPIO_ON = "1";
    static final String GPIO_OFF = "0";
   // static final String GPIO_CH00="0";

    /**
     * @param args the command line arguments
     */
    public static void setGPIO(String gpioChannel, boolean status ){
        
        try {
            
            /*** Init GPIO port for output ***/
            
            // Open file handles to GPIO port unexport and export controls
            FileWriter unexportFile = new FileWriter("/sys/class/gpio/unexport");
            FileWriter exportFile = new FileWriter("/sys/class/gpio/export");

            // Reset the port
            File exportFileCheck = new File("/sys/class/gpio/gpio"+gpioChannel);
            if (exportFileCheck.exists()) {
                unexportFile.write(gpioChannel);
                unexportFile.flush();
            }
            
            
            // Set the port for use
            exportFile.write(gpioChannel);   
            exportFile.flush();

            // Open file handle to port input/output control
            FileWriter directionFile =
                    new FileWriter("/sys/class/gpio/gpio"+gpioChannel+"/direction");
            
            // Set port for output
            directionFile.write(GPIO_OUT);
            directionFile.flush();
            
            /*** Send commands to GPIO port ***/
            
            // Open file handle to issue commands to GPIO port
            FileWriter commandFile = new FileWriter("/sys/class/gpio/gpio"+gpioChannel+
                    "/value");
            
            // Loop forever
            if(status) {                
                // Set GPIO port ON
                commandFile.write(GPIO_ON);
            }else{
            	commandFile.write(GPIO_OFF);
            }
        
              
                
                commandFile.flush();
                
               
            
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

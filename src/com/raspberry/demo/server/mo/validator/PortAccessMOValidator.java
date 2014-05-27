/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.raspberry.demo.server.mo.validator;

import com.raspberry.demo.server.portio.PortAccess;
import org.snmp4j.agent.mo.MOValueValidationEvent;
import org.snmp4j.agent.mo.MOValueValidationListener;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

/**
 *
 * @author VAIO
 */
     public class PortAccessMOValidator implements MOValueValidationListener {
          public static String PORT_PIN_7 = "7";
          
        public void validate(MOValueValidationEvent validationEvent) {
            Variable newValue = validationEvent.getNewValue();
            int v = ((Integer32) newValue).getValue();
            if (v > 0) {
               
                PortAccess.setGPIO(PORT_PIN_7, true);
            } else {
                PortAccess.setGPIO(PORT_PIN_7, false);
            }

        }
     }
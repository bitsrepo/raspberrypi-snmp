/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.raspberry.demo.server.mo;

import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

/**
 *
 * @author mitthoma
 */
        
public class TempSensorMO extends MOScalar {
    
   public  TempSensorMO(OID oid, MOAccess access) {
      super(oid, access, new Integer32());
//--AgentGen BEGIN=shAirCondTemperature
//--AgentGen END
    }



    public Variable getValue() {
     //--AgentGen BEGIN=shAirCondTemperature::getValue
     //--AgentGen END
      return  super.getValue();    
    }

    @Override
    public int setValue(Variable newValue) {
     //--AgentGen BEGIN=shAirCondTemperature::setValue
     //--AgentGen END
      return super.setValue(newValue);    
    }

     //--AgentGen BEGIN=shAirCondTemperature::_METHODS
     //--AgentGen END

  }
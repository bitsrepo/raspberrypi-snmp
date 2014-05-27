/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raspberry.demo.server.mo;

import com.raspberry.demo.server.sensor.TempSensor;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

/**
 *
 * @author mitthoma
 */
public class TempSensorMO extends MOScalar {

    public TempSensorMO(OID oid, MOAccess access) {
        super(oid, access, new Integer32());
    }

    @Override
    public Variable getValue() {
        TempSensor sensor = new TempSensor();
        String temp = sensor.readTemperature();
        System.out.println("temperature=" + temp);
        return new OctetString(temp);     
    }

    @Override
    public int setValue(Variable newValue) {
        return super.setValue(newValue);
    }

}

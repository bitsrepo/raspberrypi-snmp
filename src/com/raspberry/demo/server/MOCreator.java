/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.raspberry.demo.server;

/**
 *
 * @author bitmathe
 */


import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

/**
 * This class creates and returns ManagedObjects
 * @author Bittu
 *
 */
public class MOCreator {
	public static MOScalar createReadOnly(OID oid,Object value ){
		return new MOScalar(oid,
				MOAccessImpl.ACCESS_READ_ONLY,
				getVariable(value));
	}

	private static Variable getVariable(Object value) {
		if(value instanceof String) {
			return new OctetString((String)value);
		}
		throw new IllegalArgumentException("Unmanaged Type: " + value.getClass());
	}

}

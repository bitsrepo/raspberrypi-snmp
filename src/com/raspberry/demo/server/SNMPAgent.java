/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raspberry.demo.server;

import com.raspberry.demo.server.mo.PortAccessMO;
import com.raspberry.demo.server.mo.TempSensorMO;
import com.raspberry.demo.server.mo.validator.PortAccessMOValidator;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.DefaultMOFactory;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOFactory;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.TransportMappings;

/**
 *
 * @author VAIO
 */
public class SNMPAgent extends BaseAgent {

    // todo make this configurable
    static final OID ledOut = new OID(".1.3.8.1.2.1.1.1.0");
    static final OID tempSensor = new OID(".1.3.7.1.2.1.1.1.0");
    private String address;

    /**
     *
     * @param address
     * @throws IOException
     */
    public SNMPAgent(String address) throws IOException {

        /**
         * Agent with boot-counter, config file, and a CommandProcessor for
         * processing SNMP requests.
         */
        super(new File("conf.agent"), new File("bootCounter.agent"),
                new CommandProcessor(
                        new OctetString(MPv3.createLocalEngineID())));
        this.address = address;
    }

    /**
     * Adds community to security name mappings needed for SNMPv1 and SNMPv2c.
     *
     * @param communityMIB
     */
    @Override
    protected void addCommunities(SnmpCommunityMIB communityMIB) {
        Variable[] com2sec = new Variable[]{new OctetString("public"),
            new OctetString("cpublic"), // security name
            getAgent().getContextEngineID(), // local engine ID
            new OctetString("public"), // default context name
            new OctetString(), // transport tag
            new Integer32(StorageType.nonVolatile), // storage type
            new Integer32(RowStatus.active) // row status
    };
        MOTableRow row = communityMIB.getSnmpCommunityEntry().createRow(
                new OctetString("public2public").toSubIndex(true), com2sec);
        communityMIB.getSnmpCommunityEntry().addRow(row);

    }

    /**
     * Adds initial notification targets and filters.
     *
     * @param arg0
     * @param arg1
     */
    @Override
    protected void addNotificationTargets(SnmpTargetMIB arg0,
            SnmpNotificationMIB arg1) {

    }

    /**
     * Adds all the necessary initial users to the USM.
     *
     * @param arg0
     */
    @Override
    protected void addUsmUser(USM arg0) {

    }

    /**
     * Adds initial VACM configuration.
     *
     * @param vacm
     */
    @Override
    protected void addViews(VacmMIB vacm) {
        vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(
                "cpublic"), new OctetString("v1v2group"),
                StorageType.nonVolatile);

        vacm.addAccess(new OctetString("v1v2group"), new OctetString("public"),
                SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV,
                MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView"),
                new OctetString("fullWriteView"), new OctetString(
                        "fullNotifyView"), StorageType.nonVolatile);

        vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"),
                new OctetString(), VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile);

        vacm.addViewTreeFamily(new OctetString("fullWriteView"), new OID("1.3"),
                new OctetString(), VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile);

    }

    /**
     * Unregister the basic MIB modules from the agent's MOServer.
     */
    @Override
    protected void unregisterManagedObjects() {
        // TODO Auto-generated method stub

    }

    /**
     * Register additional managed objects at the agent's server.
     */
    @Override
    protected void registerManagedObjects() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initTransportMappings() throws IOException {
        transportMappings = new TransportMapping[1];
        Address addr = GenericAddress.parse(address);
        TransportMapping tm = TransportMappings.getInstance()
                .createTransportMapping(addr);
        transportMappings[0] = tm;
    }

    /**
     * Start method invokes some initialization methods needed to start the
     * agent
     *
     * @throws IOException
     */
    public void start() throws IOException {

        init();
        addShutdownHook();
        // todo make this configurable
        getServer().addContext(new OctetString("public"));
        finishInit();
        run();
        sendColdStartNotification();
    }

    /**
     * Clients can register the MO they need
     *
     * @param mo
     */
    public void registerManagedObject(ManagedObject mo) {
        try {
            server.register(mo, null);
        } catch (DuplicateRegistrationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void unregisterManagedObject(MOGroup moGroup) {
        moGroup.unregisterMOs(server, getContext(moGroup));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private final MOFactory moFactory
            = DefaultMOFactory.getInstance();

    public PortAccessMO createPortAccessMO(OID oid) {
        return new PortAccessMO(oid,
                moFactory.createAccess(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE)
        );
    }

    public TempSensorMO createTempSensorMO(OID oid) {
        return new TempSensorMO(oid,
                moFactory.createAccess(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE)
        );
    }

    public static void main(String[] args) throws IOException {
        SNMPAgent agent = new SNMPAgent("0.0.0.0/161");
        agent.start();
        PortAccessMO portMO = agent.createPortAccessMO(ledOut);
        portMO.addMOValueValidationListener(new PortAccessMOValidator());
        agent.registerManagedObject(portMO);

        //create and register temperature sensor MO
        TempSensorMO tmo = agent.createTempSensorMO(tempSensor);
        agent.registerManagedObject(tmo);

        // Setup the client to use our newly started agent
        SNMPManager client = new SNMPManager("udp:127.0.0.1/161");
        client.start();
        // read a test value
        System.out.println(client.getAsString(tempSensor));

        while (true) {
            // keep main thread running, 
            // a deamon thread runs in background and process snmp requests
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SNMPAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

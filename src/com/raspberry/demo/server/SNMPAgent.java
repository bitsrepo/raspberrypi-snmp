package com.raspberry.demo.server;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.raspberry.demo.server.mo.TempSensorMO;
import com.raspberry.demo.server.sensor.PortAccess;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.DefaultMOFactory;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOFactory;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.MOValueValidationEvent;
import org.snmp4j.agent.mo.MOValueValidationListener;
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

public class SNMPAgent extends BaseAgent {

    static final OID ledOut = new OID(".1.3.8.1.2.1.1.1.0");
    static final OID tempSensor = new OID(".1.3.7.1.2.1.1.1.0");

    private String strValue;

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private MOFactory moFactory
            = DefaultMOFactory.getInstance();

    public static void main(String[] args) throws IOException {
        SNMPAgent agent = new SNMPAgent("0.0.0.0/161");
        agent.start();

		// Since BaseAgent registers some MIBs by default we need to unregister
        // one before we register our own ledOut. Normally you would
        // override that method and register the MIBs that you need
        agent.unregisterManagedObject(agent.getSnmpv2MIB());
        agent.setStrValue("My first value set");
		// Register a system description, use one from you product environment
        // to test with
//                shFridgeTemperature = 
//      new ShFridgeTemperature(oidShFridgeTemperature, 
//                              moFactory.createAccess(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE));
        PortMO portMO = agent.createPortMO(ledOut,
                agent.getStrValue());
        portMO.addMOValueValidationListener(new PortMOValidator());
        agent.registerManagedObject(portMO);

                //create and register temperature sensor MO
        TempSensorMO tmo = agent.createTempSensorMO(tempSensor);
        agent.registerManagedObject(tmo);

        // Setup the client to use our newly started agent
        SNMPManager client = new SNMPManager("udp:127.0.0.1/161");
        client.start();
        // Get back Value which is set
        System.out.println(client.getAsString(ledOut));
        while (true) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SNMPAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public PortMO createPortMO(OID oid, Object value) {
        return new PortMO(oid,
                moFactory.createAccess(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE)
        );
    }

    public TempSensorMO createTempSensorMO(OID oid) {
        return new TempSensorMO(oid,
                moFactory.createAccess(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE)
        );
    }

//	private static Variable getVariable(Object value) {
//		if(value instanceof String) {
//			return new OctetString((String)value);
//		}
//		throw new IllegalArgumentException("Unmanaged Type: " + value.getClass());
//	}
//	
    private String address;

    /**
     *
     * @param address
     * @throws IOException
     */
    public SNMPAgent(String address) throws IOException {

        /**
         * Creates a base agent with boot-counter, config file, and a
         * CommandProcessor for processing SNMP requests. Parameters:
         * "bootCounterFile" - a file with serialized boot-counter information
         * (read/write). If the file does not exist it is created on shutdown of
         * the agent. "configFile" - a file with serialized configuration
         * information (read/write). If the file does not exist it is created on
         * shutdown of the agent. "commandProcessor" - the CommandProcessor
         * instance that handles the SNMP requests.
         */
        super(new File("conf.agent"), new File("bootCounter.agent"),
                new CommandProcessor(
                        new OctetString(MPv3.createLocalEngineID())));
        this.address = address;
    }

    /**
     * Adds community to security name mappings needed for SNMPv1 and SNMPv2c.
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
     */
    @Override
    protected void addNotificationTargets(SnmpTargetMIB arg0,
            SnmpNotificationMIB arg1) {
        // TODO Auto-generated method stub

    }

    /**
     * Adds all the necessary initial users to the USM.
     */
    @Override
    protected void addUsmUser(USM arg0) {
        // TODO Auto-generated method stub

    }

    /**
     * Adds initial VACM configuration.
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
		// This method reads some old config from a file and causes
        // unexpected behavior.
        // loadConfig(ImportModes.REPLACE_CREATE);
        addShutdownHook();
        getServer().addContext(new OctetString("public"));
        finishInit();
        run();
        sendColdStartNotification();
    }

    /**
     * Clients can register the MO they need
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

    public class PortMO extends MOScalar {

        PortMO(OID oid, MOAccess access) {
            super(oid, access, new Integer32());
//--AgentGen BEGIN=shAirCondTemperature
//--AgentGen END
        }

        public Variable getValue() {
     //--AgentGen BEGIN=shAirCondTemperature::getValue
            //--AgentGen END
            return super.getValue();
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

    static class PortMOValidator implements MOValueValidationListener {

        public void validate(MOValueValidationEvent validationEvent) {
            Variable newValue = validationEvent.getNewValue();
            int v = ((Integer32) newValue).getValue();
            if (v > 0) {
                /* try {
                 System.out.println("About to blink LED");
                 blinkLed();
                 System.out.println("LED blink over!!!!!");
                 } catch (InterruptedException ex) {
                 throw new RuntimeException(ex);
                 }
          
                 */

                PortAccess.setGPIO("7", true);

            } else {
                PortAccess.setGPIO("7", false);
            }

        }

        private void blinkLed() throws InterruptedException {
            GpioPinDigitalOutput myLed;
            GpioController controller = GpioFactory.getInstance();
            myLed = controller.provisionDigitalOutputPin(RaspiPin.GPIO_07);
            for (int i = 0; i < 2; i++) {

                myLed.setState(true);
                Thread.sleep(3000);
                myLed.setState(false);
                Thread.sleep(3000);

            }
        }
    }

}

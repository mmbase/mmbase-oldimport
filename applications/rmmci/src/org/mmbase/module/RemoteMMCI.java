/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

 */
package org.mmbase.module;

import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.mmbase.bridge.LocalContext;
import org.mmbase.bridge.remote.RemoteCloudContext;
import org.mmbase.bridge.remote.rmi.RemoteCloudContext_Rmi;
import org.mmbase.module.core.MMBase;
import org.mmbase.util.logging.*;

/**
 * RemoteMMCI is a MMBase module that starts a Remote Method Invocation
 * registry and binds a remote MMCI context to the server. Look a rmmci.xml for configuration
 * options. Note that in the configuration of mmbaseroot.xml the host should be a valid
 * host address if the RMIRegistryServer in rmmci.xml is no set.
 * @author Kees Jongenburger <keesj@dds.nl>
 * @version $Id: RemoteMMCI.java,v 1.9 2005-10-02 16:59:53 michiel Exp $
 * @since MMBase-1.5
 */
public class RemoteMMCI extends ProcessorModule {

    private Registry registry;

    //get an instance and initialize the logger
    private static final Logger log = Logging.getLoggerInstance(RemoteMMCI.class);

    /**
     * DEFAULT_RMIREGISTRY_PORT = 1111
     */
    public static final int DEFAULT_RMIREGISTRY_PORT = 1111;

    /**
     * DEFAULT_BIND_NAME = "remotecontext"
     */
    public static final String DEFAULT_BIND_NAME = "remotecontext";

    /**
     * Method called by MMBase at startup
     * it calls the createRemoteMMCI based on the rmmci.xml configuration
     */
    public void init() {
        super.init(); // is this required?

        log.debug("Module RemoteMMCI starting");

        //set the class default hard coded start values
        int registryPort = DEFAULT_RMIREGISTRY_PORT;
        String bindName = DEFAULT_BIND_NAME;

        //read the server port from the configuration
        String portString = getInitParameter("port");
        if (portString != null) {
            try {
                registryPort = Integer.parseInt(portString);
            } catch (NumberFormatException nfe) {
                log.warn("port parameter of rmmci.xml is not ot type int");
            };
        } else {
            log.warn("missing port init param, using (default)=(" + registryPort + ")");
        }

        //read the rmi server host from the configuration
        String host = getInitParameter("RMIRegistryServer");
        //if RMIRegistryServer is null or "" use the mmbaseroot.xml host
        if (host == null || host.equals("")) {
            try {
                // load MMBase and make sure it is started first
                MMBase mmbase  = MMBase.getMMBase();
                host = mmbase.getInitParameter("host");
                log.debug("using host FROM MMBASEROOT " + host);
                java.net.InetAddress.getByName(host);
                System.setProperty("java.rmi.server.hostname", host);
            } catch (java.net.UnknownHostException uhn) {
                log.warn("property host in mmbaseroot.xml is not set correctly.");
                log.warn("Chances are big the Remote MMCI will not work");
            }
        } else {
            log.debug("RemoteMMCI is using the RMIRegistryServer{" + host + "} as hostname to create/connect to the RMI registry");
        }

        String bindNameParam = getInitParameter("bindname");
        if (bindNameParam != null) {
            if (bindNameParam.equals("$MACHINENAME")) {
                // use machine name
                bindName = MMBase.getMMBase().getMachineName();
            } else {
                bindName = bindNameParam;
            }
        } else {
            log.warn("missing bindname init param, using (default)=(" + bindName + ")");
        }
        registry = createRemoteMMCI(host, registryPort, bindName);
    }

    /**
     * This method creates or locates the RMI registry at a specific port and host and binds a new RemoteContext
     * @param registryPort the registry port to start the RMI registry
     * @param bindName the name of the object (aka remotecontext)
     * @return the registry used
     */
    private Registry createRemoteMMCI(String host, int registryPort, String bindName) {
        //System.setSecurityManager (new RMISecurityManager ());
        Registry reg = null;
        try {

            try {
                //Note that a getRegistry call does not actually make a connection to the remote host.
                //It simply creates a local reference to the remote registry and will succeed even if
                //no registry is running on the remote host. Therefore, a subsequent method invocation
                //to a remote registry returned as a result of this method may fail.
                reg = java.rmi.registry.LocateRegistry.getRegistry(host, registryPort);
                //try if the registry is running
                reg.list();
                //if no RemoteException is thrown we are probabely ok
                log.debug("using an existing RMI registry");
            } catch (RemoteException rex) {
                reg = java.rmi.registry.LocateRegistry.createRegistry(registryPort);
                log.debug("creating a new RMI registry");
            }

            // Create the Database object
            //interface RemoteCloudContext ... implemented by RemoteCloudContext_Rmi .. using LocalContext
            RemoteCloudContext remoteCloudContext = new RemoteCloudContext_Rmi(LocalContext.getCloudContext());

            log.info("bind RemoteCloudContext in the registry using (tcp port, name)=(" + registryPort + ", " + bindName + ")");

            //bind it to the registry.
            reg.rebind(bindName, remoteCloudContext);
            log.info("Module RemoteMMCI Running on (tcp port,name)=(" + registryPort + "," + bindName + ")");
        } catch (RemoteException rex) {
            log.fatal("RMI Registry not started because of exception {" + rex.getMessage() + Logging.stackTrace(rex) + "}");

        }
        return reg;
    }
    
    /**
     * unbinds the object bound to the registry in order to try to stop the registry
     * this usualy fails(the regsitry keeps running and prevents the webapp to shutdown)
     */
    protected void shutdown() {
        if (registry != null) {
            log.info("Stopping the RMI registry");
            try {
                String[] names = registry.list();
                for (int x = 0; x < names.length; x++) {
                    try {
                    	log.info("unbind " + names[x]);
                        registry.unbind(names[x]);
                    } catch (NotBoundException e1) {
                        log.warn(Logging.stackTrace(e1));
                    }
                }
                if (!UnicastRemoteObject.unexportObject(registry, true)) {
                    log.warn("Could not unexport " + registry);
                } else {
                    log.service("Unexported " + registry);
                }
            } catch (AccessException e) {
                log.warn(Logging.stackTrace(e));
            } catch (RemoteException e) {
                log.warn(Logging.stackTrace(e));
            }
            registry = null;
            // Explicitely calling the garbage collector here helps tomcat to stop faster.
            // It can take several minutes otherwise for the RMI Reaper thread to stop.
            Runtime.getRuntime().gc();
        }
        super.shutdown();
    }
}

/*
 * Copyright 2011 SUSE Linux Products GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package de.suse.logkeeper;

import de.suse.logkeeper.service.LogKeeperService;
import de.suse.logkeeper.service.LogKeeperServiceH2;
import de.suse.logkeeper.service.entities.LogEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlrpc.XmlRpcException;

/**
 * Log keeper main
 * @author bo
 */
public class Main {
    public void testLogKeeperService() {
        try {
            LogKeeperService service = new LogKeeperServiceH2(new URL("file:///tmp/logkeeper"));
            service.log(new LogEntry(null, "glassfish", 1, "Test message", System.getProperty("user.name"),
                                     InetAddress.getLocalHost(), null, new HashMap()));
            // Export
            service.exportToFile(new File("/tmp/logkeeper.xml"), LogKeeperService.FORMAT_XML);
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.getLocalizedMessage());
            System.err.println("----------------------");
            ex.printStackTrace();
            System.err.println("----------------------");
        }
    }


    /**
     * Log keeper daemon.
     */
    public void logKeeperDaemon(Properties setup) {
        LogKeeperDaemon daemon = null;
        try {
            daemon = new LogKeeperDaemon(setup);
            daemon.getConfig().setBasicEncoding("UTF-8");
            daemon.getConfig().setContentLengthOptional(false);
            daemon.getConfig().setEnabledForExceptions(true);
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }


        try {
            daemon.addHandler("audit", de.suse.logkeeper.LogKeeperOperations.class);
        } catch (XmlRpcException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }

        try {
            daemon.start();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
    }


    /**
     * Creates a PID file of the current process.
     * 
     * @param setup
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void createPID(Properties setup) throws FileNotFoundException, IOException {
        String procName = ManagementFactory.getRuntimeMXBean().getName();
        if (procName != null) {
            FileOutputStream stream = new FileOutputStream(new File(setup.getProperty("server.pid.filename", "/var/run/auditlog-keeper.pid")));
            stream.write((procName.split("@")[0] + "\n").getBytes());
            stream.close();
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        StringBuilder buff = new StringBuilder();
        buff.append("key" + "=" + "value");

        if (args.length < 2) {
            System.err.println("Usage: logkeeper --daemon file:///path/to/config");
            System.exit(0);
        } else if (args[0].equals("--daemon")) {
            Properties setup = new Properties();
            try {
                setup.load(new FileInputStream(new File(new URL(args[1]).toURI())));
            } catch (IOException ex) {
                System.err.println("IO Error: " + ex.getLocalizedMessage());
                System.exit(0);
            } catch (URISyntaxException ex) {
                System.err.println("Error: " + ex.getLocalizedMessage() + "\nPlease write something like file:///var/log/foobar instead.");
                System.exit(0);
            }

            try {
                Main.createPID(setup);
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getLocalizedMessage());
                System.exit(0);
            } catch (IOException ex) {
                System.err.println("I/O Exception occurred: " + ex.getLocalizedMessage());
                System.exit(0);
            }

            new Main().logKeeperDaemon(setup);
        }
    }
}

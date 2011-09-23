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

import de.suse.logkeeper.db.EmbeddedDBServer;
import de.suse.logkeeper.db.EmbeddedH2DbServer;
import java.net.Socket;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

/**
 * Local web server that listens only on a localhost
 * from the components on the local host.
 * 
 * @author bo
 */
class LocalWebServer extends WebServer {
    public LocalWebServer(int port) {
        super(port);
    }


    @Override
    protected boolean allowConnection(Socket s) {
        return s.getInetAddress().getHostName().equals("localhost");
    }
}



/**
 * Log keeper daemon main class.
 * 
 * @author bo
 */
public class LogKeeperDaemon {
    public static final int DEFAULT_HTTP_PORT = 6888;

    private int port;
    private LocalWebServer webServer;
    private PropertyHandlerMapping handleMap;
    private XmlRpcServerConfigImpl config;
    private EmbeddedDBServer dBServer;


    /**
     * Log keeper daemon.
     *
     * @param setup
     * @throws SQLException
     * @throws URISyntaxException
     * @throws ClassNotFoundException
     * @throws Exception
     */
    public LogKeeperDaemon(Properties setup)
            throws SQLException,
                   URISyntaxException,
                   ClassNotFoundException,
                   Exception {

        if (setup.getProperty("backend.db.type", "single").toLowerCase().equals("multi")) {
            this.dBServer = new EmbeddedH2DbServer();
            this.dBServer.setup(setup);
            this.dBServer.start();
        }

        this.port = Integer.parseInt(setup.getProperty("server.port", LogKeeperDaemon.DEFAULT_HTTP_PORT + ""));
        this.webServer = new LocalWebServer(this.port);
        this.handleMap = new PropertyHandlerMapping();
        this.webServer.getXmlRpcServer().setHandlerMapping(this.handleMap);
        this.config = (XmlRpcServerConfigImpl) this.webServer.getXmlRpcServer().getConfig();

        MessageDispatcherFactory.init(setup);
        ServiceGate.init(setup);
    }


    public LogKeeperDaemon addHandler(String handler, Class cls) throws XmlRpcException {
        this.handleMap.addHandler(handler == null ? cls.getName() : handler, cls);
        return this;
    }


    public XmlRpcServerConfigImpl getConfig() {
        return this.config;
    }


    public void start() throws Exception {
        this.webServer.start();
    }
}

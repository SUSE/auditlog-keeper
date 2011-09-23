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


package de.suse.logkeeper.db;

import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.tools.Server;

/**
 * Embedded H2 server, which allows concurrent connections only on localhost.
 * 
 * @author bo
 */
public class EmbeddedH2DbServer implements EmbeddedDBServer {
    private Server server;

    public EmbeddedH2DbServer() {}


    /**
     * Setup H2 server before start.
     * This method is mandatory to call before starting the server.
     * 
     * @param config
     */
    public void setup(Properties config) {
        this.stop();
        try {
            this.server = Server.createTcpServer(new String[]
                    {"-tcpPort", config.getProperty("backend.db.port", EmbeddedDBServer.DEFAULT_TCP_PORT + "")});
        } catch (SQLException ex) {
            Logger.getLogger(EmbeddedH2DbServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Stops an embedded H2 server.
     * @return
     */
    public boolean stop() {
        if (this.server != null && this.server.isRunning(false)) {
            this.server.stop();
        }

        return this.server != null ? !this.server.isRunning(false) : false;
    }


    /**
     * Starts an embedded H2 server.
     * @return
     */
    public boolean start() {
        if (this.server != null && !this.server.isRunning(false)) {
            try {
                this.server.start();
            } catch (SQLException ex) {
                Logger.getLogger(EmbeddedH2DbServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return this.server != null ? this.server.isRunning(false) : false;
    }
}

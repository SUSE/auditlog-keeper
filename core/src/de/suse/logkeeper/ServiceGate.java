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
import de.suse.logkeeper.service.LogKeeperService;
import de.suse.logkeeper.service.LogKeeperServiceH2;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Properties;


/**
 *
 * @author bo
 */
public class ServiceGate {
    private static ServiceGate instance;
    private LogKeeperService service;


    /**
     * Constructor of the service gate.
     * At the moment only H2 is supported and this is long-term solution.
     * Maybe HSQLDB or Derby in a future as well, if somebody would demand.
     * 
     * @param config
     * @throws Exception
     */
    private ServiceGate(Properties config) throws Exception {
        String db = config.getProperty("backend.db", "h2").toLowerCase();
        Properties dbInitConfig = new Properties();
        dbInitConfig.put("databases", db);
        if (config.getProperty("backend.db.type", "single").toLowerCase().equals("multi")) {
            dbInitConfig.put(db + ".url", String.format("jdbc:h2:tcp://localhost:%s/%s",
                                                        config.getProperty("backend.db.port", EmbeddedDBServer.DEFAULT_TCP_PORT + ""),
                                                        config.getProperty("backend.db.location", "/var/log/suse/manager/auditlog")));
        } else {
            dbInitConfig.put(db + ".url", "jdbc:" + db + "://-" + config.getProperty("backend.db.location", "/var/log/suse/manager/auditlog"));
        }
        dbInitConfig.put(db + ".user", config.getProperty("backend.db.auth.user", ""));
        dbInitConfig.put(db + ".password", config.getProperty("backend.db.auth.password", ""));

        if (db.equals("h2")) {
            this.service = new LogKeeperServiceH2(dbInitConfig);
        } else {
            throw new Exception("Database vendor\"" + db + "\" is not supported.");
        }
    }


    public static void init(Properties config) throws SQLException, URISyntaxException, ClassNotFoundException, Exception {
        if (ServiceGate.instance == null) {
            synchronized (ServiceGate.class) {
                ServiceGate.instance = new ServiceGate(config);
            }
        }
    }


    public static ServiceGate getInstance() throws Exception {
        if (ServiceGate.instance != null) {
            return instance;
        } else {
            throw new Exception("Service Gate is not initialized.");
        }
    }


    public LogKeeperService getService() {
        return this.service;
    }
}

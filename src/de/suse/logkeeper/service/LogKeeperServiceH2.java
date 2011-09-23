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


package de.suse.logkeeper.service;

import de.suse.lib.sqlmap.SQLMapper;
import de.suse.logkeeper.service.entities.LogEntry;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation for H2 embedded database.
 * @author bo
 */
public class LogKeeperServiceH2 implements LogKeeperService {
    private static final int BATCH_SIZE = 5;

    private SQLMapper mapper;
    private static Map<String, Integer> levels;


    /**
     * Quick DB creation. This database is not protected.
     *
     * @param databaseFileUrl
     * @throws SQLException
     * @throws URISyntaxException
     * @throws ClassNotFoundException
     * @throws Exception
     */
    public LogKeeperServiceH2(URL databaseFileUrl) 
            throws SQLException,
                   URISyntaxException,
                   ClassNotFoundException,
                   Exception {
        Properties config = new Properties();
        config.put("databases", "h2");
        config.put("h2.url", "jdbc:h2://-" + databaseFileUrl.getPath());
        config.put("h2.url", databaseFileUrl.getPath());
        config.put("h2.user", "");
        config.put("h2.password", "");

        this.init(config);
    }


    public LogKeeperServiceH2(Properties config)
            throws SQLException,
                   URISyntaxException,
                   ClassNotFoundException,
                   Exception {
        this.init(config);
    }


    /**
     * Connect to the database with the params.
     *
     * @param config
     * @throws SQLException
     * @throws URISyntaxException
     * @throws ClassNotFoundException
     * @throws Exception
     */
    private void init(Properties config)
            throws SQLException,
                   URISyntaxException,
                   ClassNotFoundException,
                   Exception {
        this.mapper = new SQLMapper(config).setResourceClass(LogKeeperService.class).connect("h2");

        // Test or init db.
        Map<String, String> params = new HashMap<String, String>();
        params.put("limit", "0");
        params.put("offset", "0");
        try {
            this.mapper.close(this.mapper.call("de.suse.logkeeper.service.sql.h2.getLogEntries", params));
        } catch (SQLException ex) {
            try {
                this.mapper.call("de.suse.logkeeper.service.sql.h2.initDb", params);
            } catch (Exception fatalException) {
                System.err.println("Error initializing database: " + fatalException.getLocalizedMessage());
                System.exit(1);
            }
        }
    }


    private Long allocLogId() throws SQLException, Exception {
        ResultSet result = this.mapper.call("de.suse.logkeeper.service.sql.h2.allocLogId", null);
        Long id = null;
        if (result.next()) {
            id = result.getLong("id");
        }

        this.mapper.close(result);

        return id;
    }

    /**
     * Add a log entry.
     * 
     * @param entry
     * @throws SQLException
     * @throws Exception
     */
    public Long log(LogEntry entry) throws SQLException, Exception {
        Long id = this.allocLogId();
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());
        params.put("facility", entry.getFacility());
        params.put("level", entry.getLevel().toString());
        params.put("message", entry.getMessage());
        params.put("userid", entry.getUserName());
        params.put("ipaddr", entry.getNode().getHostAddress());
        params.put("extmap", entry.renderExtMap());

        this.mapper.call("de.suse.logkeeper.service.sql.h2.log", params);

        return id;
    }


    public void rotate() throws SQLException, Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * Export log to the file.
     *
     * @param destination
     * @param format
     * @throws SQLException
     * @throws Exception
     */
    public synchronized void exportToFile(File destination, int format)
            throws SQLException,
                   IOException,
                   Exception {
        if (destination.exists()) {
            throw new IOException(String.format("File %s already exists.", destination.getAbsolutePath()));
        }

        ResultSet result = null;
        Writer out = new BufferedWriter(new FileWriter(destination));
        out.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<auditlog>");
        try {
            Long offset = 0L;
            Map<String, String> params = new HashMap<String, String>();
            params.put("limit", LogKeeperServiceH2.BATCH_SIZE + "");
            params.put("offset", offset.toString());

            // Get log by chunks, not entire into the memory.
            result = this.mapper.call("de.suse.logkeeper.service.sql.h2.getLogEntriesCount", null);
            result.next();
            Long logSize = result.getLong("amt");
            this.mapper.close(result);

            while (logSize > 0) {
                result = this.mapper.call("de.suse.logkeeper.service.sql.h2.getLogEntries", params);
                offset += LogKeeperServiceH2.BATCH_SIZE;
                logSize -= LogKeeperServiceH2.BATCH_SIZE;
                params.put("offset", offset.toString());
                while (result.next()) {
                    out.write("<entry>");
                    out.write(String.format("<id>%s</id>", result.getLong("id")));
                    out.write(String.format("<facility>%s</facility>", result.getString("facility")));
                    out.write(String.format("<level>%s</level>", result.getLong("level")));
                    out.write(String.format("<message>%s</message>", result.getString("message")));
                    out.write(String.format("<userid>%s</userid>", result.getString("userid")));
                    out.write(String.format("<ipaddr>%s</ipaddr>", result.getString("ipaddr")));
                    out.write(String.format("<created>%s</created>", result.getTimestamp("created")));
                    out.write(String.format("<extmap><!CDATA[%s]]></extmap>", result.getString("extmap")));
                    out.write("</entry>");
                }
            }
            out.write("</auditlog>");
        } finally {
            this.mapper.close(result);
            out.close();
        }
    }


    /**
     * Get log levels into the static map.
     * 
     * @return
     * @throws SQLIntegrityConstraintViolationException
     * @throws SQLException
     * @throws Exception
     */
    public Map<String, Integer> getLogLevels()
            throws SQLIntegrityConstraintViolationException,
                   SQLException,
                   Exception {
        if (LogKeeperServiceH2.levels == null) {
            synchronized (this) {
                LogKeeperServiceH2.levels = new HashMap<String, Integer>();
                ResultSet result = this.mapper.call("de.suse.logkeeper.service.sql.h2.getLogLevels", null);
                while (result.next()) {
                    LogKeeperServiceH2.levels.put(result.getString("description"), result.getInt("level"));
                }

                this.mapper.close(result);
            }
        }

        return LogKeeperServiceH2.levels;
    }


    /**
     * Get a message by an ID.
     *
     * @param id
     * @return
     * @throws SQLException
     * @throws Exception
     */
    public LogEntry get(Long id) throws SQLException, Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        LogEntry entry = null;
        ResultSet result = null;
        try {
            result = this.mapper.call("de.suse.logkeeper.service.sql.h2.getLogEntry", params);
            if (result.next()) {
                entry = new LogEntry(result.getLong("id"), result.getString("facility"),
                                     result.getInt("level"), result.getString("message"),
                                     result.getString("userid"), InetAddress.getByName(result.getString("ipaddr")),
                                     result.getTimestamp("created"), null).parseExtMap(result.getString("extmap"));
            }
        } finally {
            this.mapper.close(result);
        }

        return entry;
    }


    /**
     * Remove message by an ID.
     * 
     * @param id
     * @throws SQLException
     * @throws Exception
     */
    public void remove(Long id) throws SQLException, Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());
        this.mapper.call("de.suse.logkeeper.service.sql.h2.removeLogEntry", params);
    }
}

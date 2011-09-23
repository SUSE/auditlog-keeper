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

package de.suse.logkeeper.service.entities;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Log entry record.
 * 
 * @author bo
 */
public class LogEntry {
    public static final String ISO8601_MASK = "EEE, dd MMM yyyy HH:mm:ss z"; // yyyy-MM-dd'T'HH:mm:ssz <-- Atom

    private Long id;
    private String facility;
    private Integer level;
    private String message;
    private String userName;
    private InetAddress node;
    private Date timestamp;
    private Map<String, String> extmap;
    private final SimpleDateFormat iso8601Formatter;


    public LogEntry(Long id, String facility, Integer level, String message,
                    String userName, InetAddress node, Date timestamp, Map<String, String> extmap) {
        this.id = id;
        this.facility = facility;
        this.level = level;
        this.message = message;
        this.userName = userName;
        this.node = node;
        this.timestamp = timestamp;
        this.extmap = extmap;
        this.iso8601Formatter = new SimpleDateFormat(LogEntry.ISO8601_MASK);
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the facility
     */
    public String getFacility() {
        return facility;
    }

    /**
     * @param facility the facility to set
     */
    public void setFacility(String facility) {
        this.facility = facility;
    }

    /**
     * @return the level
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(Integer level) {
        this.level = level;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the node
     */
    public InetAddress getNode() {
        return node;
    }

    /**
     * @param node the node to set
     */
    public void setNode(InetAddress node) {
        this.node = node;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the extmap
     */
    public Map<String, String> getExtmap() {
        return extmap;
    }

    /**
     * @param extmap the extmap to set
     */
    public void setExtmap(Map<String, String> extmap) {
        this.extmap = extmap;
    }


    /**
     * Render an extmap out of the mapping.
     * @return
     */
    public String renderExtMap() {
        StringBuilder map = new StringBuilder();
        for (String key : this.getExtmap().keySet()) {
            map.append(key).append("=").append(this.extmap.get(key).replaceAll("\n", "")).append("\n");
        }

        return map.toString();
    }


    /**
     * Parses a rendered map.
     * @param map
     */
    public LogEntry parseExtMap(String map) throws Exception {
        String[] lines = map.split("\n");
        if (lines.length < 1) {
            throw new Exception("Can not parse extmap.");
        }

        this.extmap = new HashMap<String, String>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equals("")) {
                continue;
            }

            String[] tokens = line.trim().split("=", 2);
            this.extmap.put(tokens[0], tokens[1]);
        }

        return this;
    }


    /**
     * Get ISO 8601 timestamp.
     * 
     * @return
     */
    public String getISO8601Timestamp() {
        return this.iso8601Formatter.format(this.getTimestamp());
    }
}

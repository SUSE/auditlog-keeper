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


package com.suse.logkeeper.plugins;

import de.suse.logkeeper.plugins.LogKeeperBackend;
import de.suse.logkeeper.plugins.LogKeeperBackendException;
import de.suse.logkeeper.service.entities.LogEntry;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XML output
 * @author bo
 */
public class XMLLog implements LogKeeperBackend {
    private static final String ROOT_TAG = "auditlog";
    private static final Integer DEFAULT_ROTATION_SIZE = 10; // MB
    private SimpleDateFormat backupFileDateFormat;

    private Integer rotation;
    private URL url;

    /**
     * XML Log setup.
     * 
     * @param definition
     * @param setup
     * @throws LogKeeperBackendException
     */
    public void setup(String definition, Properties setup) throws LogKeeperBackendException {
        this.backupFileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            this.rotation = Integer.parseInt(setup.getProperty("plugin." + definition + ".size",
                                                               XMLLog.DEFAULT_ROTATION_SIZE.toString()));
        } catch (Exception ex) {
            Logger.getLogger(XMLLog.class.getName()).log(Level.WARNING, "WARN: Using default rotation size of {0}MB.", XMLLog.DEFAULT_ROTATION_SIZE);
            this.rotation = XMLLog.DEFAULT_ROTATION_SIZE;
        }

        try {
            this.url = new URL(setup.getProperty("plugin." + definition + ".url"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(XMLLog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Init the XML.
     * 
     * @throws LogKeeperBackendException
     */
    private void init() throws LogKeeperBackendException {
        try {
            File file = new File(this.url.toURI());
            if (file.length() == 0 || (file.length() / 1024 / 1024) > this.rotation) {
                if (file.length() > 0) {
                    file.renameTo(new File(file.getAbsolutePath() + "." + this.backupFileDateFormat.format(new Date())));
                    file = new File(this.url.toURI());
                }

                RandomAccessFile writer = new RandomAccessFile(file, "rw");
                writer.writeBytes("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
                writer.writeBytes(String.format("<%s></%s>", XMLLog.ROOT_TAG, XMLLog.ROOT_TAG));
                writer.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(XMLLog.class.getName()).log(Level.SEVERE, null, ex);
            throw new LogKeeperBackendException(ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(XMLLog.class.getName()).log(Level.SEVERE, null, ex);
            throw new LogKeeperBackendException(ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(XMLLog.class.getName()).log(Level.SEVERE, null, ex);
            throw new LogKeeperBackendException(ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLLog.class.getName()).log(Level.SEVERE, null, ex);
            throw new LogKeeperBackendException(ex);
        }
    }


    /**
     * Send log message.
     * 
     * @param entry
     * @throws LogKeeperBackendException
     */
    public synchronized void log(LogEntry entry) throws LogKeeperBackendException {
        this.init();
        RandomAccessFile writer = null;
        try {
            File file = new File(this.url.toURI());
            writer = new RandomAccessFile(file, "rw");
            writer.seek(file.length() - ("</" + XMLLog.ROOT_TAG + ">").length());
            writer.writeBytes("<entry>");
            writer.writeBytes(String.format("<id>%s</id>", entry.getId()));
            writer.writeBytes(String.format("<facility>%s</facility>", entry.getFacility()));
            writer.writeBytes(String.format("<level>%s</level>", entry.getLevel()));
            writer.writeBytes(String.format("<message><![CDATA[%s]]></message>", entry.getMessage()));
            writer.writeBytes(String.format("<userid>%s</userid>", entry.getUserName()));
            writer.writeBytes(String.format("<ipaddr>%s</ipaddr>", entry.getNode().getHostAddress()));
            writer.writeBytes(String.format("<created>%s</created>", entry.getISO8601Timestamp()));
            writer.writeBytes(String.format("<extmap><![CDATA[%s]]></extmap>", entry.renderExtMap()));
            writer.writeBytes("</entry>");
            writer.writeBytes(String.format("</%s>", XMLLog.ROOT_TAG));
        } catch (URISyntaxException ex) {
            Logger.getLogger(XMLLog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLLog.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(XMLLog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

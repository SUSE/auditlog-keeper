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

import de.suse.logkeeper.plugins.LogKeeperBackend;
import de.suse.logkeeper.plugins.LogKeeperBackendException;
import de.suse.logkeeper.plugins.SpecValidator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Message dispatcher factory.
 *
 * @author bo
 */
public class MessageDispatcherFactory {
    private Properties setup;
    private static MessageDispatcherFactory instance;
    private Map<String, LogKeeperBackend> plugins;
    private Map<String, SpecValidator> validators;


    private MessageDispatcherFactory(Properties setup) {
        this.setup = setup;
        this.plugins = new HashMap<String, LogKeeperBackend>();
        this.validators = new HashMap<String, SpecValidator>();

        // Load plugins
        String[] moduleIds = setup.getProperty("plugin.available", "").replaceAll(" ", "").split(",");
        for (int i = 0; i < moduleIds.length; i++) {
            String bkndId = moduleIds[i];
            String className = setup.getProperty("plugin." + bkndId + ".entry", "");
            if (!className.equals("")) {
                try {
                    this.plugins.put(bkndId, (LogKeeperBackend) Class.forName(className).newInstance());
                    this.plugins.get(bkndId).setup(bkndId, setup);
                } catch (LogKeeperBackendException ex) {
                    Logger.getLogger(MessageDispatcherFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    System.err.println("Can not start " + bkndId + " plugin: " + ex.getLocalizedMessage());
                } catch (IllegalAccessException ex) {
                    System.err.println("Access error to " + bkndId + " plugin: " + ex.getLocalizedMessage());
                } catch (ClassNotFoundException ex) {
                    System.err.println("Plugin " + bkndId + " not found: " + ex.getLocalizedMessage());
                }
            }
        }

        // Load validators
        moduleIds = setup.getProperty("validator.available", "").replaceAll(" ", "").split(",");
        for (int i = 0; i < moduleIds.length; i++) {
            String bkndId = moduleIds[i];
            String className = setup.getProperty("validator." + bkndId + ".entry", "");
            if (!className.equals("")) {
                try {
                    this.validators.put(bkndId, (SpecValidator) Class.forName(className).newInstance());
                } catch (InstantiationException ex) {
                    System.err.println("Can not boot " + bkndId + " validator: " + ex.getLocalizedMessage());
                } catch (IllegalAccessException ex) {
                    System.err.println("Access error to " + bkndId + " validator: " + ex.getLocalizedMessage());
                } catch (ClassNotFoundException ex) {
                    System.err.println("Plugin " + bkndId + " not found: " + ex.getLocalizedMessage());
                }
            }
        }
    }


    /**
     * Initialize message dispatcher factory.
     *
     * @param setup
     */
    public static void init(Properties setup) {
        if (MessageDispatcherFactory.instance == null) {
            synchronized (MessageDispatcherFactory.class) {
                MessageDispatcherFactory.instance = new MessageDispatcherFactory(setup);
            }
        }
    }


    /**
     * Get a message dispatcher instance.
     *
     * @return
     */
    public static MessageDispatcher getDispatcher() {
        return new MessageDispatcher(MessageDispatcherFactory.instance.plugins,
                                     MessageDispatcherFactory.instance.validators);
    }
}

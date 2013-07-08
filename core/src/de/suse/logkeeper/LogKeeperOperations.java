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

import de.suse.logkeeper.service.entities.LogEntry;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bo
 */
public class LogKeeperOperations {
    /**
     * Return the current {@link Date} for indicating status 'alive'.
     *
     * @return current date
     */
    public Date ping() {
        return new Date();
    }

    /**
     * Add a log message.
     * 
     * @param uid
     * @param message
     * @return
     */
    public int log(String uid, String message, String host) throws UnknownHostException {
        return this.log(uid, message, host, new HashMap());
    }


    /**
     * Add a log message.
     *
     * @param uid
     * @param message
     * @param extmap
     * @return
     */
    public int log(String uid, String message, String host, Map<String, String> extmap) throws UnknownHostException {
        try {
            MessageDispatcherFactory.getDispatcher().log(new LogEntry(null, "audit", 1, message, uid, InetAddress.getByName(host), null, extmap));
            return 1;
        } catch (Exception ex) {
            Logger.getLogger(LogKeeperOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }
}

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
import de.suse.logkeeper.service.entities.LogEntry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


class DispatcherWorker implements Runnable {
    private Thread workerThread;
    private int timeout; // seconds
    private MessageDispatcher dispatcher;
    private Thread containerThread;


    public DispatcherWorker(Runnable workerTask, int timeout, MessageDispatcher dispatcher) {
        this.workerThread = new Thread(workerTask);
        this.workerThread.setDaemon(true);
        this.timeout = timeout * 1000;
        this.dispatcher = dispatcher;
    }


    public void run() {
        this.dispatcher.incWorker(this.containerThread);
        this.workerThread.start();
        boolean lock = true;
        int timelapse = 0;
        while (lock) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(DispatcherWorker.class.getName()).log(Level.SEVERE, null, ex);
            }

            timelapse++;

            if ((lock = (this.workerThread != null && this.workerThread.isAlive())) && timelapse > this.timeout) {
                this.workerThread.interrupt();
                // ToDO report here timeout
            }
        }
        this.dispatcher.decWorker(this.containerThread.getName());
    }


    public void setContainerThread(Thread container) {
        this.containerThread = container;
    }
}


/**
 * Message dispatcher. It receives a message from the database and sends it
 * to all required destinations.
 *
 * @author bo
 */
public class MessageDispatcher {
    public static final int DEFAULT_TIMEOUT = 10; // seconds
    private Map<String, LogKeeperBackend> plugins;
    private Map<String, SpecValidator> validators;
    private Map<String, Thread> workers;


    public MessageDispatcher(Map<String, LogKeeperBackend> plugins, Map<String, SpecValidator> validators) {
        this.plugins = plugins;
        this.validators = validators;
        this.workers = new ConcurrentHashMap<String, Thread>();
    }


    public void log(final LogEntry entry) throws Exception {
        if (this.validate(entry)) {
            Thread process = new Thread(new Runnable() {

                public void run() {
                    try {
                        Long id = ServiceGate.getInstance().getService().log(entry);
                        if (id != null) {
                            MessageDispatcher.this.dispatch(ServiceGate.getInstance().getService().get(id));
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(MessageDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            process.setDaemon(true);
            process.start();
        } else {
            throw new Exception(String.format("Entry has invalid extmap: %s", this.inspect(entry)));
        }
    }


    private boolean validate(LogEntry entry) {
        for (String validatorKey : this.validators.keySet()) {
            if (!this.validators.get(validatorKey).validate(entry.getExtmap())) {
                return false;
            }
        }

        return true;
    }


    private String inspect(LogEntry entry) {
        for (String validatorKey : this.validators.keySet()) {
            List<String> inspection = this.validators.get(validatorKey).inspect(entry.getExtmap());
            if (!inspection.isEmpty()) {
                return inspection.toString();
            }
        }

        return "";
    }


    private void dispatch(final LogEntry entry) {
        // Send message to all configured destinations
        // Policy: at least one should succeed.
        for (final String bkndId : this.plugins.keySet()) {
            DispatcherWorker worker = new DispatcherWorker(new Runnable() {
                public void run() {
                    try {
                        MessageDispatcher.this.plugins.get(bkndId).log(entry);
                    } catch (LogKeeperBackendException ex) {
                        Logger.getLogger(MessageDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }, MessageDispatcher.DEFAULT_TIMEOUT, this);
            
            Thread messageDepositProcess = new Thread(worker);
            worker.setContainerThread(messageDepositProcess);
            messageDepositProcess.setDaemon(true);
            messageDepositProcess.start();
        }

        try {
            int counter = 0;
            int timeout = (MessageDispatcher.DEFAULT_TIMEOUT + 1) * 1000; // Emergency timeout
            while (!this.workers.isEmpty()) {
                Thread.sleep(1);
                counter++;
                if (counter > timeout) {
                    // Emergency hara-kiru
                    for (String key : this.workers.keySet()) {
                        this.decWorker(key);
                    }
                }
            }

            ServiceGate.getInstance().getService().remove(entry.getId());
        } catch (Exception ex) {
            Logger.getLogger(MessageDispatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    protected synchronized void decWorker(String name) {
        Thread thread = null;
        try {
            thread = this.workers.get(name);
        } catch (Exception ex) {
            Logger.getLogger(MessageDispatcher.class.getName()).log(Level.WARNING, "No worker for {0} - {1}", new Object[]{name, ex.getLocalizedMessage()});
        }

        if (thread != null) {
            if (thread.isAlive()) {
                thread.interrupt();
            }

            this.workers.remove(name);
        }
    }


    protected synchronized void incWorker(Thread thread) {
        this.workers.put(thread.getName(), thread);
    }
}

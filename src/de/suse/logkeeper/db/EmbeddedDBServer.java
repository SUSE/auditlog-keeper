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

import java.util.Properties;

/**
 *
 * @author bo
 */
public interface EmbeddedDBServer {
    public static final int DEFAULT_TCP_PORT = 6868;

    public void setup(Properties config);
    public boolean stop();
    public boolean start();
}

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

import de.suse.logkeeper.service.entities.LogEntry;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * LogKeeper service interface.
 *
 * @author bo
 */
public interface LogKeeperService {
    // Export formats
    public static final int FORMAT_XML = 0;
    public static final int FORMAT_TEXT = 0;

    public Long log(LogEntry entry) throws SQLException, Exception;
    public LogEntry get(Long id) throws SQLException, Exception;
    public void remove(Long id) throws SQLException, Exception;
    public void rotate() throws SQLException, Exception;
    public void exportToFile(File destination, int format) throws SQLException, IOException, Exception;
}

/*
 * Copyright 2017 lorislab.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package liquibase.ext.wildfly.service;

import liquibase.ext.wildfly.config.Config;
import java.io.File;
import liquibase.changelog.OfflineChangeLogHistoryService;
import liquibase.database.Database;
import liquibase.ext.wildfly.database.WildflyDatabase;
import liquibase.servicelocator.LiquibaseService;

/**
 *
 * @author Andrej Petras
 */
@LiquibaseService(skip = false)
public class FileChangeLogHistoryService extends OfflineChangeLogHistoryService {

    public FileChangeLogHistoryService(Database database, File file) {
        super(database, file, false, false);
    }
    
    public FileChangeLogHistoryService() {
        super(null, Config.getChangeLogHistoryFile(), false, false);
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof WildflyDatabase;
    }

}

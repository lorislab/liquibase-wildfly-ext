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
package liquibase.ext.wildfly.config;

import java.io.File;
import java.io.IOException;
import liquibase.exception.UnexpectedLiquibaseException;

/**
 *
 * @author Andrej Petras
 */
public class Config {
   
    public static final String CONFIG_BASE = "liquibase.wildfly";

    private Config() {
    }

    public static File getChangeLogHistoryFile() {
        String fileProperty = System.getProperty(CONFIG_BASE + ".file", "./databasechangelog.csv");
        try {
            return new File(fileProperty).getCanonicalFile();
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public static String getLockProperty() {
        return System.getProperty(CONFIG_BASE + ".property", CONFIG_BASE + ".lock");
    }

}

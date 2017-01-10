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
        return System.getProperty(CONFIG_BASE + ".property", "liquibase.wildfly.lock");
    }

}

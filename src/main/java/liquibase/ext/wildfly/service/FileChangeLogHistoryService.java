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

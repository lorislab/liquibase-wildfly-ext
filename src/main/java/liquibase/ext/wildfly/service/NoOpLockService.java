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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.ext.wildfly.config.Config;
import liquibase.ext.wildfly.database.WildflyDatabase;
import liquibase.ext.wildfly.database.WildflyDatabaseConnection;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author Andrej Petras
 */
public class NoOpLockService implements LockService {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");

    private WildflyDatabase database;

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof WildflyDatabase;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = (WildflyDatabase) database;
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
    }

    @Override
    public boolean hasChangeLogLock() {
        ModelNode node = lock("read-resource");
        String outcome = node.get("outcome").asString();
        if (outcome.equals("success")) {
            return true;
        }
        return false;
    }

    @Override
    public void waitForLock() throws LockException {
        boolean tmp = acquireLock();
        if (!tmp) {
            String lockedBy = "UNKNOWN";
            DatabaseChangeLogLock[] locks = listLocks();
            if (locks != null && locks.length > 0) {
                lockedBy = locks[0].getLockedBy();
            }
            throw new LockException("Could not acquire change log lock.  Currently locked by " + lockedBy);
        }

    }

    @Override
    public boolean acquireLock() throws LockException {
        String author = System.getProperty("user.name");
        String date = SDF.format(new Date());
        ModelNode node = lock("add(value=" + date + "#" + author + ")");
        String outcome = node.get("outcome").asString();
        if (outcome.equals("success")) {
            return true;
        }
        return false;
    }

    @Override
    public void releaseLock() throws LockException {
        ModelNode node = lock("remove");
        String outcome = node.get("outcome").asString();
        if (outcome.equals("failed")) {
            String value = node.get("failure-description").asString();
            throw new RuntimeException("Could not release lock! Error: " + value);
        }
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        ModelNode node = lock("read-resource");
        String outcome = node.get("outcome").asString();
        if (outcome.equals("success")) {
            String value = node.get("result").asObject().get("value").asString();
            String[] tmp = value.split("#");
            String user = tmp[1];

            Date date = null;
            try {
                date = SDF.parse(tmp[0]);
            } catch (ParseException ex) {
                // ok
            }
            return new DatabaseChangeLogLock[]{
                new DatabaseChangeLogLock(1, date, user)
            };
        } 
        return new DatabaseChangeLogLock[0];
    }

    @Override
    public void forceReleaseLock() throws LockException, DatabaseException {
        releaseLock();
    }

    @Override
    public void reset() {

    }

    @Override
    public void init() throws DatabaseException {

    }

    @Override
    public void destroy() throws DatabaseException {

    }

    private ModelNode lock(String method) {
        WildflyDatabaseConnection con = (WildflyDatabaseConnection) database.getConnection();
        return con.processDomainProfileCommand("/system-property=" + Config.getLockProperty() + ":" + method);
    }
}

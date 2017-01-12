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
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

/**
 * The wildfly lock service.
 * 
 * @author Andrej Petras
 */
public class WildflyLockService implements LockService {

    /**
     * The lock value separator.
     */
    private static final String SEPARATOR = "#";
    
    /**
     * The date format for the date key in the lock value.
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");

    /**
     * The wildfly server.
     */
    private WildflyDatabase database;
    
    /**
     * {@inheritDoc }
     */
    @Override
    public int getPriority() {
        return 1000;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean supports(Database database) {
        return database instanceof WildflyDatabase;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void setDatabase(Database database) {
        this.database = (WildflyDatabase) database;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean hasChangeLogLock() {
        ModelNode node = lock(ClientConstants.READ_RESOURCE_OPERATION);
        if (Operations.isSuccessfulOutcome(node)) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc }
     */
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
    
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean acquireLock() throws LockException {
        String author = System.getProperty("user.name");
        String date = SDF.format(new Date());
        
        StringBuilder sb = new StringBuilder();
        sb.append(ClientConstants.ADD).append('(');
        sb.append(ClientConstants.VALUE).append('=');
        sb.append(date).append(SEPARATOR);
        sb.append(author).append(')');
        
        ModelNode node = lock(sb.toString());
        
        if (Operations.isSuccessfulOutcome(node)) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void releaseLock() throws LockException {
        ModelNode node = lock(ClientConstants.REMOVE_OPERATION);
        if (!Operations.isSuccessfulOutcome(node)) {            
            String value = Operations.getFailureDescription(node).asString();
            throw new RuntimeException("Could not release lock! Error: " + value);
        }
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        ModelNode node = lock(ClientConstants.READ_RESOURCE_OPERATION);
        if (Operations.isSuccessfulOutcome(node)) {            
            String value =  node.get(ClientConstants.RESULT).asObject().get(ClientConstants.VALUE).asString();
            String[] tmp = value.split(SEPARATOR);
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
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void forceReleaseLock() throws LockException, DatabaseException {
        releaseLock();
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void reset() {

    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void init() throws DatabaseException {

    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void destroy() throws DatabaseException {

    }

    private ModelNode lock(String method) {
        WildflyDatabaseConnection con = (WildflyDatabaseConnection) database.getConnection();
        return con.processDomainProfileCommand("/system-property=" + Config.getLockProperty() + ":" + method);
    }
}

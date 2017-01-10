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
package liquibase.ext.wildfly.precodition;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.logging.LogFactory;
import liquibase.precondition.AbstractPrecondition;
import liquibase.serializer.ReflectionSerializer;

/**
 * The max id check precondition for field and value.
 * 
 * @author Andrej Petras
 */
public class MaxCheckPrecondition extends AbstractPrecondition {

    /**
     * The expected id.
     */
    private String expectedId;
    
    /**
     * The change field.
     */
    private String field;
    
    /**
     * The default id.
     */
    private String defaultId;
    
    /**
     * The field value.
     */
    private String value;

    public String getDefaultId() {
        return defaultId;
    }

    public void setDefaultId(String defaultId) {
        this.defaultId = defaultId;
    }

    public String getExpectedId() {
        return expectedId;
    }

    public void setExpectedId(String expectedId) {
        this.expectedId = expectedId;
    }    

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return "maxCheck";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        try {
            String result = null;
            List<String> data = new LinkedList<>();
            data.add(getDefaultId());

            ChangeLogHistoryService service = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
            List<RanChangeSet> changes = service.getRanChangeSets();
            if (changes != null) {

                for (RanChangeSet change : changes) {
                    Object tmp = ReflectionSerializer.getInstance().getValue(change, field);
                    if (tmp != null && tmp.equals(value)) {
                        data.add(change.getId());
                    }
                }
            }

            if (!data.isEmpty()) {
                Collections.sort(data, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2) * -1;
                    }
                });
                result = data.get(0);
            }
            
            if (result == null) {
                LogFactory.getInstance().getLog().severe("No rows returned from SQL Precondition");                
                throw new PreconditionFailedException("No rows returned from SQL Precondition", changeLog, this);
            }

            String expectedResult = getExpectedId();
            if (!expectedResult.equals(result)) {
                LogFactory.getInstance().getLog().severe("SQL Precondition failed.  Expected id '" + expectedResult + "' got '" + result + "'");
                throw new PreconditionFailedException("SQL Precondition failed.  Expected id '" + expectedResult + "' got '" + result + "'", changeLog, this);
            }
        } catch (PreconditionFailedException ee) {
            throw ee;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/dbchangelog-ext/liquibase-wildfly";
    }

}

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
package liquibase.executor.jvm;

import java.util.List;
import java.util.Map;
import liquibase.exception.DatabaseException;
import liquibase.executor.AbstractExecutor;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

/**
 *
 * @author Andrej Petras
 */
@LiquibaseService(skip = true)
public class JdbcExecutor extends AbstractExecutor {

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType) throws DatabaseException {
        return null;
    } 

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return null;
    }

    @Override
    public long queryForLong(SqlStatement sql) throws DatabaseException {
        return 0;
    }

    @Override
    public long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return 0;
    }

    @Override
    public int queryForInt(SqlStatement sql) throws DatabaseException {
        return 0;
    }

    @Override
    public int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return 0;
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType) throws DatabaseException {
        return null;
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return null;
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql) throws DatabaseException {
        return null;
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return null;
    }

    @Override
    public void execute(SqlStatement sql) throws DatabaseException {
        // empty method
    }

    @Override
    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        // empty method
    }

    @Override
    public int update(SqlStatement sql) throws DatabaseException {
        return 0;
    }

    @Override
    public int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return 0; 
    }

    @Override
    public void comment(String message) throws DatabaseException {
        // empty method
    }

    @Override
    public boolean updatesDatabase() {
        return false;
    }
    
}

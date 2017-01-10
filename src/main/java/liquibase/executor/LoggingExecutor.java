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
package liquibase.executor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.ext.wildfly.statement.CliStatement;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.ExecutablePreparedStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.GetNextChangeSetSequenceValueStatement;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.util.StreamUtil;

@LiquibaseService(skip = true)
public class LoggingExecutor extends AbstractExecutor {

    private Writer output;
    
    private Executor delegatedReadExecutor;

    public LoggingExecutor(Executor delegatedExecutor, Writer output, Database database) {
        if (output != null) {
            this.output = output;
        } else {
            this.output = new NoopWriter();
        }
        this.delegatedReadExecutor = delegatedExecutor;
        setDatabase(database);
    }

    protected Writer getOutput() {
        return output;
    }

    @Override
    public void execute(SqlStatement sql) throws DatabaseException {
        outputStatement(sql);
    }

    @Override
    public int update(SqlStatement sql) throws DatabaseException {
        outputStatement(sql);

        if (sql instanceof LockDatabaseChangeLogStatement) {
            return 1;
        } else if (sql instanceof UnlockDatabaseChangeLogStatement) {
            return 1;
        }
        return 0;
    }

    @Override
    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
    }

    @Override
    public int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        outputStatement(sql, sqlVisitors);
        return 0;
    }

    @Override
    public void comment(String message) throws DatabaseException {
        try {
            output.write(database.getLineComment());
            output.write(" ");
            output.write(message);
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    private void outputStatement(SqlStatement sql) throws DatabaseException {
        outputStatement(sql, new ArrayList<SqlVisitor>());
    }

    private void outputStatement(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        try {
            if (SqlGeneratorFactory.getInstance().generateStatementsVolatile(sql, database)) {
                throw new DatabaseException(sql.getClass().getSimpleName() + " requires access to up to date database metadata which is not available in SQL output mode");
            }
            if (sql instanceof ExecutablePreparedStatement) {
                output.write("WARNING: This statement uses a prepared statement which cannot be execute directly by this script. Only works in 'update' mode\n\n");
            }

            CliStatement cli = (CliStatement) sql;
            for (String statement : cli.getCli()) {
                output.write(statement);
                output.write(StreamUtil.getLineSeparator());
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType) throws DatabaseException {
        if (sql instanceof SelectFromDatabaseChangeLogLockStatement) {
            return (T) Boolean.FALSE;
        }
        return delegatedReadExecutor.queryForObject(sql, requiredType);
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForObject(sql, requiredType, sqlVisitors);
    }

    @Override
    public long queryForLong(SqlStatement sql) throws DatabaseException {
        return delegatedReadExecutor.queryForLong(sql);
    }

    @Override
    public long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForLong(sql, sqlVisitors);
    }

    @Override
    public int queryForInt(SqlStatement sql) throws DatabaseException {
        try {
            return delegatedReadExecutor.queryForInt(sql);
        } catch (DatabaseException e) {
            if (sql instanceof GetNextChangeSetSequenceValueStatement) { //table probably does not exist
                return 0;
            }
            throw e;
        }
    }

    @Override
    public int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForInt(sql, sqlVisitors);
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, elementType);
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, elementType, sqlVisitors);
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql);
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        return delegatedReadExecutor.queryForList(sql, sqlVisitors);
    }

    @Override
    public boolean updatesDatabase() {
        return false;
    }

    private class NoopWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            // does nothing
        }

        @Override
        public void flush() throws IOException {
            // does nothing
        }

        @Override
        public void close() throws IOException {
            // does nothing
        }

    }

}

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

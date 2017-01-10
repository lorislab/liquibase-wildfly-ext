/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package liquibase.ext.wildfly.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.ext.wildfly.database.WildflyDatabaseConnection;
import liquibase.ext.wildfly.statement.CliStatement;
import liquibase.logging.LogFactory;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

/**
 *
 * @author Andrej Petras
 */
public class WildflyExecutor extends JdbcExecutor {

    protected Database database;

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long queryForLong(SqlStatement sql) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int queryForInt(SqlStatement sql) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Map<String, ?>> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void execute(Change change) throws DatabaseException {
        execute(change, new ArrayList<>());
    }

    @Override
    public void execute(Change change, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        SqlStatement[] sqlStatements = change.generateStatements(database);
        if (sqlStatements != null) {
            for (SqlStatement statement : sqlStatements) {
                execute(statement, sqlVisitors);
            }
        }
    }

    @Override
    public void execute(SqlStatement sql) throws DatabaseException {
        execute(sql, new ArrayList<>());
    }

    @Override
    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        WildflyDatabaseConnection conn = (WildflyDatabaseConnection) database.getConnection();
        CliStatement cli = (CliStatement) sql;
        conn.processCommands(cli.getCli());
    }

    @Override
    public int update(SqlStatement sql) throws DatabaseException {
        return update(sql, new ArrayList<>());
    }

    @Override
    public int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        WildflyDatabaseConnection conn = (WildflyDatabaseConnection) database.getConnection();
        CliStatement cli = (CliStatement) sql;
        conn.processCommands(cli.getCli());
        return 1;
    }

    @Override 
    public void comment(String message) throws DatabaseException {
        LogFactory.getLogger().debug(message);
    }

    @Override
    public boolean updatesDatabase() {
        return true;
    }

}

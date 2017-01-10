package liquibase.ext.wildfly.database;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.DateParseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.util.StreamUtil;
import liquibase.ext.wildfly.jdbc.WildflyConnection;

/**
 *
 * @author Andrej Petras
 */
public class WildflyDatabase implements Database {

    private WildflyDatabaseConnection connection;
    private String catalogName;
    private String schemaName;
    private String timeFunction;
    private ObjectQuotingStrategy quotingStrategy;
    private String databaseChangeLogTableName;
    private String databaseChangeLogLockTableName;
    private String tablespaceName;
    private boolean outputDefaultCatalog;
    
    /**
     * @see liquibase.database.Database#getDatabaseChangeLogTableName()
     */
    @Override
    public String getDatabaseChangeLogTableName() {
        if (databaseChangeLogTableName != null) {
            return databaseChangeLogTableName;
        }

        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDatabaseChangeLogTableName();
    }

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogLockTableName()
     */
    @Override
    public String getDatabaseChangeLogLockTableName() {
        if (databaseChangeLogLockTableName != null) {
            return databaseChangeLogLockTableName;
        }

        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDatabaseChangeLogLockTableName();
    }
    
    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "wildfly".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("wildfly")) {
            return "liquibase.ext.wildfly.jdbc.WildflyDriver";
        }
        return null;        
    }

    @Override
    public DatabaseConnection getConnection() {
        return connection;
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        JdbcConnection c = (JdbcConnection) conn;
        WildflyConnection wc = (WildflyConnection) c.getWrappedConnection();
        this.connection = new WildflyDatabaseConnection(wc);
    }

    @Override
    public boolean requiresUsername() {
        return false;
    }

    @Override
    public boolean requiresPassword() {
        return false;
    }

    @Override
    public boolean getAutoCommitMode() {
        return false;
    }

    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

    @Override
    public String getDatabaseProductName() {
        return "Wildfly";
    }

    @Override
    public String getDatabaseProductVersion() throws DatabaseException {
        return connection.getDatabaseProductVersion();
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        return connection.getDatabaseMajorVersion();
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        return connection.getDatabaseMinorVersion();
    }

    @Override
    public String getShortName() {
        return "wildfly";
    }

    @Override
    public String getDefaultCatalogName() {
        return catalogName;
    }

    @Override
    public void setDefaultCatalogName(String catalogName) throws DatabaseException {
        this.catalogName = catalogName;
    }

    @Override
    public String getDefaultSchemaName() {
        return schemaName;
    }

    @Override
    public void setDefaultSchemaName(String schemaName) throws DatabaseException {
        this.schemaName = schemaName;
    }

    @Override
    public Integer getDefaultPort() {
        return 9990;
    }

    @Override
    public Integer getFetchSize() {
        return 0;
    }

    @Override
    public String getLiquibaseCatalogName() {
        return catalogName;
    }

    @Override
    public void setLiquibaseCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public String getLiquibaseSchemaName() {
        return schemaName;
    }

    @Override
    public void setLiquibaseSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }

    @Override
    public String getDateLiteral(String isoDate) {
        return isoDate;
    }

    @Override
    public String getCurrentDateTimeFunction() {
        return timeFunction;
    }

    @Override
    public void setCurrentDateTimeFunction(String function) {
        this.timeFunction = function;
    }

    @Override
    public String getLineComment() {
        return "# ";
    }

    @Override
    public String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy) {
        return "";
    }

    @Override
    public String getLiquibaseTablespaceName() {
        return tablespaceName;
    }

    @Override
    public void setLiquibaseTablespaceName(String tablespaceName) {
        this.tablespaceName = tablespaceName;
    }

    @Override
    public void setDatabaseChangeLogTableName(String tableName) {
        this.databaseChangeLogTableName = tableName;
    }

    @Override
    public void setDatabaseChangeLogLockTableName(String tableName) {
        this.databaseChangeLogLockTableName = tableName;
    }

    @Override
    public String getConcatSql(String... values) {
        return "";
    }

    @Override
    public void setCanCacheLiquibaseTableInfo(boolean canCacheLiquibaseTableInfo) {
        // empty method
    }

    @Override
    public void dropDatabaseObjects(CatalogAndSchema schema) throws LiquibaseException {
        // empty method
    }

    @Override
    public void tag(String tagString) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).tag(tagString);
    }

    @Override
    public boolean doesTagExist(String tag) throws DatabaseException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).tagExists(tag);
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        return true;
    }

    @Override
    public boolean isLiquibaseObject(DatabaseObject object) {
        return true;
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String name) throws DatabaseException {
        return "";
    }

    @Override
    public String getDateLiteral(Date date) {
        return "";
    }

    @Override
    public String getTimeLiteral(Time time) {
        return "";
    }

    @Override
    public String getDateTimeLiteral(Timestamp timeStamp) {
        return "";
    }


    @Override
    public String escapeObjectName(String catalogName, String schemaName, String objectName, Class<? extends DatabaseObject> objectType) {
        return "";
    }

    @Override
    public String escapeTableName(String catalogName, String schemaName, String tableName) {
        return "";
    }

    @Override
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        return "";
    }

    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return "";
    }

    @Override
    public String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName) {
        return "";
    }

    @Override
    public String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName, boolean quoteNamesThatMayBeFunctions) {
        return "";
    }

    @Override
    public String escapeColumnNameList(String columnNames) {
        return columnNames;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsCatalogs() {
        return false;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return false;
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        return "";
    }

    @Override
    public String escapeSequenceName(String catalogName, String schemaName, String sequenceName) {
        return "";
    }

    @Override
    public String escapeViewName(String catalogName, String schemaName, String viewName) {
        return "";
    }

    @Override
    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRunStatus(changeSet);
    }

    @Override
    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanChangeSet(changeSet);
    }

    @Override
    public void markChangeSetExecStatus(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).setExecType(changeSet, execType);
    }

    @Override
    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanChangeSets();
    }

    @Override
    public Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanDate(changeSet);
    }

    @Override
    public void removeRanStatus(ChangeSet changeSet) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).removeFromHistory(changeSet);
    }

    @Override
    public void commit() throws DatabaseException {
        try {
            getConnection().commit();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void rollback() throws DatabaseException {
        try {
            getConnection().rollback();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public String escapeStringForDatabase(String string) {
        return "";
    }

    @Override
    public void close() throws DatabaseException {
        connection.close();
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public String escapeConstraintName(String constraintName) {
        return "";
    }

    @Override
    public boolean isAutoCommit() throws DatabaseException {
        try {
            return getConnection().getAutoCommit();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setAutoCommit(boolean b) throws DatabaseException {
        try {
            getConnection().setAutoCommit(b);
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean isSafeToRunUpdate() throws DatabaseException {
        DatabaseConnection connection = getConnection();
        if (connection == null) {
            return true;
        }
        String url = connection.getURL();
        if (url == null) {
            return false;
        }
        return (url.contains("localhost")) || (url.contains("127.0.0.1"));
    }

    @Override
    public void executeStatements(Change change, DatabaseChangeLog changeLog, List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        SqlStatement[] statements = change.generateStatements(this);

        execute(statements, sqlVisitors);
    }

    @Override
    public void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        for (SqlStatement statement : statements) {
            if (statement.skipOnUnsupported() && !SqlGeneratorFactory.getInstance().supports(statement, this)) {
                continue;
            }
            LogFactory.getLogger().debug("Executing Statement: " + statement);
            try {
                ExecutorService.getInstance().getExecutor(this).execute(statement, sqlVisitors);
            } catch (DatabaseException e) {
                if (statement.continueOnError()) {
                    LogFactory.getLogger().severe("Error executing statement '"+statement.toString()+"', but continuing", e);
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public void saveStatements(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        SqlStatement[] statements = change.generateStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(StreamUtil.getLineSeparator()).append(StreamUtil.getLineSeparator());
            }
        }
    }

    @Override
    public void executeRollbackStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, RollbackImpossibleException {
        final SqlStatement[] statements = change.generateRollbackStatements(this);
        executeRollbackStatements(statements, sqlVisitors);
    }

    @Override
    public void executeRollbackStatements(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException, RollbackImpossibleException {
        execute(statements, filterRollbackVisitors(sqlVisitors));
    }

    @Override
    public void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        SqlStatement[] statements = change.generateRollbackStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(StreamUtil.getLineSeparator()).append(StreamUtil.getLineSeparator());;
            }
        }
    }

    @Override
    public java.util.Date parseDate(String dateAsString) throws DateParseException {
        return null;
    }

    @Override
    public List<DatabaseFunction> getDateFunctions() {
        return null;
    }

    @Override
    public void resetInternalState() {
        
    }

    @Override
    public boolean supportsForeignKeyDisable() {
        return false;
    }

    @Override
    public boolean disableForeignKeyChecks() throws DatabaseException {
        return false;
    }

    @Override
    public void enableForeignKeyChecks() throws DatabaseException {
        
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }

    @Override
    public boolean isReservedWord(String string) {
        return false;
    }

    @Override
    public CatalogAndSchema correctSchema(CatalogAndSchema schema) {
        return null;
    }

    @Override
    public String correctObjectName(String name, Class<? extends DatabaseObject> objectType) {
        return "";
    }

    @Override
    public boolean isFunction(String string) {
        return false;
    }

    @Override
    public int getDataTypeMaxParameters(String dataTypeName) {
        return -1;
    }

    @Override
    public CatalogAndSchema getDefaultSchema() {
        return null;
    }

    @Override
    public boolean dataTypeIsNotModifiable(String typeName) {
        return false;
    }

    @Override
    public String generateDatabaseFunctionValue(DatabaseFunction databaseFunction) {
        return "";
    }

    @Override
    public void setObjectQuotingStrategy(ObjectQuotingStrategy quotingStrategy) {
        this.quotingStrategy = quotingStrategy;
    }

    @Override
    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return quotingStrategy;
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return false;
    }

    @Override
    public void setOutputDefaultSchema(boolean outputDefaultSchema) {
        // empty method
    }

    @Override
    public boolean getOutputDefaultSchema() {
        return false;
    }

    @Override
    public boolean isDefaultSchema(String catalog, String schema) {
        return false;
    }

    @Override
    public boolean isDefaultCatalog(String catalog) {
        return false;
    }

    @Override
    public boolean getOutputDefaultCatalog() {
        return outputDefaultCatalog;
    }

    @Override
    public void setOutputDefaultCatalog(boolean outputDefaultCatalog) {
        this.outputDefaultCatalog = outputDefaultCatalog;
    }

    @Override
    public boolean supportsPrimaryKeyNames() {
        return false;
    }

    @Override
    public String getSystemSchema() {
        return "";
    }

    @Override
    public void addReservedWords(Collection<String> words) {
    }

    @Override
    public String escapeDataTypeName(String dataTypeName) {
        return "";
    }

    @Override
    public String unescapeDataTypeName(String dataTypeName) {
        return "";
    }

    @Override
    public String unescapeDataTypeString(String dataTypeString) {
        return "";
    }

    @Override
    public ValidationErrors validate() {
        ValidationErrors result = new ValidationErrors();
        return result;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public String getDateLiteral(java.sql.Date date) {
        return "";
    }
    
    protected List<SqlVisitor> filterRollbackVisitors(final List<SqlVisitor> visitors) {
        final List<SqlVisitor> rollbackVisitors = new ArrayList<SqlVisitor>();
        if (visitors != null) {
            for (SqlVisitor visitor : visitors) {
               if (visitor.isApplyToRollback()) {
                   rollbackVisitors.add(visitor);
               }
            }
        }
        return rollbackVisitors;
    }    
        
    public boolean isCliProperties() {
        return connection.isCliProperties();
    }
    
    public Properties getCliProperties() {        
        return connection.getCliProperties();
    }

}

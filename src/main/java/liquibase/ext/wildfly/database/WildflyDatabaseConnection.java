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
package liquibase.ext.wildfly.database;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.ext.wildfly.config.Config;
import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.impl.CommandContextConfiguration;
import liquibase.ext.wildfly.jdbc.WildflyConnection;
import liquibase.logging.LogFactory;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.scriptsupport.CLI;
import org.jboss.dmr.ModelNode;

/**
 * The wildfly server connection.
 * 
 * @author Andrej Petras
 */
public class WildflyDatabaseConnection implements DatabaseConnection {

    private String url;

    private Properties info;

    private String domainProfile;

    private Properties cliProperties;

    private CommandContext cmdCtx = null;

    private String username;
    
    private int exitCode = 0;

    public WildflyDatabaseConnection(WildflyConnection con) {
        this.url = con.getUrl();
        this.info = con.getInfo();
        
        // load domain profile
        domainProfile = info.getProperty(Config.CONFIG_BASE + ".domain.profile", "");

        // load CLI properties
        String cliPropertyFile = info.getProperty(Config.CONFIG_BASE + ".properties");                
        if (cliPropertyFile != null) {
            LogFactory.getLogger().info("Load CLI properties: " + cliPropertyFile);
            cliProperties = new Properties();
            try (InputStream in = Files.newInputStream(Paths.get(cliPropertyFile))) {
                cliProperties.load(in);
            } catch (Exception e) {
                LogFactory.getLogger().severe(e.getMessage(), e);
            }
        }
        
        // open connection
        String tmp = null;
        if (url != null) {
            tmp = url.substring(8);
        }
        if (tmp.startsWith("embed-server")) {
            this.exitCode = connectEmbedded(tmp);
        } else {
            this.exitCode = connect(tmp);
        }
        
    }

    public String getDomainProfile() {
        return domainProfile;
    }

    public boolean isCliProperties() {
        return cliProperties != null;
    }

    public Properties getCliProperties() {
        return cliProperties;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public void close() throws DatabaseException {
        closeSession();
    }

    @Override
    public void commit() throws DatabaseException {

    }

    @Override
    public boolean getAutoCommit() throws DatabaseException {
        return false;
    }

    @Override
    public String getCatalog() throws DatabaseException {
        return null;
    }

    @Override
    public String nativeSQL(String string) throws DatabaseException {
        return null;
    }

    @Override
    public void rollback() throws DatabaseException {

    }

    @Override
    public void setAutoCommit(boolean bln) throws DatabaseException {
        // empty method
    }

    @Override
    public String getDatabaseProductName() throws DatabaseException {
        return "wildfly";
    }

    @Override
    public String getDatabaseProductVersion() throws DatabaseException {
        return "1";
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        return 1;
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        return 0;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String getConnectionUserName() {
        return username;
    }

    @Override
    public boolean isClosed() throws DatabaseException {
        return cmdCtx.isTerminated();
    }

    @Override
    public void attached(Database dtbs) {
        // empty method
    }

    private int closeSession() {
        if ((cmdCtx != null)) {
            cmdCtx.terminateSession();
            return cmdCtx.getExitCode();
        }
        return 0;
    }

    private int connectEmbedded(String command) {
         LogFactory.getLogger().info("Start wildfly server in the embedded mode: " + command);
        int result = 0;                
        try {
            
            System.setProperty("java.util.logging.manager","org.jboss.logmanager.LogManager");
            
            CLI c = CLI.newInstance();
            cmdCtx = c.getCommandContext();
            c.cmd(command);
        } catch (Throwable t) {
            LogFactory.getLogger().severe(Util.getMessagesFromThrowable(t));
            result = 1;
        }
        return result;
    }

    private int connect(String controller) {
        LogFactory.getLogger().info("Connect to remote server: " + controller);
        int result = 0;

        final CommandContextConfiguration.Builder ctxBuilder = new CommandContextConfiguration.Builder();
        ctxBuilder.setErrorOnInteract(false);

        if (controller != null && !controller.isEmpty()) {
            ctxBuilder.setController(controller);
        }
        String user = info.getProperty("username");
        if (user != null && !user.isEmpty()) {
            username = user;
            ctxBuilder.setUsername(user);
            ctxBuilder.setDisableLocalAuth(true);
        }
        String password = info.getProperty("password");
        if (password != null && !password.isEmpty()) {
            ctxBuilder.setPassword(password.toCharArray());
        }

        String nolocalauth = info.getProperty(Config.CONFIG_BASE + ".no-local-auth");
        if (nolocalauth != null && nolocalauth.equals(Boolean.TRUE.toString())) {
            ctxBuilder.setDisableLocalAuth(true);
        }
        String erroroninteract = info.getProperty(Config.CONFIG_BASE + ".error-on-interact");
        if (erroroninteract != null && erroroninteract.equals(Boolean.TRUE.toString())) {
            ctxBuilder.setErrorOnInteract(true);
        }
        String bind = info.getProperty(Config.CONFIG_BASE + ".bind");
        if (bind != null && !bind.isEmpty()) {
            ctxBuilder.setClientBindAddress(bind);
        }

        int connectionTimeout = -1;
        String tmp = info.getProperty(Config.CONFIG_BASE + ".timeout");
        if (tmp != null && !tmp.isEmpty()) {
            try {
                connectionTimeout = Integer.parseInt(tmp);
            } catch (final NumberFormatException e) {
                //
            }
        }

        ctxBuilder.setConnectionTimeout(connectionTimeout);

        try {
            CommandContextConfiguration ctx = ctxBuilder.build();
            cmdCtx = CommandContextFactory.getInstance().newCommandContext(ctx);
            try {
                cmdCtx.connectController();
            } catch (CommandLineException e) {
                throw new CliInitializationException("Failed to connect to the controller", e);
            }
        } catch (Throwable t) {
            LogFactory.getLogger().severe(Util.getMessagesFromThrowable(t));
            result = 1;
        }
        return result;
    }

    public void processCommands(List<String> commands) {
        int i = 0;
        try {
            while (checkStatus() && i < commands.size()) {
                cmdCtx.handleSafe(commands.get(i));
                ++i;
            }
        } catch (Exception ex) {
            LogFactory.getLogger().severe(ex.getMessage(), ex);
        }
    }

    public ModelNode processCommand(String command) {
        ModelNode result = null;
        if (checkStatus()) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos)) {
                cmdCtx.captureOutput(ps);
                cmdCtx.handle(command);
                cmdCtx.releaseOutput();
                String tmp = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                result = ModelNode.fromString(tmp);
            } catch (Exception ex) {
                LogFactory.getLogger().debug(ex.getMessage(), ex);
                result = ModelNode.fromString(ex.getMessage());
            }
        }
        return result;
    }

    public ModelNode processDomainProfileCommand(String command) {
        return processCommand(domainProfile + command);
    }

    public boolean checkStatus() {
        return cmdCtx.getExitCode() == 0 && !cmdCtx.isTerminated();
    }
}

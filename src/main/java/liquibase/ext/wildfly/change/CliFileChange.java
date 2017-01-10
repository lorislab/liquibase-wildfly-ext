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
package liquibase.ext.wildfly.change;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.ext.wildfly.database.WildflyDatabase;
import liquibase.ext.wildfly.statement.CliStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

/**
 *
 * @author Andrej Petras
 */
@DatabaseChange(
        name = "cli",
        description = "Wildfly configuration file",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CliFileChange extends AbstractChange {

    private final static Pattern LTRIM = Pattern.compile("\\s+$");

    private final String ENDCODING_DEFAULT = "UTF-8";
    private String encoding = ENDCODING_DEFAULT;
    private String path;
    private Boolean relativeToChangelogFile;

    @Override
    public String getConfirmationMessage() {
        return "Wildfly cli file " + getPath() + " executed";
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof WildflyDatabase;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> statments = new LinkedList<>();

        Properties prop = ((WildflyDatabase) database).getCliProperties();
        if (prop == null) {
            prop = new Properties();
        }

        List<String> cli = new LinkedList<>();
        CliStatement cliStatment = new CliStatement(cli);
        String charset = Optional.ofNullable(encoding).orElse(ENDCODING_DEFAULT);
        try (InputStream sqlStream = openSqlStream()) {
            if (sqlStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(sqlStream, charset))) {
                    String line = reader.readLine();
                    while (line != null) {
                        line = LTRIM.matcher(line).replaceAll("");
                        line = ExpressionValidator.expandExpressions(line, prop, false);
                        cli.add(line);
                        line = reader.readLine();
                    }
                }
            }
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        statments.add(cliStatment);

        return statments.toArray(new SqlStatement[statments.size()]);
    }

    @Override
    public void finishInitialization() throws SetupException {
        if (path == null) {
            throw new SetupException("<clifile> - No path specified");
        }
    }

    @DatabaseChangeProperty(description = "Is relative to the change log file")
    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    @DatabaseChangeProperty(description = "The file path of the wildfly-cli file to load", requiredForDatabase = "all", exampleValue = "my/path/file.cli")
    public String getPath() {
        return path;
    }

    /**
     * Sets the file name but setUp must be called for the change to have
     * impact.
     *
     * @param fileName The file to use
     */
    public void setPath(String fileName) {
        path = fileName;
    }

    /**
     * The encoding of the file containing SQL statements
     *
     * @return the encoding
     */
    @DatabaseChangeProperty(exampleValue = "UTF-8")
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public InputStream openSqlStream() throws IOException {
        if (path == null) {
            return null;
        }

        InputStream inputStream = null;
        try {
            inputStream = StreamUtil.openStream(path, isRelativeToChangelogFile(), getChangeSet(), getResourceAccessor());
        } catch (IOException e) {
            throw new IOException("Unable to read file '" + path + "'", e);
        }
        if (inputStream == null) {
            throw new IOException("File does not exist: '" + path + "'");
        }
        return inputStream;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (StringUtils.trimToNull(getPath()) == null) {
            validationErrors.addError("'path' is required");
        }
        return validationErrors;
    }

}

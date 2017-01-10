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
package liquibase.ext.wildfly.sqlgenerator;

import java.util.ArrayList;
import java.util.List;
import liquibase.ext.wildfly.statement.CliStatement;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.ext.wildfly.database.WildflyDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

/**
 *
 * @author Andrej Petras
 */
public class CliSqlGenerator implements SqlGenerator<CliStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(CliStatement st, Database dtbs) {
        return dtbs instanceof WildflyDatabase;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database dtbs) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(Database dtbs) {
        return false;
    }

    @Override
    public ValidationErrors validate(CliStatement st, Database dtbs, SqlGeneratorChain sgc) {
        ValidationErrors validationErrors = new ValidationErrors();
        return validationErrors;
    }

    @Override
    public Warnings warn(CliStatement st, Database dtbs, SqlGeneratorChain sgc) {
        return null;
    }

    @Override
    public Sql[] generateSql(CliStatement st, Database dtbs, SqlGeneratorChain sgc) {
        List<Sql> sqls = new ArrayList<>(st.size());
        for (String item : st.getCli()) {
            sqls.add(new UnparsedSql(item));
        }
        return sqls.toArray(new Sql[sqls.size()]);
    }


}

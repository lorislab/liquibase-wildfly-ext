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
package liquibase.ext.wildfly.statement;

import java.util.ArrayList;
import java.util.List;
import liquibase.statement.SqlStatement;

/**
 *
 * @author Andrej Petras
 */
public class CliStatement implements SqlStatement {

    private List<String> cli = new ArrayList<>();

    public CliStatement(List<String> cli) {
        this.cli = cli;
    }

    public List<String> getCli() {
        return cli;
    }
           
    public int size() {
        return cli.size();
    }
    
    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

    @Override
    public boolean continueOnError() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CliStatment: [\n");
        for (String item : cli) {
            sb.append('\t').append(item).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
    
    
}

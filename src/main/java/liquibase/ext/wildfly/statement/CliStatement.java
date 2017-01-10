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

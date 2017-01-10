package liquibase.ext.wildfly.change;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Andrej Petras
 */
public class ExpressionValidator {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(\\$\\{[^\\}]+\\})");

    public static String expandExpressions(String text, Properties properties, boolean enableEscaping) {
        if (text == null) {
            return null;
        }
        Matcher matcher = EXPRESSION_PATTERN.matcher(text);
        String originalText = text;
        while (matcher.find()) {
            String expressionString = originalText.substring(matcher.start(), matcher.end());
            String valueTolookup = expressionString.replaceFirst("\\$\\{", "").replaceFirst("\\}$", "");

            boolean tmp = enableEscaping && valueTolookup.startsWith(":");
            if (!tmp) {
                Object value = properties.get(valueTolookup);

                if (value != null) {
                    text = text.replace(expressionString, value.toString());
                } else {
                    throw new RuntimeException("Missing property [" + valueTolookup + "] in the CLI properties!" );
                }
            }
        }

        // replace all escaped expressions with its literal
        if (enableEscaping) {
            text = text.replaceAll("\\$\\{:(.+?)}", "\\$\\{$1}");
        }

        return text;
    }

}

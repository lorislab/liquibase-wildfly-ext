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

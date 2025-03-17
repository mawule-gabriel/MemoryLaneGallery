package org.example.Template;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEngine {
    // Simple context class to hold template variables
    public static class Context {
        private final Map<String, Object> variables;

        public Context() {
            this.variables = new HashMap<>();
        }

        public void put(String key, Object value) {
            variables.put(key, value);
        }

        public Object get(String key) {
            return variables.get(key);
        }

        public boolean has(String key) {
            return variables.containsKey(key);
        }
    }

    // Render a template with the given context
    public static String render(String templatePath, Context context) {
        try {
            // Load template from classpath
            InputStream inputStream = TemplateEngine.class.getClassLoader().getResourceAsStream(templatePath);
            if (inputStream == null) {
                throw new IOException("Template not found: " + templatePath);
            }

            // Read template content
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder template = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                template.append(line).append("\n");
            }

            // Process template
            String result = processTemplate(template.toString(), context);

            return result;
        } catch (Exception e) {
            System.err.println("Error rendering template: " + e.getMessage());
            return "Error rendering template: " + e.getMessage();
        }
    }

    // Process the template with the given context
    private static String processTemplate(String template, Context context) {
        // Process variable substitutions {{ variable }}
        Pattern varPattern = Pattern.compile("\\{\\{\\s*([^}]+)\\s*\\}\\}");
        Matcher varMatcher = varPattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (varMatcher.find()) {
            String variable = varMatcher.group(1).trim();
            Object value = context.get(variable);
            String replacement = (value != null) ? value.toString() : "";
            varMatcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        varMatcher.appendTail(result);

        // Process for loops {% for item in items %}...{% endfor %}
        template = result.toString();
        Pattern forPattern = Pattern.compile("\\{%\\s*for\\s+([^\\s]+)\\s+in\\s+([^\\s]+)\\s*%\\}(.*?)\\{%\\s*endfor\\s*%\\}", Pattern.DOTALL);
        Matcher forMatcher = forPattern.matcher(template);
        result = new StringBuffer();

        while (forMatcher.find()) {
            String itemName = forMatcher.group(1).trim();
            String listName = forMatcher.group(2).trim();
            String loopContent = forMatcher.group(3);

            if (context.has(listName) && context.get(listName) instanceof List) {
                List<?> items = (List<?>) context.get(listName);
                StringBuilder loopResult = new StringBuilder();

                for (Object item : items) {
                    // Create a new context for each iteration
                    Context loopContext = new Context();
                    // Copy all variables from parent context
                    for (Map.Entry<String, Object> entry : context.variables.entrySet()) {
                        loopContext.put(entry.getKey(), entry.getValue());
                    }
                    // Add the loop variable
                    loopContext.put(itemName, item);

                    // Process the loop content with the loop context
                    loopResult.append(processTemplate(loopContent, loopContext));
                }

                forMatcher.appendReplacement(result, Matcher.quoteReplacement(loopResult.toString()));
            } else {
                forMatcher.appendReplacement(result, "");
            }
        }
        forMatcher.appendTail(result);

        // Process if statements {% if condition %}...{% endif %}
        template = result.toString();
        Pattern ifPattern = Pattern.compile("\\{%\\s*if\\s+([^\\s]+)\\s*%\\}(.*?)(?:\\{%\\s*else\\s*%\\}(.*?))?\\{%\\s*endif\\s*%\\}", Pattern.DOTALL);
        Matcher ifMatcher = ifPattern.matcher(template);
        result = new StringBuffer();

        while (ifMatcher.find()) {
            String condition = ifMatcher.group(1).trim();
            String ifContent = ifMatcher.group(2);
            String elseContent = (ifMatcher.groupCount() > 2) ? ifMatcher.group(3) : "";

            if (elseContent == null) {
                elseContent = "";
            }

            boolean conditionMet = false;

            if (context.has(condition)) {
                Object value = context.get(condition);
                if (value instanceof Boolean) {
                    conditionMet = (Boolean) value;
                } else if (value instanceof String) {
                    conditionMet = !((String) value).isEmpty();
                } else if (value instanceof Number) {
                    conditionMet = ((Number) value).doubleValue() != 0;
                } else if (value instanceof List) {
                    conditionMet = !((List<?>) value).isEmpty();
                } else {
                    conditionMet = value != null;
                }
            }

            if (conditionMet) {
                ifMatcher.appendReplacement(result, Matcher.quoteReplacement(processTemplate(ifContent, context)));
            } else {
                ifMatcher.appendReplacement(result, Matcher.quoteReplacement(processTemplate(elseContent, context)));
            }
        }
        ifMatcher.appendTail(result);

        return result.toString();
    }
}
package org.guo.treesitter.query;

import java.util.Map;

public class QueryBuilder {
    
    public static String build(String template, Map<String, String> replacements) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        if (replacements == null || replacements.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // Simple replacement logic: {{key}} -> value
            result = result.replace("{{" + key + "}}", value);
        }
        return result;
    }
}

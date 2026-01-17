package ro.pub.cs.systems.eim.colocviu_api;

import java.util.List;

public class AutocompleteResult {
    private final String query;
    private final List<String> suggestions;

    public AutocompleteResult(String query, List<String> suggestions) {
        this.query = query;
        this.suggestions = suggestions;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public String getThirdOrNull() {
        if (suggestions == null || suggestions.size() < 3) return null;
        return suggestions.get(2);
    }
}


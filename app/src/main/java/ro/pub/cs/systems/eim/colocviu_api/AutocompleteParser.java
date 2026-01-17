package ro.pub.cs.systems.eim.colocviu_api;


import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import ro.pub.cs.systems.eim.colocviu_api.AutocompleteResult;

public class AutocompleteParser {

    // Raspuns tipic: ["cafea",["cafea","cafeaua",...],...]
    public AutocompleteResult parse(String rawJson) throws Exception {
        JSONArray root = new JSONArray(rawJson);

        String query = root.optString(0, "");
        JSONArray arr = root.optJSONArray(1);

        List<String> suggestions = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                suggestions.add(arr.optString(i, ""));
            }
        }

        return new AutocompleteResult(query, suggestions);
    }
}

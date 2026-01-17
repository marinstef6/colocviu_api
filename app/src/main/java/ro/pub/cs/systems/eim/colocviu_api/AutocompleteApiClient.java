package ro.pub.cs.systems.eim.colocviu_api;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AutocompleteApiClient {
    private static final String TAG = "AutocompleteApiClient";

    // https://www.google.com/complete/search?client=chrome&q=cafea
    private static final String ENDPOINT = "https://www.google.com/complete/search?client=chrome&q=";

    public String fetchRawResponse(String prefix) throws Exception {
        String encoded = URLEncoder.encode(prefix, "UTF-8");
        URL url = new URL(ENDPOINT + encoded);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        int code = conn.getResponseCode();
        Log.d(TAG, "HTTP code=" + code);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        conn.disconnect();

        return sb.toString();
    }
}


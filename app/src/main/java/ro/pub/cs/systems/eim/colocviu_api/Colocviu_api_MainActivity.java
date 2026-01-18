package ro.pub.cs.systems.eim.colocviu_api;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ro.pub.cs.systems.eim.colocviu_api.AutocompleteBroadcast;
import ro.pub.cs.systems.eim.colocviu_api.AutocompleteResult;
import ro.pub.cs.systems.eim.colocviu_api.AutocompleteApiClient;
import ro.pub.cs.systems.eim.colocviu_api.AutocompleteParser;

public class Colocviu_api_MainActivity extends AppCompatActivity {
    private static final String TAG = "PT02v1MainActivity";

    private EditText prefixEditText;
    private TextView resultTextView;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AutocompleteApiClient apiClient = new AutocompleteApiClient();
    private final AutocompleteParser parser = new AutocompleteParser();

    private final BroadcastReceiver autocompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!AutocompleteBroadcast.ACTION_AUTOCOMPLETE_READY.equals(intent.getAction())) return;

            String prefix = intent.getStringExtra(AutocompleteBroadcast.EXTRA_PREFIX);
            String joined = intent.getStringExtra(AutocompleteBroadcast.EXTRA_SUGGESTIONS_JOINED);
            String third = intent.getStringExtra(AutocompleteBroadcast.EXTRA_THIRD);

            StringBuilder ui = new StringBuilder();
            ui.append("Prefix: ").append(prefix).append("\n\n");
            ui.append("Rezultate (separate cu virgulă și newline):\n");
            ui.append(joined == null ? "(nimic)" : joined);
            ui.append("\n\nA 3-a intrare: ").append(third == null ? "(nu există)" : third);

           resultTextView.setText(ui.toString());
            //resultTextView.setText(third == null ? "(nu există)" : third);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colocviu_api_main);

        prefixEditText = findViewById(R.id.prefixEditText);
        resultTextView = findViewById(R.id.resultTextView);

        Button searchButton = findViewById(R.id.searchButton);
        Button openMapButton = findViewById(R.id.openMapButton);

        searchButton.setOnClickListener(v -> startAutocompleteFlow());
        openMapButton.setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(AutocompleteBroadcast.ACTION_AUTOCOMPLETE_READY);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(autocompleteReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(autocompleteReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(autocompleteReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void startAutocompleteFlow() {
        final String prefix = prefixEditText.getText().toString().trim();
        if (prefix.isEmpty()) {
            resultTextView.setText("Introdu un prefix.");
            return;
        }

        resultTextView.setText("Se caută...");

        executor.execute(() -> {
            try {
                // a) request + LogCat răspuns complet
                String raw = apiClient.fetchRawResponse(prefix);
                Log.d(TAG, "RAW RESPONSE: " + raw);

                // b) parse + LogCat a 3-a intrare
                AutocompleteResult res = parser.parse(raw);
                String third = res.getThirdOrNull();
                Log.d(TAG, "THIRD ENTRY: " + (third == null ? "null" : third));

                // format cerut: valori separate cu ",\n"
                String joined = joinWithCommaNewline(res.getSuggestions());

                // c) Broadcast + afișare în UI (via receiver)
                Intent b = new Intent(AutocompleteBroadcast.ACTION_AUTOCOMPLETE_READY);
                b.setPackage(getPackageName()); // ✅ important
                b.putExtra(AutocompleteBroadcast.EXTRA_PREFIX, prefix);
                b.putExtra(AutocompleteBroadcast.EXTRA_RAW, raw);
                b.putExtra(AutocompleteBroadcast.EXTRA_THIRD, third);
                b.putExtra(AutocompleteBroadcast.EXTRA_SUGGESTIONS_JOINED, joined);
                sendBroadcast(b);

            } catch (Exception e) {
                Log.e(TAG, "Error", e);

                Intent b = new Intent(AutocompleteBroadcast.ACTION_AUTOCOMPLETE_READY);
                b.setPackage(getPackageName()); // ✅ pune și aici

                b.putExtra(AutocompleteBroadcast.EXTRA_PREFIX, prefix);
                b.putExtra(AutocompleteBroadcast.EXTRA_SUGGESTIONS_JOINED, "Eroare: " + e.getMessage());
                b.putExtra(AutocompleteBroadcast.EXTRA_THIRD, (String) null);

                sendBroadcast(b);
            }
        });
    }

    private static String joinWithCommaNewline(List<String> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) sb.append(",\n");
        }
        return sb.toString();
    }
}

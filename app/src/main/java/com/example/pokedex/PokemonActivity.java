package com.example.pokedex;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView catchButtonView;
    private String url;
    private RequestQueue requestQueue;
    private boolean caught;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        url = getIntent().getStringExtra("url");
        Log.d("Pokedex", "URL: " + url);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type_1);
        type2TextView = findViewById(R.id.pokemon_type_2);
        catchButtonView = findViewById(R.id.catch_button);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        load();
    }

    public void load() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray typeEntries = response.getJSONArray("types");
                    String name = Util.capitalize(response.getString("name"));
                    if (preferences.contains(name)) {
                        caught = true;
                    }
                    nameTextView.setText(name);
                    setCaught();
                    numberTextView.setText(String.format(Locale.getDefault(), "#%03d", response.getInt("id")));


                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = Util.capitalize(typeEntry.getJSONObject("type").getString("name"));

                        if (slot == 1) {
                            type1TextView.setText(type);
                        } else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }

                } catch (JSONException e) {
                    Log.e("Pokedex", "Pokemon json error");
                }

            }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Pokedex", "Pokemon detail error");
            }
        }
        );

        requestQueue.add(request);
    }

    public void toggleCatch(View view) {
        caught = !caught;
        setCaught();

        editor.apply();
    }

    private void setCaught() {
        String name = nameTextView.getText().toString();
        if (caught) {
            catchButtonView.setText(R.string.release_button_name);
            editor.putBoolean(name, true);
        } else {
            catchButtonView.setText(R.string.catch_button_name);
            editor.remove(name);
        }
    }
}
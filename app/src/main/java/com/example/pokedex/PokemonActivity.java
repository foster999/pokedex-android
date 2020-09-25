package com.example.pokedex;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

public class PokemonActivity extends AppCompatActivity {
    private TextView catchButtonView;
    private ImageView spriteImageView;
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView descriptionTextView;
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
        catchButtonView = findViewById(R.id.catch_button);
        spriteImageView = findViewById(R.id.pokemon_sprite);
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type_1);
        type2TextView = findViewById(R.id.pokemon_type_2);
        descriptionTextView = findViewById(R.id.pokemon_description);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        load();
    }

    public void load() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadImage(response);
                loadText(response);
                loadDescription(response);
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

    private void loadImage(JSONObject response) {
        try {
            String imageUrl = response.getJSONObject("sprites")
                    .getString("front_default");
            new DownloadSpriteTask().execute(imageUrl);
        } catch (JSONException e) {
            Log.e("Pokedex", "Pokemon image JSON error");
        }
    }

    private void loadText(JSONObject response) {
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
                    type1TextView.setVisibility(View.VISIBLE);
                } else if (slot == 2) {
                    type2TextView.setText(type);
                    type2TextView.setVisibility(View.VISIBLE);
                }
            }

        } catch (JSONException e) {
            Log.e("Pokedex", "Pokemon text JSON error");
        }
    }

    private void loadDescription(JSONObject response) {
        String speciesUrl = "";
        try {
            speciesUrl = response.getJSONObject("species").getString("url");
        } catch (JSONException e) {
            Log.e("Pokedex", "Pokemon description JSON error");
        }
        Log.d("Pokedex", "Pokemon species URL: " + speciesUrl);
        if (!speciesUrl.equals("")) {
            JsonObjectRequest descriptionResponse = new JsonObjectRequest(Request.Method.GET, speciesUrl,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    JSONArray descriptions = null;
                    try {
                        descriptions = response.getJSONArray("flavor_text_entries");
                    } catch (JSONException e) {
                        Log.e("Pokedex", "Pokemon flavor text array JSON error");
                    }
                    for (int i = 0; i < descriptions.length(); i++) {
                        try {
                            if (descriptions.getJSONObject(i).getJSONObject("language").getString("name").equals("en")) {
                                String description = descriptions.getJSONObject(i).getString("flavor_text");
                                descriptionTextView.setText(description);
                                break;
                            }
                        } catch (JSONException e) {
                            Log.e("Pokedex", "Pokemon flavor text string JSON error");
                        }
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Pokedex", "Pokemon description request error");
                }
            }
            );

            requestQueue.add(descriptionResponse);
        }
    }

    private static class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            } catch (IOException e) {
                Log.e("Pokedex", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            spriteImageView.setImageBitmap(bitmap);
        }
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
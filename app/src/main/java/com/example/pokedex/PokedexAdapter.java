package com.example.pokedex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder>
        implements Filterable {
    public static class PokedexViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout containerView;
        private TextView textView;

        PokedexViewHolder(View view) {
            super(view);
            containerView = view.findViewById(R.id.pokedex_row);
            textView = view.findViewById(R.id.pokedex_row_text_view);

            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon current = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("url", current.getUrl());

                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    private List<Pokemon> pokemon = new ArrayList<>();
    private List<Pokemon> filtered = pokemon;
    private RequestQueue requestQueue;

    private SharedPreferences preferences;

    @SuppressLint("CommitPrefEdits")
    PokedexAdapter(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadPokemon();
    }

    private class PokemonFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d("Pokedex", "Filter constraint: " + constraint.toString());
            FilterResults results = new FilterResults();
            List<Pokemon> filteredPokemon = new ArrayList<>();

            for (Pokemon poke : pokemon) {
                if (poke.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                    filteredPokemon.add(poke);
                }
            }
            results.values = filteredPokemon;
            results.count = filteredPokemon.size();
            Log.d("Pokedex", "Filter size: " + String.format("%d", results.count));

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered = (List<Pokemon>) results.values;
            notifyDataSetChanged();
        }
    }

    public void loadPokemon() {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=151";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String name = Util.capitalize(result.getString("name"));
                        pokemon.add(new Pokemon(
                                        name,
                                        result.getString("url")
                                )
                        );
                    }

                    notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("Pokedex", "Json error");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Pokedex", "Pokemon list error");
            }
        }
        );

        requestQueue.add(request);
    }


    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pokedex_row, parent, false);
        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
        Pokemon current = filtered.get(position);

        holder.textView.setText(current.getName());
        if (preferences.contains(current.getName())) {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pokeball, 0);
        } else {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        holder.containerView.setTag(current);  // Can be anything to provide access from holder
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }
}

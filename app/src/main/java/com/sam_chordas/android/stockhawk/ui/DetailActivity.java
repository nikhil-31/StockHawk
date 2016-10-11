package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.network.VolleySingleton;
import com.sam_chordas.android.stockhawk.rest.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int position = extras.getInt("Position");
//            Toast.makeText(this, " From the main screen at position" + position, Toast.LENGTH_SHORT).show();
        }
        volleySingleton = volleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();
        sendJsonRequest();

    }


    private void sendJsonRequest() {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20=%20%22YHOO%22%20and%20startDate%20=%20%222009-09-11%22%20and%20endDate%20=%20%222010-03-10%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=",
                null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ArrayList<Stock> stocker = new ArrayList<>();
                try {
                    stocker.addAll(parseJSONResponse(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                String s = response.toString();
//                Toast.makeText(getApplicationContext(), " " + s, Toast.LENGTH_SHORT).show();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Check internet connection", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);

    }

    public ArrayList<Stock> parseJSONResponse(JSONObject response) throws JSONException {

        final String QUERY = "query";
        final String RESULTS = "results";
        final String QUOTE = "quote";
        final String SYMBOL = "Symbol";
        final String DATE = "Date";
        final String OPEN = "Open";
        final String HIGH = "High";
        final String LOW = "Low";

        ArrayList<Stock> data = new ArrayList<Stock>();

        if (response == null || response.length() == 0) {
            return data;
        }

        JSONObject query = response.getJSONObject(QUERY);
        JSONObject results = query.getJSONObject(RESULTS);
        JSONArray quote = results.getJSONArray(QUOTE);

        for (int i = 0; i < quote.length(); i++) {

            Stock current = new Stock();

            JSONObject jsonObject = quote.getJSONObject(i);

            current.setSymbol(jsonObject.getString(SYMBOL));
            current.setDate(jsonObject.getString(DATE));
            current.setOpen(jsonObject.getString(OPEN));
            current.setHigh(jsonObject.getString(HIGH));
            current.setLow(jsonObject.getString(LOW));

            data.add(current);

            String toast = jsonObject.getString(DATE);
            Toast.makeText(getApplicationContext(),"Symbol "+toast,Toast.LENGTH_SHORT).show();
        }
        return data;
    }

}

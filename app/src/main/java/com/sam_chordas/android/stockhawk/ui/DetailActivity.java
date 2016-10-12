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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.network.VolleySingleton;
import com.sam_chordas.android.stockhawk.rest.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;
    ArrayList<Stock> stocker = new ArrayList<>();
    ArrayList<YourData> data;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        volleySingleton = volleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();
        sendJsonRequest();

        data = new ArrayList<YourData>();
        LineChart chart = (LineChart) findViewById(R.id.linegraph);

        int[] x = {
                10,20,30,40,50
        };
        int[] y = {
                12, 83, 63, 15, 90
        };
        for(int i=0;i<5;i++){
            YourData s = new YourData(x[i],y[i]);
            data.add(s);
        }

        List<Entry> entries = new ArrayList<Entry>();

        for (YourData dat : data) {

            // turn your data into Entry objects
            entries.add(new Entry(dat.getX(), dat.getY()));
        }
        /*As a next step, you need to add the List<Entry> you created to a LineDataSet object.
        DataSet objects hold data which belongs together, and allow individual styling of that data.
        The below used "Label" has only a descriptive purpose and shows up in the Legend, if enabled.*/

        LineDataSet dataSet = new LineDataSet(entries, "Label");

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int position = extras.getInt("Position");
//            Toast.makeText(this, " From the main screen at position" + position, Toast.LENGTH_SHORT).show();
        }


    }


    private void sendJsonRequest() {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20=%20%22YHOO%22%20and%20startDate%20=%20%222009-09-11%22%20and%20endDate%20=%20%222010-03-10%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=",
                null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

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

        float xaxis = 0;
        ArrayList<Stock> data = new ArrayList<Stock>();

        if (response == null || response.length() == 0) {
            return data;
        }

        JSONObject query = response.getJSONObject(QUERY);
        JSONObject results = query.getJSONObject(RESULTS);
        JSONArray quote = results.getJSONArray(QUOTE);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < quote.length(); i++) {

            Stock current = new Stock();

            JSONObject jsonObject = quote.getJSONObject(i);

            current.setSymbol(jsonObject.getString(SYMBOL));

            float open = (float) jsonObject.getDouble(OPEN);
            current.setOpen(open);

            float high = (float) jsonObject.getDouble(HIGH);
            current.setHigh(high);

            float low = (float) jsonObject.getDouble(LOW);
            current.setLow(low);


            xaxis =  i;
            current.setXaxis(xaxis);

            try {
                Date date = dateFormat.parse(DATE);
                current.setDate(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            data.add(current);

            String x = String.valueOf(xaxis);
            builder.append("high "+high+" xaxis "+x +" \n");
        }
        Toast.makeText(this,builder,Toast.LENGTH_LONG).show();
        return data;

    }

}

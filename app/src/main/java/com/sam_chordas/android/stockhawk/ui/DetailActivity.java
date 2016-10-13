package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.network.VolleySingleton;
import com.sam_chordas.android.stockhawk.rest.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;
    ArrayList<Stock> stocker = new ArrayList<>();
    List<Entry> entries;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    LineChart chart;
    StringBuilder build;
    String startdate;
    String enddate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        volleySingleton = volleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();
        sendJsonRequest();

        build = new StringBuilder();

        chart = (LineChart) findViewById(R.id.linegraph);
        chart.getXAxis().setTextColor(getResources().getColor(android.R.color.white));
        chart.getAxisLeft().setTextColor(getResources().getColor(android.R.color.white));
        chart.getAxisRight().setTextColor(getResources().getColor(android.R.color.white));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

    }

    private void sendJsonRequest() {

        startdate = getStartDate();
        enddate = getEndDate();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20=%20%22GOOG%22%20and%20startDate%20=%20%222009-09-11%22%20and%20endDate%20=%20%222010-03-10%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=",
                null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    stocker.addAll(parseJSONResponse(response));

                    entries = new ArrayList<Entry>();

                    for (Stock stk : stocker) {
                        entries.add(new Entry(stk.getXaxis(), stk.getHigh()));
                        build.append("X axis" + stk.getXaxis() + "High " + stk.getHigh());
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "Label");

                    LineData lineData = new LineData(dataSet);
                    chart.setData(lineData);
                    chart.invalidate();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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

            xaxis = i;
            current.setXaxis(xaxis);

            try {
                Date date = dateFormat.parse(DATE);
                current.setDate(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            data.add(current);

            String x = String.valueOf(xaxis);

        }


        return data;
    }

    public String getEndDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd");
        String endDate = mdformat.format(calendar.getTime());
        return endDate;
    }

    public String getStartDate() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date startDate = cal.getTime();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd");
        String startString = mdformat.format(startDate);
        return startString;
    }

    public String formatDateString(String date) {
        return date.substring(5);
    }

}

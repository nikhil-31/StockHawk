package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
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

import com.android.volley.toolbox.JsonObjectRequest;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
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
    private ArrayList<Stock> stocker = new ArrayList<>();
    private List<Entry> entries;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private StringBuilder build;
    private String startdate;
    private String enddate;
    private String mSymbol;
    private String[] labels;
    private float[] values;
    Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mSymbol = intent.getStringExtra(getString(R.string.symbol));
        mContext = getApplicationContext();
        volleySingleton = volleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();
        sendJsonRequest();

        build = new StringBuilder();

        LineChartView lineChartView = (LineChartView)findViewById(R.id.linechart);



    }

    private void sendJsonRequest() {

        startdate = getStartDate();
        enddate = getEndDate();


        Log.d("Start date", getStartDate());
        Log.d("End date", getEndDate());

        String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20=%20%22" + mSymbol + "%22%20and%20startDate%20=%20%22" + startdate + "%22%20and%20endDate%20=%20%22" + enddate + "%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

        Log.d("URL ", url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url,
                null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    stocker.addAll(parseJSONResponse(response));
                    int stocksize = stocker.size();

                    labels = new String[stocker.size()];
                    values = new float[stocker.size()];

                    int min = Math.round(stocker.get(1).getLow());
                    int max = Math.round(stocker.get(1).getHigh());


                    for(int i = 0; i < stocker.size(); i++) {
                        Stock stocking = stocker.get(i);
                        Float yaxis = stocking.getHigh();
                        String date = formatDateString(stocking.getDateString());


                        //need to put in the values opposite from how they are read

                        labels[stocker.size()-1-i] = date;
                        values[stocker.size()-1-i] = yaxis;
                    }

                    LineSet dataset = new LineSet(labels, values);
                    dataset.setColor(ContextCompat.getColor(mContext, R.color.material_green_700));
                    LineChartView lineChartView = (LineChartView)findViewById(R.id.linechart);
                    lineChartView.addData(dataset);
                    lineChartView.setAxisColor(ContextCompat.getColor(mContext, R.color.md_divider_white));
                    lineChartView.setLabelsColor(0xffffffff);
                    dataset.setDotsRadius(18);
                    dataset.setDotsColor(ContextCompat.getColor(mContext, R.color.material_green_700));
                    lineChartView.setOnEntryClickListener(new OnEntryClickListener() {
                        @Override
                        public void onClick(int setIndex, int entryIndex, Rect rect) {
                            StringBuilder priceString = new StringBuilder();
                            priceString.append("$")
                                    .append(values[entryIndex]);
                            Toast.makeText(getApplicationContext(), priceString, Toast.LENGTH_SHORT).show();
                        }
                    });


                    lineChartView.setAxisBorderValues(min-10, max+10);
                    //display table
                    lineChartView.show();








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

            current.setDateString(jsonObject.getString(DATE));

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

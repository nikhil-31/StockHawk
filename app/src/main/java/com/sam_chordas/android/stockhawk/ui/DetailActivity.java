package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int position = extras.getInt("Position");
            Toast.makeText(this," From the main screen at position" +position,Toast.LENGTH_SHORT).show();
        }


    }


}

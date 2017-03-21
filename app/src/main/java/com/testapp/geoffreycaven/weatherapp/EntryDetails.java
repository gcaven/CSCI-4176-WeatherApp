package com.testapp.geoffreycaven.weatherapp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by geoffreycaven on 2017-03-08.
 */

public class EntryDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        TextView entrySummary = (TextView)findViewById(R.id.summary_text);

        HashMap<String,String> entry = (HashMap<String,String>)getIntent().getExtras().getSerializable("entry");
        String city = getIntent().getExtras().getString("city");
        if (city.length() > 18) {
            city = city.substring(0,15) + "... ";
        }
        if (entry.get("title").toLowerCase().contains("watches or warnings") || entry.get("title").toLowerCase().contains("special weather statement")) {
            setTitle(city + ": Watches & Warnings");
        } else {
            setTitle(city + ": " + entry.get("title").split(":")[0]);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            entrySummary.setText(Html.fromHtml(entry.get("summary"), Html.FROM_HTML_MODE_LEGACY));
        } else {
            entrySummary.setText(Html.fromHtml(entry.get("summary")));
        }
    }

}

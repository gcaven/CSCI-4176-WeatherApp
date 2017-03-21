package com.testapp.geoffreycaven.weatherapp;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String currentCity = "";
    private String currentProvince = "";

    private TextView currentLocation;
    private ArrayList<HashMap<String, String>> activeResults;

    private static final int PICK_LOCATION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentLocation = (TextView)findViewById(R.id.location_preview);
    }

    public void onClickChangeLocation(View view){
        Intent intent = new Intent(this, ChangeLocation.class);
        startActivityForResult(intent, PICK_LOCATION_REQUEST);
    }

    //Parse the entry text and select a color and emoji to represent the content
    public String setColorBasedOnWeather(TextView entry, String text) {
        String lc = text.toLowerCase();
        GradientDrawable bg = (GradientDrawable) entry.getBackground();
        bg.setColor(getResources().getColor(android.R.color.white));
        entry.setTextColor(getResources().getColor(android.R.color.black));
        //yellow background and sun emoji
        if (lc.contains("sunny")) {
            text = "\uD83C\uDF24️ ️" + text;
            bg.setColor(getResources().getColor(R.color.sunnyBG));
        //blue background and lightning emoji
        } else if (lc.contains("lightning")) {
            text = "\uD83C\uDF29️ " + text;
            bg.setColor(getResources().getColor(R.color.rainyBG));
        //blue background and droplet emoji
        } else if (lc.contains("rain")) {
            text = "\uD83D\uDCA7 ️" + text;
            bg.setColor(getResources().getColor(R.color.rainyBG));
            entry.setTextColor(getResources().getColor(android.R.color.white));
        //grey background and cloud emoji
        } else if (lc.contains("cloudy")) {
            text = "☁️ " + text;
            bg.setColor(getResources().getColor(R.color.cloudyBG));
            entry.setTextColor(getResources().getColor(android.R.color.white));
        //light blue background and sun+cloud emoji
        } else if (lc.contains("clear") || lc.contains("mix of sun and cloud")) {
            if (lc.contains("night")) {
                text = "\uD83C\uDF14 " + text;
            } else {
                text = "\uD83C\uDF25️ " + text;
            }
            bg.setColor(getResources().getColor(R.color.clearBG));
            entry.setTextColor(getResources().getColor(android.R.color.white));
        //white background and snowflake emoji
        } else if (lc.contains("flurries") || lc.contains("snow") || lc.contains("blizzard")) {
            text = "❄️ " + text;
            bg.setColor(getResources().getColor(R.color.snowyBG));
        //red background and exclamation emoji
        } else if (lc.contains("special weather statement in effect")) {
            text = "❗ " + text;
            bg.setColor(getResources().getColor(R.color.alertBG));
            entry.setTextColor(getResources().getColor(android.R.color.white));
        }
        return text;
    }

    //Executed when the XMLParser returns the result of parsing the forecast XML object
    public void loadedCallback(ArrayList<HashMap<String, String>> results) {
        int prevID = 0;
        activeResults = results;
        RelativeLayout main = (RelativeLayout) findViewById(R.id.entry_view);
        main.removeAllViews();

        for (int i = 0; i < results.size(); i++) {
            TextView entry = new TextView(MainActivity.this);
            String msg = results.get(i).get("title");
            entry.setTag(i);
            entry.setId(100 + i);
            entry.setTextSize(16);
            entry.setBackgroundResource(R.drawable.rounded);
            entry.setPadding(50,50,50,50);
            entry.setText(setColorBasedOnWeather(entry, msg));

            //set layout parameters
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (prevID == 0) {
                params.addRule(RelativeLayout.BELOW, R.id.location_preview);
            } else {
                params.addRule(RelativeLayout.BELOW, prevID);
            }
            params.setMargins(0, 20, 0, 20);
            prevID = 100 + i;

            //open EntryDetails activity on tap entry TextView
            entry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView tv = (TextView)view;
                    int index = (int)tv.getTag();
                    HashMap<String,String> entry = activeResults.get(index);
                    Intent intent = new Intent(MainActivity.this, EntryDetails.class);
                    intent.putExtra("entry", entry);
                    intent.putExtra("city", currentCity);
                    ActivityOptions options =
                            ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                    startActivity(intent, options.toBundle());
                }
            });

            entry.setLayoutParams(params);
            main.addView(entry);
        }
    }

    //Listen for selected location to be returned from ChangeLocation activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case(PICK_LOCATION_REQUEST): {
                if (resultCode == Activity.RESULT_OK) {
                    String city = data.getStringExtra("city");
                    String province = data.getStringExtra("province");
                    String feed = data.getStringExtra("feed");
                    Log.i("result", "city: " + city + ", province: " + province + ", feed: " + feed);
                    currentLocation.setText(city+ ", " + province);
                    currentCity = city;
                    currentProvince = province;
                    //parse XML from the feed
                    new parseXmlAsync(this, feed).execute();
                } else {
                    //do nothing, location has not changed
                }
                break;
            }
        }
    }
}

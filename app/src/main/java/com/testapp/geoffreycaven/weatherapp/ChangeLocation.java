package com.testapp.geoffreycaven.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by geoffreycaven on 2017-03-08.
 */

public class ChangeLocation extends AppCompatActivity {

    private Spinner provinceSpinner;
    private Spinner citySpinner;
    
    private Map<String, ArrayList<String>> provincesWithCities;
    private HashMap<String,String> feedsForCurrentProvince;
    private String[] provArray;
    private String[] currentCityArray;

    private String chosenProvince;
    private String chosenCity;
    private String chosenFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_location);

        provinceSpinner = (Spinner)findViewById(R.id.province_spinner);
        citySpinner = (Spinner)findViewById(R.id.city_spinner);

        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ((TextView) provinceSpinner.getSelectedView()).setTextColor(getResources().getColor(android.R.color.white));
                if (provArray != null) {
                    chosenProvince = provArray[position];
                    setCitySpinner();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                //idk
            }

        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ((TextView) citySpinner.getSelectedView()).setTextColor(getResources().getColor(android.R.color.white));
                if (currentCityArray != null) {
                    chosenCity = currentCityArray[position];
                    chosenFeed = feedsForCurrentProvince.get(chosenCity);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                //idk
            }

        });

        citySpinner.setEnabled(false);

        ArrayList<String[]> rowList;
        rowList = parseCSV("feeds.csv");

        provincesWithCities = new HashMap<>();
        for (String[] row : rowList) {
            if (provincesWithCities.containsKey(row[2])) {
                provincesWithCities.get(row[2]).add(row[1] + "," + row[0]);
            } else {
                provincesWithCities.put(row[2], new ArrayList<String>());
                provincesWithCities.get(row[2]).add(row[1] + "," + row[0]);
            }
        }
        provArray = provincesWithCities.keySet().toArray(new String[provincesWithCities.keySet().size()]);
        Arrays.sort(provArray);
        chosenProvince = provArray[0];
        ArrayAdapter<String> spinnerProvinceArrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                provArray
        );
        spinnerProvinceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(spinnerProvinceArrayAdapter);
        provinceSpinner.setSelection(0, true);
        setCitySpinner();
    }

    //Parse feed CSV rows into arrayList
    public ArrayList<String[]> parseCSV(String file) {
        InputStream inputStream;
        try {
            inputStream = getAssets().open(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error opening CSV file " + file + ":" + e);
        }
        ArrayList<String[]> rowList = new ArrayList<>();
        BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line = bf.readLine();
            while (line != null) {
                String[] row = line.split(",");
                rowList.add(row);
                line = bf.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error closing CSV file: " + e);
            }
        }
        return rowList;
    }

    public void onClickSetLocation(View view) {
        //close activity, send location and feed url back to MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("city", chosenCity);
        resultIntent.putExtra("province", chosenProvince);
        resultIntent.putExtra("feed", chosenFeed);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    //Every time the province changes, load the correct cities into the city spinner
    public void setCitySpinner() {
        ArrayList<String> currentProvinceCities = provincesWithCities.get(chosenProvince);
        if (currentProvinceCities != null && !currentProvinceCities.isEmpty()) {

            String[] cityArray = currentProvinceCities.toArray(new String[currentProvinceCities.size()]);
            feedsForCurrentProvince = new HashMap<>();
            for (int i = 0; i < cityArray.length; i++) {
                String[] split = cityArray[i].split(",");
                cityArray[i] = split[0];
                feedsForCurrentProvince.put(split[0], split[1]);
            }
            Arrays.sort(cityArray);
            currentCityArray = cityArray;
            chosenCity = cityArray[0];
            chosenFeed = feedsForCurrentProvince.get(chosenCity);
            ArrayAdapter<String> spinnerCityArrayAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    cityArray
            );
            spinnerCityArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setAdapter(spinnerCityArrayAdapter);
            citySpinner.setSelection(0, true);
            citySpinner.setEnabled(true);
        }
    }
}

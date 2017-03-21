package com.testapp.geoffreycaven.weatherapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by geoffreycaven on 2017-03-08.
 */

public class parseXmlAsync extends AsyncTask {

    private MainActivity activity;
    private String url;
    private XmlPullParserFactory xmlFactory;
    private ProgressDialog dialog;

    public parseXmlAsync(MainActivity activity, String url) {
        this.activity = activity;
        this.url = url;
    }

    //open a loading dialog, is usual quick enough that this is never seen
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Retrieving weather information");
        dialog.setMessage("Please wait...");
        dialog.show();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {

            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            InputStream stream = connection.getInputStream();

            xmlFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            ArrayList<HashMap<String,String>> result = parseXML(parser);
            stream.close();

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getDataTask", "exception");
            return null;
        }
    }

    private ArrayList<HashMap<String, String>> parseXML(XmlPullParser parser) {
        ArrayList<HashMap<String, String>> entries = new ArrayList<>();

        try {
            int eventType = parser.getEventType();
            int entryIndex = -1;
            String saveName = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch(eventType) {
                    //When find new entry tag, create new element in entries list, if find new other tag save the name
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if (name.equals("entry")) {
                            entries.add(new HashMap<String, String>());
                            entryIndex++;
                        } else if (entryIndex >= 0) {
                            saveName = parser.getName();
                        }
                        break;
                    //When find text, grab the saved name and add name:text to current entry element
                    case XmlPullParser.TEXT:
                        if (saveName != null) {
                            HashMap<String, String> entry = entries.get(entryIndex);
                            entry.put(saveName, parser.getText());
                        }
                        saveName = null;
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
            //Log.i("entries", entries.toString());
            return entries;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        dialog.dismiss();
        activity.loadedCallback((ArrayList<HashMap<String, String>>)result);
    }
}

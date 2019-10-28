package de.abring.internet;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class GetUrlContentTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = "GetUrlContentTask";
    private boolean success = false;
    @Override
    protected String doInBackground(String... urls) {
        String content = "";
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(urls[0]);
            Log.d(TAG, "doInBackground: url: " + url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            BufferedReader br;
            if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
                success = true;
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String line;
            while ((line = br.readLine()) != null) {
                content += line + "\n";
            }
        } catch (Exception e) {
            //Log.e(TAG, "doInBackground: ", e);
            Log.e(TAG, "doInBackground: request failed");
            e.printStackTrace();
        }
        Log.d(TAG, "doInBackground:\n" + content);
        return content;
    }

    @Override
    protected void onPostExecute(String content) {
        super.onPostExecute(content);
        getResult(success, content);
    }

    public abstract void getResult(boolean success, String content);
}
package de.abring.internet;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import de.abring.service.Service;

public abstract class ServiceRequest {

    private static final String TAG = "ServiceRequest";

    private float mult = (1023.0f / 255.0f);
    private float diff = (255.0f / 1023.0f);


    public ServiceRequest(final Service service) {
        String url = service.getUriAsString() + "?get=true";
        Log.d(TAG, "getRequest: " + url);

        // ServiceRequest a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject json = new JSONObject(response);

                            service.powerOn = (Boolean) json.get("powerOn");

                            service.dimmer = json.getInt("dimmer");

                            JSONObject color = json.getJSONObject("color");
                            int red = Math.round(diff * (float) color.getInt("red"));
                            int green = Math.round(diff * (float) color.getInt("green"));
                            int blue = Math.round(diff * (float) color.getInt("blue"));

                            red = (red << 16) & 0x00FF0000;
                            green = (green << 8) & 0x0000FF00;
                            blue = blue & 0x000000FF;

                            service.mixedColor = 0xFF000000 | red | green | blue;

                            finished(service);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: ", error);
                    }
                });
        stringRequest.setTag(TAG);
        service.queue.add(stringRequest);

    }

    public abstract void finished(Service service);
}

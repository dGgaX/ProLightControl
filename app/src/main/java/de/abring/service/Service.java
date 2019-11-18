package de.abring.service;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceInfo;

import de.abring.prolightcontrol.R;

public class Service implements Serializable {

    public static final String SIGNAL_WS_TCP_LOCAL = "_http._tcp.local.";
    public static final String SIGNAL_APPLICATION = "http";
    public static final String SIGNAL_PREFIX = "PL_";

    public static Service fromServiceInfo (ServiceInfo info, RequestQueue queue) {
        if (info.getApplication().equals(SIGNAL_APPLICATION) &&
                info.getName().startsWith(SIGNAL_PREFIX) &&
                info.getType().equals(SIGNAL_WS_TCP_LOCAL)) {
            if (info.getHostAddresses().length == 0) {
                return new Service(info.getName().substring(SIGNAL_PREFIX.length()), queue);
            }
            return new Service(info.getName().substring(SIGNAL_PREFIX.length()), info.getApplication() + "://" + info.getHostAddresses()[0] + ":" + String.valueOf(info.getPort()), queue);
        }
        return null;
    }

    private static final String TAG = "Service";


    private float mult = (1023.0f / 255.0f);
    private float diff = (255.0f / 1023.0f);

    private final String name;
    private final URI uri;

    private int icon;
    private int removeIcon;

    public boolean powerOn = false;
    public int mixedColor = 0;
    public int dimmer = 0;

    private boolean inactiv;
    private boolean showBucket;

    private URI stringToURI(String uri) {
        try {
            if (uri.isEmpty()) {
                return null;
            }
            return new URI(uri);
        } catch (URISyntaxException e) {
            Log.e(TAG, "stringToURI: ", e);
        }
        return null;
    }

    public transient RequestQueue queue;

    public Service(RequestQueue queue) {
        this.queue = queue;
        this.name = "";
        this.uri = stringToURI("");
        this.icon = R.drawable.ic_icon_wt;
        this.removeIcon = R.drawable.ic_delete_foreground;
        this.inactiv = false;
    }

    public Service(String name, RequestQueue queue) {
        this.queue = queue;
        this.name = name;
        this.uri = stringToURI("");
        this.icon = R.drawable.ic_icon_wt;
        this.removeIcon = R.drawable.ic_delete_foreground;
        this.inactiv = false;
    }

    public Service(String name, URI uri, RequestQueue queue) {
        this.queue = queue;
        this.name = name;
        this.uri = uri;
        this.icon = R.drawable.ic_icon_wt;
        this.removeIcon = R.drawable.ic_delete_foreground;
        this.inactiv = false;

    }

    public Service(String name, String uri, RequestQueue queue) {
        this.queue = queue;
        this.name = name;
        this.uri = stringToURI(uri);
        this.icon = R.drawable.ic_icon_wt;
        this.removeIcon = R.drawable.ic_delete_foreground;
        this.inactiv = false;

    }

    public String getName() {
        return name;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isInactiv() {
        return inactiv;
    }

    public void setInactiv() {
        this.inactiv = true;
    }

    @Override
    public String toString() {
        String returnString = name + ": ";
        if (inactiv) {
            returnString += "inactiv! ";
        }
        if (uri == null) {
            returnString += "No URI ...";
        } else {
            returnString += getUriAsString();
        }
        return returnString;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int imageResource) {
        this.icon = imageResource;
    }

    public String getUriAsString() {
        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }
        return uri.toString();
    }

    public void showBucket(boolean b) {
        this.showBucket = b;
    }

    public boolean isShowBucket() {
        return this.showBucket;
    }

    private void sendRequest(String ... param) {
        String params = "";
        for (int i = 0; i < param.length; i++) {
            params += param[i];
            if (i < param.length - 1) {
                params += "&";
            }
        }

        String url = getUriAsString() + "?" + params;

        Log.d(TAG, "sendRequest: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: ", error);
                    }
                });
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }

    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
        String param = "powerOn=";
        if (powerOn) {
            param += "true";
        } else {
            param += "false";
        }
        sendRequest(param);
    }

    public void setColor(int selectedColor) {
        mixedColor = selectedColor;
        Log.d(TAG, "onColorSelected mix: " + Integer.toHexString(selectedColor));
        int red = Math.round(mult * (float) ((selectedColor >> 16) & 0xff));
        int green = Math.round(mult * (float) ((selectedColor >> 8) & 0xff));
        int blue = Math.round(mult * (float) ((selectedColor >> 0) & 0xff));
        Log.d(TAG, "onColorSelected red: " + Integer.toString(red));
        Log.d(TAG, "onColorSelected green: " + Integer.toString(green));
        Log.d(TAG, "onColorSelected blue: " + Integer.toString(blue));

        String paramRed = "red=" + Integer.toString(red);
        String paramGreen = "green=" + Integer.toString(green);
        String paramBlue = "blue=" + Integer.toString(blue);
        sendRequest(paramRed, paramGreen, paramBlue);
    }

    public void setQueue(RequestQueue queue) {
        this.queue = queue;
    }

    public void setDimmer(int dimmer) {
        this.dimmer = dimmer;
        String paramDimmer = "dimmer=" + Integer.toString(dimmer);
        sendRequest(paramDimmer);
    }
}

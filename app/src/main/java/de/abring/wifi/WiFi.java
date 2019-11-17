package de.abring.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import de.abring.service.ServicesHandler;
import de.abring.internet.DeviceCommunicator;
import de.abring.prolightcontrol.R;
import de.abring.service.Service;
import de.abring.service.Services;

public class WiFi {

    private static final String TAG = "WiFi";

    private static final String WPA_KEY = "vierzigzwei";

    private final Context context;

    private WifiScanner wifiScanner;

    private WifiManager wifiManager;
    private WifiInfo connectedNetworkWifiInfo;


    public WiFi(final Context context, final ServicesHandler handler) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connectedNetworkWifiInfo = wifiManager.getConnectionInfo();

        ;
        wifiScanner = new WifiScanner(context) {
            @Override
            public void getResult(int result, List<ScanResult> scanResults) {
                if (result == WifiScanner.SUCCESS) {
                    for (ScanResult scanResult : scanResults) {
                        if (scanResult.SSID.startsWith(context.getResources().getString(R.string.wifi_name))) {
                            Info info = new Info(scanResult.SSID, scanResult.BSSID, WPA_KEY);
                            Log.d(TAG, "serviceAdded: " + info);
                            Message.obtain(handler, ServicesHandler.SERVICE_FOUND, info).sendToTarget();
                        }
                    }
                } else if (result == WifiScanner.FAILURE) {
                    Message.obtain(handler, ServicesHandler.SERVICE_NOT_FOUND, null).sendToTarget();
                } else if (result == WifiScanner.NOT_POSSIBLE) {
                    Message.obtain(handler, ServicesHandler.SERVICE_NOT_POSSIBLE, null).sendToTarget();
                }
            }
        };

    }


    public void start() {
        wifiScanner.start();
    }

    public void reconnectToDefaultNetwork() {
        wifiManager.disconnect();
        wifiManager.enableNetwork(connectedNetworkWifiInfo.getNetworkId(), true);
        wifiManager.reconnect();
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.startsWith("\"" + context.getResources().getString(R.string.wifi_name))) {
                wifiManager.removeNetwork(i.networkId);
            }
        }
    }
}

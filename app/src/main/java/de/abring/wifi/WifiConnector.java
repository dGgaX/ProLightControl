package de.abring.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import de.abring.remotecontrol.R;

public abstract class WifiConnector extends AsyncTask<String, Integer, Boolean> {

    private static final String TAG = "WifiConnector";

    private WifiManager wifiManager;
    private Context context;

    private BroadcastReceiver wifiConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            NetworkInfo networkInfo = intent .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Log.d(TAG, "connect: " + networkInfo.getDetailedState().name());
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                context.unregisterReceiver(wifiConnectReceiver);
                Log.d(TAG, "connect: connected to " + wifiManager.getConnectionInfo().getSSID());
                Toast.makeText(context, R.string.wifi_connector_state_connected, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "connect: execute connected-method!!!");
                connected(wifiManager.getConnectionInfo().getSSID());
            }
        }
    };

    public WifiConnector(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);
        if (s) {
            Toast.makeText(context, R.string.wifi_connector_state_connecting, Toast.LENGTH_SHORT).show();
            final IntentFilter intentFilter =
                    new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            context.registerReceiver(wifiConnectReceiver, intentFilter);
        }
    }

    @Override
    protected Boolean doInBackground(String... words) {
        if (words.length != 3) {
            return false;
        }
        return connect(words[0], words[1], words[2]);
    }

    private boolean connect(String SSID, String BSSID, String WPAKey) {
        if (SSID == null || SSID.isEmpty() || BSSID == null || BSSID.isEmpty()) {
            return false;
        }

        Log.d(TAG, "connect: to \"" + SSID + "\" (\"" + BSSID + "\") with WPA-Key \"" + WPAKey + "\"");

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        wifiConfiguration.status = WifiConfiguration.Status.CURRENT;

        wifiConfiguration.SSID = "\"" + SSID + "\"";
        wifiConfiguration.BSSID = BSSID;
        wifiConfiguration.preSharedKey = "\""+ WPAKey +"\"";

        int netId = wifiManager.addNetwork(wifiConfiguration);
        Log.d(TAG, "connect: addNetwork: " + wifiConfiguration.SSID);
        if (netId == -1) {
            netId = wifiManager.updateNetwork(wifiConfiguration);
            Log.d(TAG, "connect: updateNetwork: " + wifiConfiguration.SSID);
        }

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals(wifiConfiguration.SSID) && i.BSSID != null && i.BSSID.equals(wifiConfiguration.BSSID)) {
                Log.d(TAG, "connect: WifiConfiguration SSID " + i.SSID);

                boolean isDisconnected = wifiManager.disconnect();
                Log.d(TAG, "connect: isDisconnected: " + isDisconnected);

                boolean isEnabled = wifiManager.enableNetwork(i.networkId, true);
                Log.d(TAG, "connect: isEnabled: " + isEnabled);


                boolean isReconnected = wifiManager.reconnect();
                Log.d(TAG, "connect: isReconnected: " + isReconnected);

                return true;
            }
        }
        return false;
    }

    public abstract void connected(String SSID);

}

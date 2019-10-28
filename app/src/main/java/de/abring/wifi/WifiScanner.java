package de.abring.wifi;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.abring.prolightcontrol.R;

public abstract class WifiScanner {

    public static int SUCCESS       = 0;
    public static int FAILURE       = 1;
    public static int NOT_POSSIBLE  = 2;

    private static final String TAG = "WifiScanner";
    private WifiManager wifiManager;
    private final Context context;
    private List<ScanResult> results = new ArrayList<>();
    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }
    };
    private boolean wifiWasDisabled = false;

    public WifiScanner(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void start() {
        if (wifiManager.isWifiEnabled()) {
            scanForNetworks();
        } else {
            enableWifi();
        }
    }

    public void end() {
        disableWifi();
    }

    public abstract void getResult(int result, List<ScanResult> scanResults);

    private void scanForNetworks() {
        Log.d(TAG, "scanForNetworks: Scan for Wifis ...");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();

        if (!success) {
            scanFailure();
        }
    }

    private void scanSuccess() {
        results = wifiManager.getScanResults();
        context.unregisterReceiver(wifiScanReceiver);
        message(R.string.wifi_scanner_scan_success, Toast.LENGTH_SHORT);
        getResult(SUCCESS, results);
    }

    private void scanFailure() {
        context.unregisterReceiver(wifiScanReceiver);
        message(R.string.wifi_scanner_scan_failure, Toast.LENGTH_LONG);
        getResult(FAILURE, results);
    }

    private void scanNotPossible() {
        results.clear();
        message(R.string.wifi_scanner_scan_not_possible, Toast.LENGTH_LONG);
        getResult(NOT_POSSIBLE, results);
    }

    private void message(int text, int duration) {
        Log.d(TAG, "message: " + context.getResources().getString(text));
        Toast.makeText(context, text, duration).show();
    }

    private void enableWifi() {
        wifiWasDisabled = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder
            .setMessage(R.string.wifi_scanner_enable_wifi_message)
            .setTitle(R.string.wifi_scanner_enable_wifi_title)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (wifiManager.setWifiEnabled(true)) {
                        scanForNetworks();
                    } else {
                        scanNotPossible();
                    }
                }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    scanNotPossible();
                }
            });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void disableWifi() {
        if (wifiWasDisabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder
                .setMessage(R.string.wifi_scanner_disable_wifi_message)
                .setTitle(R.string.wifi_scanner_disable_wifi_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (wifiManager.setWifiEnabled(false)) {
                            message(R.string.wifi_scanner_disable_wifi_success, Toast.LENGTH_SHORT);
                        } else {
                            message(R.string.wifi_scanner_disable_wifi_failed, Toast.LENGTH_SHORT);
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }
}

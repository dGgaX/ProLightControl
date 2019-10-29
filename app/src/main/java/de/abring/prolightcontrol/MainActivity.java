package de.abring.prolightcontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceInfo;

import de.abring.MDNS.MDNS;
import de.abring.MDNS.MDNSHandler;
import de.abring.internet.DeviceCommunicator;
import de.abring.service.Service;
import de.abring.service.Services;
import de.abring.wifi.WifiConnector;
import de.abring.wifi.WifiScanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private  static final String WPA_KEY = "JEX2h6Bn6uo5pDai";

    private List<String> foundSSIDs;
    private List<String> foundBSSIDs;
    private WifiScanner wifiScanner;

    private NetworkInfo.DetailedState state = NetworkInfo.DetailedState.DISCONNECTED;

    private ImageView logo;
    private ProgressBar progressBar;
    private TextView text;
    private FloatingActionButton fab_scan_wifi;

    private WifiManager wifiManager;
    private WifiInfo connectedNetworkWifiInfo;

    private MDNS mDNS = null;
    private Services services;

    public static MDNSHandler serviceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: start...");
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        logo = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        text = findViewById(R.id.textView);
        fab_scan_wifi = findViewById(R.id.fab_scan_wifi);

        fab_scan_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWifi();
            }
        });

        foundSSIDs = new ArrayList<>();
        foundBSSIDs = new ArrayList<>();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectedNetworkWifiInfo = wifiManager.getConnectionInfo();

        wifiScanner = new WifiScanner(this) {
            @Override
            public void getResult(int result, List<ScanResult> scanResults) {
                progressBar.setVisibility(View.INVISIBLE);
                if (result == WifiScanner.SUCCESS) {
                    foundSSIDs.clear();
                    foundBSSIDs.clear();
                    for (ScanResult scanResult : scanResults) {
                        if (scanResult.SSID.startsWith(getResources().getString(R.string.wifi_name))) {
                            foundSSIDs.add(scanResult.SSID);
                            foundBSSIDs.add(scanResult.BSSID);
                        }
                    }
                    showWifis();
                } else if (result == WifiScanner.FAILURE) {
                    text.setText(R.string.wifi_scanner_scan_failure);
                } else if (result == WifiScanner.NOT_POSSIBLE) {
                    text.setText(R.string.wifi_scanner_scan_not_possible);
                }
            }
        };
        services = new Services(this);
        serviceHandler = new MDNSHandler(services){
            public void handleMessage(Message msg){
                String text = "";
                switch(msg.what){
                    case MDNSHandler.SERVICE_FOUND:
                        services.addService(Service.fromServiceInfo((ServiceInfo) msg.obj));
                        text = "Fida found!";
                        break;
                    case MDNSHandler.SERVICE_RESOLVED:
                        ServiceInfo updateInfo = (ServiceInfo) msg.obj;
                        if (services.updateService(Service.fromServiceInfo(updateInfo))) {
                            text = "Fida " + updateInfo.getName() + " updated!";
                        }
                        break;
                    case MDNSHandler.SERVICE_REMOVED:
                        ServiceInfo deleteInfo = (ServiceInfo) msg.obj;
                        if (services.removeService(Service.fromServiceInfo(deleteInfo))) {
                            text = "Fida " + deleteInfo.getName() + " deleted!";
                        }
                        break;
                }
                if (!text.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        };
    }

    private void getWifi() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder
                    .setMessage(R.string.main_activiy_location_permission_message)
                    .setTitle(R.string.main_activiy_location_permission_title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0x12345);
                        }
                    });


            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            fab_scan_wifi.hide();
            progressBar.setVisibility(View.VISIBLE);
            text.setText(R.string.main_activiy_device_search);
            wifiScanner.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0x12345) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            getWifi();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_show_legal_notice was selected
            case R.id.action_show_legal_notice:
                showLegalNotice();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        getWifi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: end ...");
        reconnectToDefaultNetwork();
        // -> needs to be set in a different thread...
        //wifiScanner.end();
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.startsWith("\"" + getResources().getString(R.string.wifi_name))) {
                wifiManager.removeNetwork(i.networkId);
            }
        }
    }

    private void showWifis() {
        Log.d(TAG, "showWifis: found " + String.valueOf(foundSSIDs.size()) + " wifis.");

        if (foundSSIDs.isEmpty()) {
            text.setText(R.string.main_activiy_device_not_found);
            showTutorial();
        } else {
            text.setText(R.string.main_activiy_device_found);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder
                    .setTitle(R.string.main_activiy_device_chooser_title)
                    .setSingleChoiceItems(foundSSIDs.toArray(new CharSequence[foundSSIDs.size()]), -1, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "onClick: changeDevice to: " + foundSSIDs.get(which));
                            text.setText(getResources().getString(R.string.main_activiy_device_found));
                            if (wifiManager.getConnectionInfo().getBSSID().equals(foundBSSIDs.get(which))) {
                                new DeviceCommunicator.Blink(DeviceCommunicator.Blink.TOGGLE) {
                                    @Override
                                    public void finished(boolean success) {}
                                };
                                if (state.equals(NetworkInfo.DetailedState.CONNECTED)) {
                                    text.setText(getResources().getString(R.string.wifi_connector_connected_to) + " " + wifiManager.getConnectionInfo().getSSID());
                                }
                            } else {
                                final WifiConnector wifiConnector = new WifiConnector(getApplicationContext()) {
                                    @Override
                                    public void update(String SSID) {
                                        text.setText(getResources().getString(R.string.wifi_connector_connected_to) + " " + SSID);
                                    }

                                };
                                wifiConnector.execute(foundSSIDs.get(which), foundBSSIDs.get(which), WPA_KEY);
                            }
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "onClick: ok");
                            if (state.equals(NetworkInfo.DetailedState.CONNECTED)) {
                                configDevice();
                            } else {
                                Log.d(TAG, "onClick: cancel");
                                new DeviceCommunicator.Blink(DeviceCommunicator.Blink.OFF) {
                                    @Override
                                    public void finished(boolean success) {}
                                };
                                //reconnectToDefaultNetwork();
                                fab_scan_wifi.show();
                                dialog.dismiss();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "onClick: cancel");
                            new DeviceCommunicator.Blink(DeviceCommunicator.Blink.OFF) {
                                @Override
                                public void finished(boolean success) {}
                            };
                            //reconnectToDefaultNetwork();
                            fab_scan_wifi.show();
                            dialog.dismiss();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    public void reconnectToDefaultNetwork() {
        wifiManager.disconnect();
        wifiManager.enableNetwork(connectedNetworkWifiInfo.getNetworkId(), true);
        wifiManager.reconnect();
    }

    private void showTutorial() {
        Log.d(TAG, "showTutorial: start.");

    }

    private void showLegalNotice() {
        Intent legalNoticeActivityIntent = new Intent(this, LegalNoticeActivity.class);
        startActivity(legalNoticeActivityIntent);
    }
    private void configDevice() {
        Log.d(TAG, "configDevice: start.");
        new DeviceCommunicator.Blink(DeviceCommunicator.Blink.OFF) {
            @Override
            public void finished(boolean success) {}
        };


     //   remoteConfigurator.start();

    }
}

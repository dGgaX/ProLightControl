package de.abring.prolightcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.abring.MDNS.MDNS;
import de.abring.service.ServicesHandler;
import de.abring.internet.DeviceCommunicator;
import de.abring.service.Services;
import de.abring.wifi.WiFi;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private ImageView logo;
    private ProgressBar progressBar;
    private TextView text;
    private FloatingActionButton fab_scan_mdns;
    private FloatingActionButton fab_scan_wifi;

    private Services services;
    private ServicesHandler handler;
    private WiFi wifi;
    private MDNS mDNS;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

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
        fab_scan_mdns = findViewById(R.id.fab_scan_mdns);

        services = new Services(getApplicationContext());
        handler = new ServicesHandler(services);
        wifi = new WiFi(getApplicationContext(), handler);
        mDNS = new MDNS(getApplicationContext(), handler);

        fab_scan_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWifiSearch();
            }
        });

        fab_scan_mdns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMDNSSearch();
            }
        });
        builder = new AlertDialog.Builder(this);

        builder
                .setTitle(R.string.main_activiy_device_chooser_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0x12345);
                    }
                });
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
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
        dialog.hide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: end ...");
        wifi.reconnectToDefaultNetwork();
    }

    private void showLegalNotice() {
        Intent legalNoticeActivityIntent = new Intent(this, LegalNoticeActivity.class);
        startActivity(legalNoticeActivityIntent);
    }

    private void playDevices() {
        Log.d(TAG, "playDevices: start.");
        //new DeviceCommunicator.Blink(DeviceCommunicator.Blink.OFF) {};
        Intent playActivityIntent = new Intent(getApplicationContext(), PlayActivity.class);
        startActivity(playActivityIntent);
    }

    private void startMDNSSearch() {
        //dialog.show();
        mDNS.run();
    }

    private void startWifiSearch() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder
                    .setMessage(R.string.main_activiy_location_permission_message)
                    .setTitle(R.string.main_activiy_location_permission_title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0x12345);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            this.dialog.show();
            wifi.start();
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
            startWifiSearch();
        }
    }
}

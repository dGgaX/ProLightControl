package de.abring.prolightcontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import javax.jmdns.ServiceInfo;

import de.abring.MDNS.MDNS;
import de.abring.internet.ServiceRequest;
import de.abring.service.Service;
import de.abring.service.ServiceAdapter;
import de.abring.service.ServicesHandler;
import de.abring.service.Services;
import de.abring.wifi.WiFi;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private FloatingActionButton fab_scan_mdns;

    private Services services;
    private ServicesHandler handler;

    private RecyclerView mRecyclerView;
    private ServiceAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RequestQueue queue;

    private WiFi wifi;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: start...");
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(getApplicationContext());

        fab_scan_mdns = findViewById(R.id.fab_scan_mdns);

        services = new Services(this, queue);
        handler = new ServicesHandler(services){
            public void handleMessage(Message msg) {
                String text = "";
                switch (msg.what) {
                    case ServicesHandler.SERVICE_FOUND:
                        services.addService(Service.fromServiceInfo((ServiceInfo) msg.obj, queue));
                        break;
                    case ServicesHandler.SERVICE_RESOLVED:
                        ServiceInfo updateInfo = (ServiceInfo) msg.obj;
                        Service service = Service.fromServiceInfo(updateInfo, queue);
                        if (services.updateService(service)) {
                            new ServiceRequest(service) {
                                @Override
                                public void finished(Service service) {
                                    mAdapter.notifyDataSetChanged();
                                }
                            };
                            text = updateInfo.getName() + " updated!";
                        }
                        break;
                    case ServicesHandler.SERVICE_REMOVED:
                        ServiceInfo deleteInfo = (ServiceInfo) msg.obj;
                        if (services.removeService(Service.fromServiceInfo(deleteInfo, queue))) {
                            text = deleteInfo.getName() + " deleted!";
                        }
                        break;
                }
                if (!text.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = services.getAdapter();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        wifi = new WiFi(getApplicationContext(), handler);

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
        startMDNSSearch();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
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
        new MDNS(getApplicationContext(), handler) {
            @Override
            public void finished() {
                mAdapter.updateStates();
            }
        }.execute();
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

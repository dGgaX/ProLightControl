package de.abring.MDNS;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import work.fida.fidaremote.MainActivity;

import static android.util.Log.e;

public class MDNS extends Thread {

    private static final String TAG = "MDNS";

    public static final String SIGNAL_WS_TCP_LOCAL = "_ws._tcp.local.";

    private final InetAddress inetAddress;
    private final MDNSHandler handler;
    private final ServiceListener serviceListener;

    public MDNS(MainActivity mainActivity) {
        this.inetAddress = getIP(mainActivity);
        this.handler = mainActivity.getServiceHandler();
        this.serviceListener = new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                ServiceInfo info = event.getInfo();
                Log.d(TAG, "serviceAdded: " + info);
                Message.obtain(handler, MDNSHandler.SERVICE_FOUND, info).sendToTarget();
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                ServiceInfo info = event.getInfo();
                Log.d(TAG, "serviceResolved: " + info);
                Message.obtain(handler, MDNSHandler.SERVICE_RESOLVED, info).sendToTarget();
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                ServiceInfo info = event.getInfo();
                Log.d(TAG, "serviceResolved: " + info);
                Message.obtain(handler, MDNSHandler.SERVICE_REMOVED, info).sendToTarget();
            }
        };
    }


    private InetAddress getIP(MainActivity mainActivity) {
        try {
            WifiManager wifiMgr = (WifiManager) mainActivity.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int intAddress = wifiInfo.getIpAddress();
            byte[] byteAddress = {(byte)(intAddress & 0xff), (byte)(intAddress >> 8 & 0xff), (byte)(intAddress >> 16 & 0xff), (byte)(intAddress >> 24 & 0xff)};
            return InetAddress.getByAddress(byteAddress);
        } catch (Exception ex) {
            e("Test", ex.getMessage());
            return null;
        }
    }

    public void run() {
        JmDNS jmdns = null;
        boolean running = true;
        try {
            // Create a JmDNS instance
            jmdns = JmDNS.create(inetAddress);

            // Register the Servicetype
            jmdns.registerServiceType(SIGNAL_WS_TCP_LOCAL);

            // Add a service listener
            jmdns.addServiceListener(SIGNAL_WS_TCP_LOCAL, serviceListener);

        } catch (IOException e) {
            Log.e(TAG, "run: ", e);
            running = false;
        }
        while (running) {
            try {
                sleep(500);
            } catch(InterruptedException e) {
                Log.e(TAG, "run: ", e);
                running = false;
            }
        }
        if (jmdns != null) {
            jmdns.removeServiceListener(SIGNAL_WS_TCP_LOCAL, serviceListener);
            jmdns = null;
        }
    }
}

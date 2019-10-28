package de.abring.MDNS;

import android.os.Handler;

import de.abring.service.Services;

public class MDNSHandler extends Handler {
    public static final int SERVICE_FOUND = 0;
    public static final int SERVICE_RESOLVED = 1;
    public static final int SERVICE_REMOVED = 2;
    private Services services;

    public MDNSHandler(Services services) {
        this.services = services;
    }
}

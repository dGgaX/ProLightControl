package de.abring.service;

import android.os.Handler;

import de.abring.service.Services;

public class ServicesHandler extends Handler {
    public static final int SERVICE_FOUND = 0;
    public static final int SERVICE_NOT_FOUND = 1;
    public static final int SERVICE_NOT_POSSIBLE = 2;
    public static final int SERVICE_RESOLVED = 3;
    public static final int SERVICE_REMOVED = 4;
    private Services services;

    public ServicesHandler(Services services) {
        this.services = services;
    }
}

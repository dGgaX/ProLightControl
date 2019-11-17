package de.abring.internet;

import android.util.Log;

import java.util.List;

public class DeviceCommunicator {

    private static final String TAG = "DeviceCommunicator";

    private final List<String> ips;

    public DeviceCommunicator(List<String> ips) {
        this.ips = ips;
    }


    public abstract class Blink {
        public static final int ON = 0;
        public static final int OFF = 1;
        public static final int TOGGLE = 2;
        public Blink(int state) {
            Log.d(TAG, "blink: " + state);

            String url = "blink?";

            switch (state) {
                case ON:
                    url += "on=1";
                    break;
                case OFF:
                    url += "off=1";
                    break;
                case TOGGLE:
                    url += "toggle=1";
                    break;
            }

            execute(url);
        }

    }

    private void execute(String... url) {
        for (String ip : ips) {
            new GetUrlContentTask() {
                @Override
                public void getResult(boolean success, String content) {
                    Log.d(TAG, "execute: " + content);
                }
            }.execute(ip + url);
        }
    }
}

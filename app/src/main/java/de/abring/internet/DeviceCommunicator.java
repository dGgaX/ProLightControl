package de.abring.internet;

import android.util.Log;

public class DeviceCommunicator {

    private static final String TAG = "DeviceCommunicator";

    private static final String IP = "http://192.168.4.1/";


    public abstract static class Blink {
        public static final int ON = 0;
        public static final int OFF = 1;
        public static final int TOGGLE = 2;
        public Blink(int state) {
            Log.d(TAG, "blink: " + String.valueOf(state));

            String url = IP + "blink?";

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

            new GetUrlContentTask() {
                @Override
                public void getResult(boolean success, String content) {
                    Log.d(TAG, "blink: " + content);
                    finished(success);
                }
            }.execute(url);
        }

        public abstract void finished(boolean success);

    }
    public abstract static class AddRemote {
        public static final int START = 0;
        public static final int KEY = 1;
        public static final int FIN_FB = 2;
        public static final int NEU_FB = 3;
        public static final int END = 4;
        public static final int CANCEL = 5;
        public AddRemote(int page) {
            Log.d(TAG, "addRemote: " + String.valueOf(page));

            String url = IP;

            switch (page) {
                case START:
                    url += "start";
                    break;
                case KEY:
                    url += "key";
                    break;
                case FIN_FB:
                    url += "finFB";
                    break;
                case NEU_FB:
                    url += "neuFB";
                    break;
                case END:
                    url += "end2";
                    break;
                case CANCEL:
                    url += "cancel";
                    break;
            }

            new GetUrlContentTask() {
                @Override
                public void getResult(boolean success, String content) {
                    finished(success, content);
                }
            }.execute(url);
        }

        public abstract void finished(boolean success, String content);

    }


}

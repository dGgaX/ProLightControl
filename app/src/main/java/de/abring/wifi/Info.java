package de.abring.wifi;

public class Info {

    private String ssid;
    private String bssid;
    private String wpa;

    public Info(String ssid, String bssid, String wpa) {
        this.setSsid(ssid);
        this.setBssid(bssid);
        this.setWpa(wpa);
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getWpa() {
        return wpa;
    }

    public void setWpa(String wpa) {
        this.wpa = wpa;
    }
}

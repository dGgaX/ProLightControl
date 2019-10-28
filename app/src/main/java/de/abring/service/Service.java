package de.abring.service;

import android.util.Log;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceInfo;

import work.fida.fidaremote.R;
import work.fida.fidaremote.data.Remote;

public class Service implements Serializable {

    public static final String SIGNAL_WS_TCP_LOCAL = "_ws._tcp.local.";
    public static final String SIGNAL_APPLICATION = "ws";
    public static final String SIGNAL_PREFIX = "FIDA_";

    public static Service fromServiceInfo (ServiceInfo info) {
        if (info.getApplication().equals(SIGNAL_APPLICATION) &&
                info.getName().startsWith(SIGNAL_PREFIX) &&
                info.getType().equals(SIGNAL_WS_TCP_LOCAL)) {
            if (info.getHostAddresses().length == 0) {
                return new Service(info.getName().substring(SIGNAL_PREFIX.length()));
            }
            return new Service(info.getName().substring(SIGNAL_PREFIX.length()), info.getApplication() + "://" + info.getHostAddresses()[0] + ":" + String.valueOf(info.getPort()));
        }
        return null;
    }


    private static final String TAG = "Service";

    private final String name;
    private final URI uri;
    private final List<Remote> remotes;

    private int icon;
    private int removeIcon;

    private boolean inactiv;
    private boolean showBucket;

    private URI stringToURI(String uri) {
        try {
            if (uri.isEmpty()) {
                return null;
            }
            return new URI(uri);
        } catch (URISyntaxException e) {
            Log.e(TAG, "stringToURI: ", e);
        }
        return null;
    }

    public Service() {
        this.name = "";
        this.uri = stringToURI("");
        this.remotes = new ArrayList<>();
        this.icon = R.drawable.ic_service;
        this.removeIcon = R.drawable.ic_delete;
        this.inactiv = false;
    }

    public Service(String name) {
        this.name = name;
        this.uri = stringToURI("");
        this.remotes = new ArrayList<>();
        this.icon = R.drawable.ic_service;
        this.removeIcon = R.drawable.ic_delete;
        this.inactiv = false;
    }

    public Service(String name, URI uri) {
        this.name = name;
        this.uri = uri;
        this.remotes = new ArrayList<>();
        this.icon = R.drawable.ic_service;
        this.removeIcon = R.drawable.ic_delete;
        this.inactiv = false;
    }

    public Service(String name, String uri) {
        this.name = name;
        this.uri = stringToURI(uri);
        this.remotes = new ArrayList<>();
        this.icon = R.drawable.ic_service;
        this.removeIcon = R.drawable.ic_delete;
        this.inactiv = false;
    }

    public Remote getRemote(int i) {
        return remotes.get(i);
    }

    public void addRemote(Remote remote) {
        if (!updateRemote(remote)) {
            if (remote.name.toUpperCase().startsWith("INDEX")) {
                Log.i(TAG, "addRemote: Index @ position 0");
                remotes.add(0, remote);
            } else {
                Log.i(TAG, "addRemote: " + remote.name);
                remotes.add(remote);
            }
        }
    }

    public boolean updateRemote(Remote remote) {
        for (Remote lRemote : remotes) {
            if (lRemote.name.equals(remote.name)) {
                Log.i(TAG, "updateRemote: " + remote.name);
                remotes.set(remotes.indexOf(lRemote), remote);
                return true;
            }
        }
        return false;
    }


    // perhaps just delete all Remotes and ad the new ones... ?
    public boolean removeRemote(Remote remote) {
        for (Remote lRemote : remotes) {
            if (lRemote.name.equals(remote.name)) {
                Log.i(TAG, "removeRemote: " + remote.name);
                remotes.remove(lRemote);
                return true;
            }
        }
        return false;
    }

    public void clearRemotes() {
        Log.i(TAG, "clearRemotes: all Remotes removed");
        remotes.clear();
    }

    public int numberOfRemotes() {
        return remotes.size();
    }

    public String getName() {
        return name;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isInactiv() {
        return inactiv;
    }

    public void setInactiv() {
        this.inactiv = true;
    }

    @Override
    public String toString() {
        String returnString = name + ": ";
        if (inactiv) {
            returnString += "inactiv! ";
        }
        if (uri == null) {
            returnString += "No URI ...";
        } else {
            returnString += getUriAsString();
        }
        return returnString;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int imageResource) {
        this.icon = imageResource;
    }

    public String getUriAsString() {
        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }
        return uri.toString();
    }

    public Remote getRemoteByName(String name) {
        for (Remote remote : remotes) {
            if (remote.name.equals(name)) {
                return remote;
            }
        }
        return null;
    }

    public void showBucket(boolean b) {
        this.showBucket = b;
    }

    public boolean isShowBucket() {
        return this.showBucket;
    }

    public boolean updateRemotes(Service service) {
        for (int i = 0; i < service.numberOfRemotes(); i++) {
            Remote remote = service.getRemote(i);
            addRemote(remote);
        }
        return false;
    }
}

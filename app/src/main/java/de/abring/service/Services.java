package de.abring.service;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class Services {

    private static final String TAG = "Services";
    private static final String SERVICES_LIST = "services list";
    private static final String SHARED_PREFERENCES = "shared preferences";

    public static final int INPUT_ACTIVITY_RESULT = 42;
    public static final String INPUT_ACTIVITY_SERVICE = "service";
    private final Context context;
    private final List<Service> list;

    private RecyclerView.Adapter adapter;
    private boolean showDelete = false;

    public Services(Context context) {
        this.context = context;
        list = load();
        adapter = new ServiceAdapter(context, list);
        ((ServiceAdapter) adapter).addServiceClickListener(new ServiceClickListener() {
            @Override
            public void onServiceClick(int position) {
                chooseService(position);
            }

            @Override
            public void onDeleteClick(int position) {
                if (removeService(position)) {
                    showRemoveFida(false);
                }
            }
        });
        if (list.size() == 1) {
            chooseService(0);
        }
    }

    public void updateRemotes(Service service) {
        Log.i(TAG, "updateRemotes: " + service.getName());
        for (Service lService : list) {
            if (lService.getName().equals(service.getName())) {
                if (lService.updateRemotes(service)) {
                    Log.d(TAG, "updateRemotes: update succeed");
                    break;
                }
                Log.d(TAG, "updateRemotes: update failed");
                break;
            }
        }
    }

    private void chooseService(int position) {
        Service service = list.get(position);
        Log.i(TAG, "chooseService: choose: " + service.getName());
        adapter.notifyItemChanged(position);
        if (service.isInactiv()) {
            Log.d(TAG, "chooseService: Service in unavailable");
            Toast toast = Toast.makeText(context.getApplicationContext(), "Fida " + service.getName() + " is not available!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        
        Log.d(TAG, "chooseService: switch to WebIrActivity");
        Toast toast = Toast.makeText(context.getApplicationContext(), "Fida " + service.getName() + " chosen!", Toast.LENGTH_SHORT);
        toast.show();

        /*
        Intent intent = new Intent(context, WebIRActivity.class);
        intent.putExtra(INPUT_ACTIVITY_SERVICE, service);
        context.startActivityForResult(intent, INPUT_ACTIVITY_RESULT);
        */
    }

    private List<Service> load() {
        Log.i(TAG, "load: " + SHARED_PREFERENCES + " : " + SERVICES_LIST);
        List<Service> list = null;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString(SERVICES_LIST, null);
            Type type = new TypeToken<List<Service>>() {
            }.getType();
            list = gson.fromJson(json, type);
        } catch (Exception e) {
            Log.d(TAG, "load: aborted");
            Log.e(TAG, "load: ", e);
        }
        if (list == null) {
            Log.d(TAG, "load: No Entries");
            return new ArrayList<>();
        }
        
        String names = "";
        for (Service service : list)
            names += " " + service.getName();

        Log.i(TAG, "load: Entries:" + names);
        return list;
    }

    public void save() {
        Log.i(TAG, "save: " + SHARED_PREFERENCES + " : " + SERVICES_LIST);
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(list);
            editor.putString(SERVICES_LIST, json);
            editor.apply();
        } catch (Exception e) {
            Log.d(TAG, "save: aborted");
            Log.e(TAG, "save: ", e);
            return;
        }
        Log.d(TAG, "save: succeed");
    }

    public void addService(Service service) {
        if (service == null) {
            Log.d(TAG, "addService: No Service");
            return;
        }
        Log.i(TAG, "addService: " + service.getName());
        if (updateService(service)) {
            Log.d(TAG, "addService: updated the Service");
        
        } else {
            Log.d(TAG, "addService: add the Service");
            list.add(service);
            adapter.notifyItemInserted(list.size());
        }
    }

    public boolean updateService(Service service) {
        if (service == null) {
            Log.d(TAG, "updateService: No Service");
            return false;
        }
        Log.i(TAG, "updateService: " + service.getName());
        for (Service listService : list) {
            if (listService.getUri() == null) {
                if (listService.getName().equals(service.getName())) {
                    Log.d(TAG, "updateService: updated by Name");
                    int position = list.indexOf(listService);
                    list.set(position, service);
                    adapter.notifyItemChanged(position);
                    return true;
                }
            } else if (service.getUri() == null) {
                if (listService.getName().equals(service.getName())) {
                    Log.d(TAG, "updateService: updated by Name");
                    int position = list.indexOf(listService);
                    listService.setInactiv();
                    adapter.notifyItemChanged(position);
                    return true;
                }
            } else {
                if (listService.getUri().toString().equals(service.getUri().toString())) {
                    Log.d(TAG, "updateService: updated by URI: " + service.getUri().toString());
                    int position = list.indexOf(listService);
                    list.set(position, service);
                    adapter.notifyItemChanged(position);
                    return true;
                }
            }
        }
        Log.d(TAG, "updateService: Service could not be updated ");
        return false;
    }

    public boolean removeService(int position){
        if (position >= 0 && position < list.size()) {
            Log.i(TAG, "removeService: @ position: " + String.valueOf(position));
            list.remove(position);
            adapter.notifyItemRemoved(position);
            return true;
        }
        Log.d(TAG, "removeService: No Service");
        return false;
    }

    public boolean removeService(Service service) {
        if (service == null) {
            Log.d(TAG, "removeService: No Service");
            return false;
        }
        Log.i(TAG, "removeService: " + service.getName());
        for (Service listService : list) {
            if (listService.getUri() == null || service.getUri() == null) {
                if (listService.getName().equals(service.getName())) {
                    return removeService(list.indexOf(listService));
                }
            } else {
                if (listService.getUri().toString().equals(service.getUri().toString())) {
                    return removeService(list.indexOf(listService));
                }
            }
        }
        Log.d(TAG, "removeService: Service not found");
        return false;
    }

    public void setAllServicesInactive() {
        Log.i(TAG, "setAllServicesInactive: do it");
        for (Service service : list) {
            Log.d(TAG, "setAllServicesInactive: " + service.getName());
            service.setInactiv();
        }
    }

    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    public void showRemoveFida(boolean show) {
        Log.d(TAG, "showRemoveFida: " + show);
        this.showDelete = show;
        for (Service service : list) {
            int position = list.indexOf(service);
            service.showBucket(show);
            adapter.notifyItemChanged(position);
        }
    }

    public boolean isShowDelete() {
        return this.showDelete;
    }
}

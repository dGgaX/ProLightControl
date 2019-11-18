package de.abring.service;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.abring.internet.GetUrlContentTask;
import de.abring.internet.ServiceRequest;
import de.abring.prolightcontrol.R;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private Context context;
    private final List<Service> list;
    private final List<ServiceClickListener> listener;

    private static final String TAG = "ServiceAdapter";

    private RequestQueue queue;

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public ImageView removeIcon;
        public Switch powerSwitch;
        public TextView textName;
        public TextView textURI;

        public ServiceViewHolder(View itemView, final List<ServiceClickListener> listener) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.icon);
            this.removeIcon = itemView.findViewById(R.id.bucket);
            this.powerSwitch = itemView.findViewById(R.id.power);
            this.textName = itemView.findViewById(R.id.textViewName);
            this.textURI = itemView.findViewById(R.id.textViewURI);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (ServiceClickListener l : listener) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            l.onServiceClick(position);
                        }
                    }
                }
            });
            removeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (ServiceClickListener l : listener) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            l.onDeleteClick(position);
                        }
                    }
                }
            });
            powerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    for (ServiceClickListener l : listener) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            l.onSwitchClick(position, isChecked);
                        }
                    }
                }
            });
        }
    }

    public ServiceAdapter(Context context, RequestQueue queue, List<Service> list) {
        this.context = context;
        this.queue = queue;
        this.list = list;
        this.listener = new ArrayList<>();
    }

    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        ServiceViewHolder svh = new ServiceViewHolder(v, listener);
        return svh;
    }

    public void updateStates() {
        for (final Service service : list) {
            new ServiceRequest(service) {
                @Override
                public void finished(Service service) {
                    notifyDataSetChanged();
                }
            };

        }
    }

    @Override
    public void onBindViewHolder(final ServiceViewHolder holder, int position) {
        Service service = list.get(position);

        holder.icon.setImageResource(service.getIcon());
        if (service.isInactiv()) {
            //holder.icon.setBackgroundColor(get R.color.colorPrimaryDark);
        } else {
            //holder.icon.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
        if (service.isShowBucket()) {
            holder.removeIcon.setVisibility(View.VISIBLE);
            holder.powerSwitch.setVisibility(View.INVISIBLE);
        } else {
            holder.removeIcon.setVisibility(View.INVISIBLE);
            holder.powerSwitch.setVisibility(View.VISIBLE);
        }
        holder.textName.setText(service.getName());
        if (service.getUriAsString() != null) {
            holder.textURI.setText(service.getUriAsString());
            holder.powerSwitch.setChecked(service.powerOn);
        }
    }

    public void addServiceClickListener(ServiceClickListener l) {
        listener.add(l);
    }
    public void removeServiceClickListener(ServiceClickListener l) {
        listener.remove(l);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

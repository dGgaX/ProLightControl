package de.abring.service;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import work.fida.fidaremote.R;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private Context context;
    private final List<Service> list;
    private final List<ServiceClickListener> listener;


    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public ImageView removeIcon;
        public TextView textName;
        public TextView textURI;

        public ServiceViewHolder(View itemView, final List<ServiceClickListener> listener) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.icon);
            this.removeIcon = itemView.findViewById(R.id.bucket);
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

        }
    }

    public ServiceAdapter(Context context, List<Service> list) {
        this.context = context;
        this.list = list;
        this.listener = new ArrayList<>();
    }

    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        ServiceViewHolder svh = new ServiceViewHolder(v, listener);
        return svh;
    }



    @Override
    public void onBindViewHolder(ServiceViewHolder holder, int position) {
        Service service = list.get(position);

        holder.icon.setImageResource(service.getIcon());
        if (service.isInactiv()) {
            holder.icon.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentTwo));
        } else {
            holder.icon.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
        if (service.isShowBucket()) {
            holder.removeIcon.setVisibility(View.VISIBLE);
        } else {
            holder.removeIcon.setVisibility(View.INVISIBLE);
        }
        holder.textName.setText(service.getName());
        if (service.getUriAsString() != null) {
            holder.textURI.setText(service.getUriAsString());
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

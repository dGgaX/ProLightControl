package de.abring.service;

public interface ServiceClickListener {
    void onServiceClick(int position);
    void onDeleteClick(int position);
    void onSwitchClick(int position, boolean powerOn);
}

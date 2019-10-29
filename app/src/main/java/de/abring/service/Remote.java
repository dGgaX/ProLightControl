package de.abring.service;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Remote implements Serializable {
    public String name;

    public Remote(String name) {
        this.name = name;
    }
    public Remote (String name, JSONArray commands) {
        this.name = name;
    }
}

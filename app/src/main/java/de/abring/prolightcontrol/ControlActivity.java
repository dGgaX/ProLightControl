package de.abring.prolightcontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.slider.LightnessSlider;

import org.json.JSONException;
import org.json.JSONObject;

import de.abring.internet.ServiceRequest;
import de.abring.service.Service;
import de.abring.service.ServiceClickListener;

public class ControlActivity extends AppCompatActivity {

    private static final String TAG = "ControlActivity";
    private static final String SERVICE = "service";

    private RequestQueue queue;
    private Service service;

    private ColorPickerView colorPickerView;
    private Switch powerSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        queue = Volley.newRequestQueue(this);
        if (savedInstanceState != null) {
            service = (Service) savedInstanceState.getSerializable(SERVICE);
        }
        if (service == null) {
            if (getIntent().hasExtra("service")) {
                service = (Service) getIntent().getExtras().get("service");
            } else {
                finish();
            }
        }
        service.setQueue(queue);

        colorPickerView = findViewById(R.id.color_picker_view);
        colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override public void onColorChanged(int selectedColor) {
                service.setColor(selectedColor);
            }
        });
        colorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                service.setColor(selectedColor);
            }
        });


        powerSwitch = findViewById(R.id.power_control);
        powerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                service.setPowerOn(isChecked);
            }
        });

        colorPickerView.setColor(service.mixedColor,true);
        powerSwitch.setChecked(service.powerOn);
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

}

package com.tels.assignment.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tels.assignment.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceSensorActivity extends AppCompatActivity implements SensorEventListener {
    @BindView(R.id.HeartRateTxt)
    TextView txtHeartRate;
    private boolean isSensorPresent = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, 1);
            }
        }

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            mSensorManager.registerListener(this, mSensor, 3); //I am using "3" as it is said to provide best accuracy ¯\_(ツ)_/¯
            isSensorPresent = true;
        } else {
            txtHeartRate.setText("Heart rate sensor is not present!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent) {
            mSensorManager.registerListener(this, mSensor, 3);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged (SensorEvent event){
        if (isSensorPresent) {
            if ((int) event.values[0] != 0) {
                txtHeartRate.setText("Current heart rate: " + Math.round(event.values[0]) + " BPM");
            }
        }
    }

    @Override
    public void onAccuracyChanged (Sensor sensor,int i){ }
}

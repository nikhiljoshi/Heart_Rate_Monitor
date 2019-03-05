package com.tels.assignment.ui.activities;

import android.support.v7.app.AppCompatActivity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

import com.tels.assignment.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BatteryInformationActivity extends AppCompatActivity {

    Context mContext;
    @BindView(R.id.tv_battery_present)
    TextView tvBatteryPresent;
    @BindView(R.id.tv_battery_health)
    TextView tvBatteryHealth;
    @BindView(R.id.tv_battery_level)
    TextView tvBatteryLevel;
    @BindView(R.id.tv_battery_power_source)
    TextView tvBatteryPowerSource;
    @BindView(R.id.tv_battery_status)
    TextView tvBatteryStatus;
    @BindView(R.id.tv_battery_technology)
    TextView tvBatteryTechnology;
    @BindView(R.id.tv_battery_temperature)
    TextView tvBatteryTemperature;
    @BindView(R.id.tv_battery_voltage)
    TextView tvBatteryVoltage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        mContext = BatteryInformationActivity.this;
        ButterKnife.bind(this);
        loadBatteryData();
    }



    void loadBatteryData() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryInfoReceiver, intentFilter);
    }


    BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryData(intent);
        }
    };

    void updateBatteryData(Intent intent) {
        boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);

        if (present) {

            tvBatteryPresent.setText(R.string.battery_present_yes);

            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int healthLbl = -1;

            switch (health) {
                case BatteryManager.BATTERY_HEALTH_COLD:
                    healthLbl = R.string.battery_health_cold;
                    break;

                case BatteryManager.BATTERY_HEALTH_DEAD:
                    healthLbl = R.string.battery_health_dead;
                    break;

                case BatteryManager.BATTERY_HEALTH_GOOD:
                    healthLbl = R.string.battery_health_good;
                    break;

                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    healthLbl = R.string.battery_health_over_voltage;
                    break;

                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    healthLbl = R.string.battery_health_overheat;
                    break;

                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    healthLbl = R.string.battery_health_unspecified_failure;
                    break;

                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                default:
                    break;
            }

            if (healthLbl != -1) {
                // display battery health ...
                tvBatteryHealth.setText(getString(healthLbl));
            }

            // Calculate Battery Pourcentage ...
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (level != -1 && scale != -1) {
                int batteryPct = (int) ((level / (float) scale) * 100f);
                tvBatteryLevel.setText(batteryPct + " %");
            }

            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            int pluggedLbl = R.string.battery_plugged_none;

            switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    pluggedLbl = R.string.battery_plugged_wireless;
                    break;

                case BatteryManager.BATTERY_PLUGGED_USB:
                    pluggedLbl = R.string.battery_plugged_usb;
                    break;

                case BatteryManager.BATTERY_PLUGGED_AC:
                    pluggedLbl = R.string.battery_plugged_ac;
                    break;

                default:
                    pluggedLbl = R.string.battery_plugged_none;
                    break;
            }

            // display plugged status ...
            tvBatteryPowerSource.setText(getString(pluggedLbl));

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int statusLbl = R.string.battery_status_discharging;

            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusLbl = R.string.battery_status_charging;
                    break;

                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusLbl = R.string.battery_status_discharging;
                    break;

                case BatteryManager.BATTERY_STATUS_FULL:
                    statusLbl = R.string.battery_status_full;
                    break;

                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    statusLbl = -1;
                    break;

                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                default:
                    statusLbl = R.string.battery_status_discharging;
                    break;
            }

            if (statusLbl != -1) {
                tvBatteryStatus.setText(getString(statusLbl));
            }

            if (intent.getExtras() != null) {
                String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);

                if (!"".equals(technology)) {
                    tvBatteryTechnology.setText(technology);
                }
            }

            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);

            if (temperature > 0) {
                float temp = ((float) temperature) / 10f;
                tvBatteryTemperature.setText(temp + "°C");
            }

            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

            if (voltage > 0) {
                tvBatteryVoltage.setText(voltage + " mV");
            }


        } else {
            tvBatteryPresent.setText(R.string.battery_present_none);
        }
    }


}
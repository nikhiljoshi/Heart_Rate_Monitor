package com.tels.assignment.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.tels.assignment.R;
import com.tels.assignment.utility.SampleGattAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_COARSE_LOCATION = 0;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT_CHAR);
    @BindView(R.id.start)
    Button start;
    @BindView(R.id.stop)
    Button stop;
    @BindView(R.id.sensorScan)
    Button sensor;
    @BindView(R.id.batteryinfo)
    Button battery;
    @BindView(R.id.result)
    ListView result;
    private Toolbar toolbar;
    private RxBluetooth rxBluetooth;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<BleDevice> devices = new ArrayList<>();
    private Intent bluetoothServiceIntent;

    private ProgressDialog mProgressDialog;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_search);
        ButterKnife.bind(this);




        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        BleManager.getInstance().enableBluetooth();

        startScan();

        start.setOnClickListener(v -> startScan());

        stop.setOnClickListener(v -> stopScan());

        sensor.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DeviceSensorActivity.class);
            startActivity(intent);
        });
        battery.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BatteryInformationActivity.class);
            startActivity(intent);
        });

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle(getString(R.string.loading_msg));
        mProgressDialog.setMessage(getString(R.string.msg_wait));
        mProgressDialog.setCancelable(false);
    }

    @Override protected void onDestroy() {
        super.onDestroy();

    }


    private void stopScan()
    {
        BleManager.getInstance().cancelScan();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_COARSE_LOCATION) {
            for (String permission : permissions) {
                if (android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
                    // Start discovery if permission granted
                    startScan();

                }
            }
        }
    }

    private void startScan()
    {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mProgressDialog.show();

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                mProgressDialog.dismiss();
                setAdapter(scanResultList);

            }
        });

    }






    private void setAdapter(List<BleDevice> list) {
        int itemLayoutId = android.R.layout.simple_list_item_1;
        result.setAdapter(new ArrayAdapter<BleDevice>(this, android.R.layout.simple_list_item_2,
                android.R.id.text1, list) {
            @NonNull @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                BleDevice device = list.get(position);
                String devName = device.getName();
                String devAddress = device.getMac();

                if (TextUtils.isEmpty(devName)) {
                    devName = "NO NAME";
                }
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(devName);
                text2.setText(devAddress);
                return view;
            }
        });

        result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //connect(list.get(position));

                Intent intent = new Intent(MainActivity.this, HeartChartActivity.class);
                intent.putExtra("BleDevice", list.get(position));
                startActivity(intent);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }



}

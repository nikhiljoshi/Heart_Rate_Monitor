package com.tels.assignment.ui.activities;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.tels.assignment.R;
import com.tels.assignment.utility.SampleGattAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.reactivex.disposables.CompositeDisposable;

import static com.tels.assignment.utility.SampleGattAttributes.HEART_RATE_MEASUREMENT1;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_COARSE_LOCATION = 0;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT_CHAR);
    private Button start;
    private Button stop;
    private ListView result;
    private Toolbar toolbar;
    private RxBluetooth rxBluetooth;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<BleDevice> devices = new ArrayList<>();
    private Intent bluetoothServiceIntent;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_search);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        result = (ListView) findViewById(R.id.result);


        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        BleManager.getInstance().enableBluetooth();

        startScan();

    }

    @Override protected void onDestroy() {
        super.onDestroy();

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

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                setAdapter(scanResultList);

            }
        });

    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

                Toast.makeText(MainActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gdatt, int status) {
                Toast.makeText(MainActivity.this, getString(R.string.connect_success), Toast.LENGTH_LONG).show();


                BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);

                BluetoothGattService service=   gatt.getService(UUID_HEART_RATE_MEASUREMENT);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(HEART_RATE_MEASUREMENT1));

                BleManager.getInstance().notify(
                        bleDevice,
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        new BleNotifyCallback() {

                            @Override
                            public void onNotifySuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }

                            @Override
                            public void onNotifyFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int flag = characteristic.getProperties();
                                        int format = -1;

                                        if ((flag & 0x01) != 0) {
                                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                                        } else {
                                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                                        }

                                        final int heartRate = characteristic.getIntValue(format, 1);
                                        Log.e("----","----"+heartRate);


                                    }
                                });
                            }
                        });



                String name = bleDevice.getName();
                String mac = bleDevice.getMac();





            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Toast.makeText(MainActivity.this, getString(R.string.connect_disconnected), Toast.LENGTH_LONG).show();

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


package com.tels.assignment.ui.activities;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tels.assignment.R;

import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

import static com.tels.assignment.ui.activities.MainActivity.UUID_HEART_RATE_MEASUREMENT;
import static com.tels.assignment.utility.SampleGattAttributes.HEART_RATE_MEASUREMENT1;

public class HeartChartActivity extends AppCompatActivity implements
        OnChartValueSelectedListener, SensorEventListener {

    private LineChart mLineChart;
    private  BleDevice bleDevice;
    private boolean isSensorPresent = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_linechart);

        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            bleDevice = (BleDevice) extras.getParcelable("BleDevice");
            connectToDevice(bleDevice);

            setTitle("Heart Chart from HRM Bluetooth");
        }
        else {
            mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

            if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
                mSensorManager.registerListener(HeartChartActivity.this, mSensor, 3);
                isSensorPresent = true;
            }
        }
        initChartView();

    }

    private  void initChartView()
    {
        mLineChart = findViewById(R.id.chart1);
        mLineChart.setOnChartValueSelectedListener(this);

        // enable description text
        mLineChart.getDescription().setEnabled(true);
        Description description = new Description();
        description.setText("heart rate");
        mLineChart.setDescription(description);

        // enable touch gestures
        mLineChart.setTouchEnabled(true);

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
        mLineChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(true);

        // set an alternative background color
        mLineChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty extras
        mLineChart.setData(data);

        // get the legend (only possible after setting extras)
        Legend l = mLineChart.getLegend();

        // modify the legend ...
        l.setForm(LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mLineChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(160f);
        leftAxis.setAxisMinimum(60f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }


    private void addEntry(int heartRate) {

        LineData data = mLineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) heartRate), 0);
            data.notifyDataChanged();

            // let the mLineChart know it's data has changed
            mLineChart.notifyDataSetChanged();

            // limit the number of visible entries
            mLineChart.setVisibleXRangeMaximum(120);
            // mLineChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mLineChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the mLineChart (calls invalidate())
            // mLineChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "HEART RATE");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private Thread thread;

    private void feedMultiple(int heartRate) {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry(heartRate);
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }






    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
        BleManager.getInstance().disconnect(bleDevice);
    }



    private void connectToDevice(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

                Toast.makeText(HeartChartActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gdatt, int status) {
                Toast.makeText(HeartChartActivity.this, getString(R.string.connect_success), Toast.LENGTH_LONG).show();


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
                                        addEntry(heartRate);
                                        Toast.makeText(HeartChartActivity.this, getString(R.string.heart_rate)+heartRate, Toast.LENGTH_LONG).show();

                                    }
                                });
                            }
                        });



                String name = bleDevice.getName();
                String mac = bleDevice.getMac();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Toast.makeText(HeartChartActivity.this, getString(R.string.connect_disconnected), Toast.LENGTH_LONG).show();

            }
        });
    }



    @Override
    public void onSensorChanged (SensorEvent event){
        if (isSensorPresent) {
            if ((int) event.values[0] != 0) {
                //txtHeartRate.setText("Current heart rate: " + Math.round(event.values[0]) + " BPM");
                Toast.makeText(HeartChartActivity.this, getString(R.string.heart_rate)+Math.round(event.values[0]), Toast.LENGTH_LONG).show();
                addEntry(Math.round(event.values[0]));

            }
        }
    }

    @Override
    public void onAccuracyChanged (Sensor sensor,int i){ }
}

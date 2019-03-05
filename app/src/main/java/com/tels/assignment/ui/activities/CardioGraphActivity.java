package com.tels.assignment.ui.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.tels.assignment.R;
import com.tels.assignment.database.AppDatabase;
import com.tels.assignment.database.GraphItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.aflak.bluetooth.Bluetooth;

import static java.lang.Integer.parseInt;

public class CardioGraphActivity extends AppCompatActivity implements Bluetooth.CommunicationCallback {
    private String name;
    private Bluetooth mBluetooth;

    @BindView(R.id.send)
    Button send;
    @BindView(R.id.history)
    Button history;
    @BindView(R.id.save)
    Button save;
    @BindView(R.id.results)
    Button results;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    private boolean registered=false;
    @BindView(R.id.graph)
    GraphView graph;
    @BindView(R.id.bpm)
    TextView bpmText;

    private LineGraphSeries<DataPoint> mSeries;



    int counter=0;
    String values;
    int flagWait=0;
    //FlagValue 0 --> Ready to receive , Cant Show
    //          1 --> Cant Do anything
    //          2 --> Have received , ready to show/save
    int flagValue=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardio_graph);
        ButterKnife.bind(this);


        graph.getViewport().setScrollableY(true);
        graph.getViewport().setScalable(true);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        send.setEnabled(false);

        mBluetooth = new Bluetooth(this);
        mBluetooth.enableBluetooth();

        mBluetooth.setCommunicationCallback(this);

        int pos = getIntent().getExtras().getInt("pos");
        name = mBluetooth.getPairedDevices().get(pos).getName();


        Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
        mBluetooth.connectToDevice(mBluetooth.getPairedDevices().get(pos));

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flagWait=0;
                values="";
                counter=0;
                String msg = "m";
                mBluetooth.send(msg);
                flagValue=0;
                Toast.makeText(CardioGraphActivity.this, "Taking Test...", Toast.LENGTH_LONG).show();
            }
        });
        results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flagValue==2) {
                    mSeries.resetData(generateData());
                    int bpm=Bpm();
                    calcDanger(bpm);
                }else{
                    Toast.makeText(CardioGraphActivity.this, "You Haven't Taken a Heart Rate Test", Toast.LENGTH_SHORT).show();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("flag","flagValue"+flagValue);
                if (flagValue==2){
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    GraphItem graphItem = new GraphItem(10,values,date.toString());
                    AppDatabase.getDatabase(CardioGraphActivity.this).personInfoDao().addPersonInfo(graphItem);
                    Toast.makeText(CardioGraphActivity.this, "Graph Successfully Saved", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(CardioGraphActivity.this, "Nothing to Save", Toast.LENGTH_SHORT).show();
                }
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CardioGraphActivity.this, HistoryActivity.class);
                startActivity(i);
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered=true;

        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(1200);
        graph.getViewport().setXAxisBoundsManual(true);
    }

    private void calcDanger(int bpm){
        double min=60;
        double max=100;
        double dBpm=(double)bpm;
        if (dBpm<min){
            Toast.makeText(this, "Your heart rate is below normal", Toast.LENGTH_SHORT).show();
        }else if(dBpm>max){
            Toast.makeText(this, "Your heart rate is above normal", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Your heart rate is normal", Toast.LENGTH_SHORT).show();
        }
    }

    private int Bpm() {
        int beats=0;
        String[] parts=values.split(" ");
        int count = parts.length;
        //Log.d("Length", String.valueOf(count));
        for (int i=0; i<count; i++) {
            int max=0;
            while(Integer.parseInt(parts[i])>650){
                if(Integer.parseInt(parts[i])>max){
                    max= Integer.parseInt(parts[i]);
                }
                i++;
            }
            if (max!=0){
                beats++;
            }
        }
        //Log.d("Beats", String.valueOf(beats));
        float time = (float) (count*0.006);
        int bpm= (int) (beats*60/time);
        bpmText.setText(Integer.toString(bpm));
        return bpm;
    }

    private DataPoint[] generateData() {
        String[] parts=values.split(" ");
        //Log.d("Debug", "parts=" + values);
        int count = parts.length;
        DataPoint[] dataValues = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double y= parseInt(parts[i]);
            DataPoint v = new DataPoint(x, y);
            dataValues[i] = v;
        }
        return dataValues;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registered) {
            unregisterReceiver(mReceiver);
            registered=false;
        }
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        //Display("Connected to "+device.getName()+" - "+device.getAddress());
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                send.setEnabled(true);
            }
        });
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Toast.makeText(this, "Disconnected!", Toast.LENGTH_SHORT).show();
        mBluetooth.connectToDevice(device);
    }

    @Override
    public void onMessage(String message) {
        //Log.d("Debug","message="+message);
        flagWait++;
        if (flagWait>5 && flagValue==0) {
            if (counter < 1200) {
                if (counter == 0) {
                    values = message;
                    values += " ";
                } else {
                    values += message;
                    values += " ";
                }
                //Log.d("Debug", "parts=" + values);
                counter += 20;
                //Log.d("Flag","flagValue="+flagValue);
            }else{
                Log.d("Debug", "parts=" + values);
                flagValue=2;
            }
        }
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Error: "+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectError(final BluetoothDevice device, String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetooth.connectToDevice(device);
                    }
                }, 2000);
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Intent intent1 = new Intent(CardioGraphActivity.this, SelectBluetoothActivity.class);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if(registered) {
                            unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(registered) {
                            unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                }
            }
        }
    };
}
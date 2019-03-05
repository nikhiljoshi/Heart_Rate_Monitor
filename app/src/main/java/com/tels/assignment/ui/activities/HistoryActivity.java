package com.tels.assignment.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.tels.assignment.R;
import com.tels.assignment.adapter.ProductDataAdapter;
import com.tels.assignment.database.AppDatabase;
import com.tels.assignment.database.GraphItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HistoryActivity extends AppCompatActivity {

    @BindView(R.id.listView)
    RecyclerView listView;
    private LineGraphSeries<DataPoint> mSeries;
    @BindView(R.id.graph)
    GraphView graph;
    //Button reset;
    @BindView(R.id.date)
    TextView dateText;
    @BindView(R.id.bpm)
    TextView  bpmText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);



        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getViewport().setScrollableY(true);
        graph.getViewport().setScalable(true);


        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(1200);
        graph.getViewport().setXAxisBoundsManual(true);



        ArrayList<GraphItem> graphItem = (ArrayList<GraphItem>) AppDatabase.getDatabase(HistoryActivity.this).personInfoDao().getAllData();


        ProductDataAdapter adapter = new ProductDataAdapter(graphItem, HistoryActivity.this);



        listView.setAdapter(adapter);


        listView.setAdapter(adapter);

      /*  listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor itemCursor = (Cursor) HistoryActivity.this.listView.getItemAtPosition(position);
                String values = itemCursor.getString(itemCursor.getColumnIndex(DBHelper.PERSON_COLUMN_VALUE));
                String date = itemCursor.getString(itemCursor.getColumnIndex(DBHelper.PERSON_COLUMN_DATE));
                Log.d("graph",values);
                mSeries.resetData(generateData(values));
                Bpm(values);
                dateText.setText(date);
            }
        });*/


        /*
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // db.delete(String tableName, String whereClause, String[] whereArgs);
                // If whereClause is null, it will delete all rows.
                SQLiteDatabase db = dbHelper.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
                db.delete(DBHelper.PERSON_COLUMN_VALUE, null, null);
                db.delete(DBHelper.PERSON_COLUMN_DATE, null, null);
                db.delete(DBHelper.PERSON_COLUMN_ID,null,null);
            }
        });
        */

    }

    private DataPoint[] generateData(String values) {
        String[] parts=values.split(" ");
        int count = parts.length;
        DataPoint[] dataValues = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double y= Integer.parseInt(parts[i]);
            DataPoint v = new DataPoint(x, y);
            dataValues[i] = v;
        }
        return dataValues;
    }

    private void Bpm(String values) {
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
    }


}

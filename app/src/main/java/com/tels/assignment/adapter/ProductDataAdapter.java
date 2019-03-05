package com.tels.assignment.adapter;

/**
 * Created by Nikhil Joshi
 *
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tels.assignment.R;
import com.tels.assignment.database.GraphItem;

import java.util.ArrayList;


/**
 */
public class ProductDataAdapter extends RecyclerView.Adapter<ProductDataAdapter.ViewHolder> {
    private ArrayList<GraphItem> mProductDataSet;
    private Context mContext;


    /* you provide access to all the views for a data item in a view holder*/
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtGraphID;
        public TextView txtGraphDate;


        public ViewHolder(View v) {
            super(v);
            txtGraphID = (TextView) v.findViewById(R.id.graphID);
            txtGraphDate = (TextView) v.findViewById(R.id.graphDate);
        }
    }


    public ProductDataAdapter(ArrayList<GraphItem> listData, Context context) {
        mProductDataSet = listData;
        mContext=context;
    }

    /***
     *  Create new views (invoked by the layout manager)*/
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.graph_info, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    /**
     *  Replace the contents of a view (invoked by the layout manager)*/
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        /***
         *  get element from dataset at this position
         replace the contents of the view with that element*/

        holder.txtGraphID.setText(mProductDataSet.get(position).getDataId());
        holder.txtGraphDate.setText(mProductDataSet.get(position).getValue());

    }

    /***
     *  Return the size of your dataset */
    @Override
    public int getItemCount() {
        return mProductDataSet.size();
    }

}
package com.tels.assignment.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

/**
 * Created by Nikhil
 */
@Entity(tableName = "graphs")
public class GraphItem {
    @PrimaryKey
    private int dataId;
    private String value;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }



    public GraphItem(int id,String value, String date) {
        this.dataId = id;
        this.value = value;
        this.date = date;
    }


    GraphItem()
    {

    }


}

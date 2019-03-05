package com.tels.assignment.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikhil
 */

@Dao
public interface PersonInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addPersonInfo(GraphItem productItem);

    @Query("select * from graphs")
    public List<GraphItem> getAllData();



    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateProduct(GraphItem productItem);

    @Query("delete from graphs")
    void removeAllData();

    @Query("DELETE FROM graphs WHERE dataId = :productId")
    int delete(final int productId);

}

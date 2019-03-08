package com.tels.assignment.database;


import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
    void updateData(GraphItem graphItem);

    @Query("delete from graphs")
    void removeAllData();

    @Query("DELETE FROM graphs WHERE dataId = :productId")
    int delete(final int productId);

}

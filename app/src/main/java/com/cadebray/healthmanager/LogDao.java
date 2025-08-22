package com.cadebray.healthmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// Suppressed Warnings for unused to better utilize the IDE problems debugging.
@SuppressWarnings("unused")
@Dao
public interface LogDao {
    @Insert
    long insert(Log log);

    @Update
    void update(Log log);

    @Delete
    void delete(Log log);

    @Query("SELECT * FROM health_logs WHERE date = :date AND username = :username ORDER BY time ASC")
    List<Log> getLogsByDate(String date, String username);

    @Query("SELECT * FROM health_logs WHERE username = :username ORDER BY date DESC, time DESC")
    List<Log> getLogs(String username);

    @Query("SELECT * FROM health_logs WHERE id = :id AND username = :username")
    Log getLog(int id, String username);

    @Query("DELETE FROM health_logs WHERE id = :id AND username = :username")
    void deleteLog(int id, String username);
}

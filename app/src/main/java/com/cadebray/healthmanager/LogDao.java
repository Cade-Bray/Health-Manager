package com.cadebray.healthmanager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

// Suppressed Warnings for unused to better utilize the IDE problems debugging.
@SuppressWarnings("unused")
@Dao
public interface LogDao {
    @Insert
    void insert(Log log);

    @Query("SELECT * FROM health_logs WHERE date = :date AND username = :username ORDER BY time ASC")
    List<Log> getLogsByDate(String date, String username);

    @Query("SELECT * FROM health_logs WHERE username = :username ORDER BY date DESC, time DESC")
    List<Log> getLogs(String username);

    @Query("DELETE FROM health_logs WHERE id = :id AND username = :username")
    void deleteLog(int id, String username);
}

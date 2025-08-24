package com.cadebray.healthmanager;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@SuppressWarnings("unused")
@Database(entities = {Log.class}, version = 1)
public abstract class LogDatabase extends RoomDatabase {
    @SuppressWarnings("unused")
    public abstract LogDao logDao();

    private static volatile LogDatabase INSTANCE;

    @SuppressWarnings("unused")
    public static LogDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LogDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LogDatabase.class, "log_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

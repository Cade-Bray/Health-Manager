package com.cadebray.healthmanager;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@SuppressWarnings("unused")
@Database(entities = {User.class}, version = 1)
public abstract class UserDatabase extends RoomDatabase {
    @SuppressWarnings("unused")
    public abstract UserDao userDao();
    private static volatile UserDatabase INSTANCE;

    @SuppressWarnings("unused")
    public static UserDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UserDatabase.class, "user_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
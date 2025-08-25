package com.cadebray.healthmanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@SuppressWarnings("unused")
@Database(entities = {User.class}, version = 3)
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
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * This migration adds the goal and units columns to the users table.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN goal TEXT DEFAULT '200'");
            database.execSQL("ALTER TABLE users ADD COLUMN units TEXT DEFAULT 'lbs'");
        }
    };

    /**
     * This migration adds the phone number column to the users table.
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT DEFAULT '-1'");
        }
    };
}
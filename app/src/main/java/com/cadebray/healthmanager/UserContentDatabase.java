package com.cadebray.healthmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Deprecated
public class UserContentDatabase extends SQLiteOpenHelper {

    public static class WeightLog {
        public int id;
        public float weight;
        public String weightUnit;
        public LocalDateTime timestamp = LocalDateTime.now();
    }

    private static final String DATABASE_NAME = "user_content.db";
    private static final int VERSION = 2;

    public UserContentDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class WeightTable {
        private static final String TABLE = "weight";
        private static final String COL_ID = "_id";
        private static final String COL_WEIGHT = "weight_value";
        private static final String COL_UNIT = "unit";
        private static final String COL_DATETIME = "datetime";

    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + WeightTable.TABLE + " (" +
                WeightTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WeightTable.COL_WEIGHT + " TEXT, " +
                WeightTable.COL_UNIT + " TEXT, " +
                WeightTable.COL_DATETIME + " DATETIME)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("drop table if exists " + WeightTable.TABLE);
        onCreate(db);
    }

    @SuppressWarnings("unused")
    public long logWeight(WeightLog weightLog) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WeightTable.COL_WEIGHT, weightLog.weight);
        values.put(WeightTable.COL_UNIT, weightLog.weightUnit);
        values.put(WeightTable.COL_DATETIME, String.valueOf(weightLog.timestamp));

        return db.insert(WeightTable.TABLE, null, values);
    }

    public boolean removeLoggedWeight(long id) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.execSQL("DELETE FROM " + WeightTable.TABLE +
                    " WHERE _id = " + id);
        } catch (Exception e) {
            Log.e("UserContentDatabase", "Error removing weight: " + e.getMessage());
            return false;
        }

        db.close();
        return true;
    }

    public WeightLog[] getWeights() {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + WeightTable.TABLE;
        String[] selectionArgs = new String[]{};

        Cursor cursor = db.rawQuery(query, selectionArgs);
        WeightLog[] logEntries = new WeightLog[cursor.getCount()];
        cursor.moveToFirst();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        int counter = 1;
        if (cursor.getCount() > 0) {
            do {
                WeightLog logEntry = new WeightLog();
                logEntry.id = cursor.getInt(0);
                logEntry.weight = cursor.getFloat(1);
                logEntry.weightUnit = cursor.getString(2);
                try {
                    logEntry.timestamp = LocalDateTime.parse(cursor.getString(3), formatter);
                } catch (Exception e) {
                    logEntry.timestamp = LocalDateTime.parse(cursor.getString(3), formatter2);
                }
                logEntries[logEntries.length - counter] = logEntry;
                counter++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return logEntries;
    }
}

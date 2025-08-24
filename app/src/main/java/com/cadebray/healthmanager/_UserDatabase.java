package com.cadebray.healthmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.Objects;

@Deprecated
public class _UserDatabase extends SQLiteOpenHelper {

    public static class UserData {
        public int id;
        public String email;
        public boolean isAuthorized = false;
        public boolean isAuthenticated = false;
    }

    private static final String DATABASE_NAME = "users.db";
    private static final int VERSION = 2;

    public _UserDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class UsersTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "_id";
        private static final String COL_EMAIL = "email";
        private static final String COL_PASSWORD = "password";
        private static final String COL_AUTHORIZED = "authorized";

    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + UsersTable.TABLE + " (" +
                UsersTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UsersTable.COL_EMAIL + " TEXT UNIQUE, " +
                UsersTable.COL_PASSWORD + " TEXT, " +
                UsersTable.COL_AUTHORIZED + " INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("drop table if exists " + UsersTable.TABLE);
        onCreate(db);
    }

    public long addUser(String email, String password, boolean authorized) {
        //TODO Check the database to see if the user already exists.

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UsersTable.COL_EMAIL, email);
        values.put(UsersTable.COL_PASSWORD, password);
        values.put(UsersTable.COL_AUTHORIZED, authorized);

        return db.insert(UsersTable.TABLE, null, values);
    }

    public boolean removeUser(String email) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.execSQL("DELETE FROM " + UsersTable.TABLE +
                    " WHERE email = " + email);
        } catch (Exception e) {
            return false;
        }

        db.close();
        return true;
    }

    public String[] getUser(String email) {
        String[] user = new String[4];
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + UsersTable.TABLE + " WHERE " + UsersTable.COL_EMAIL +
                " = ?";
        String[] selectionArgs = new String[]{email};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (!cursor.isNull(0) && cursor.moveToFirst()) {
            // Get the ID
            user[0] = String.valueOf(cursor.getInt(0));

            // Get the Email (Should be the same)
            user[1] = cursor.getString(1);
            if (Objects.equals(email, user[1])) {
                throw new SecurityException("Queried Email is different from returned Email.");
            }

            // Get the password of the user
            user[2] = cursor.getString(2);

            // Get the authorization
            user[3] = String.valueOf(cursor.getInt(3));
        }

        cursor.close();
        db.close();

        return user;
    }

    /**
     * This function will return the helper class UserData containing user information. Ideally this
     * functionality would only be called on a authentication server that receives a payload from app
     * containing salted and hashed password with the email. All of which would be encrypted in the
     * payload. The server would handle the comparisons and return if the user is authorized for the
     * application service. This would enable a robust security measure to ensure integrity and ensure
     * users data is secure. Currently we're just doing user comparisons on the sqlite3 database
     * constructed on the user device with plain text comparisons. This is for the sake of prototyping.
     * @param email is the email to query against.
     * @param password Password in plain text. For prototyping we're not salting or hashing passwords.
     * @return UserData{id : INT, email : String, isAuthorized : boolean, isAuthenticated : boolean}
     */
    public UserData authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        UserData authenticatedUser = new UserData();
        authenticatedUser.email = email;
        String query = "SELECT " +
                UsersTable.COL_ID + ", " +
                UsersTable.COL_EMAIL + ", " +
                UsersTable.COL_PASSWORD + ", " +
                UsersTable.COL_AUTHORIZED +
                " FROM " + UsersTable.TABLE +
                " WHERE " + UsersTable.COL_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        try {
            if (cursor.moveToFirst()) {
                String queried_password = cursor.getString(2);
                if (!queried_password.equals(password)) {
                    authenticatedUser.id = cursor.getInt(0);
                    authenticatedUser.isAuthenticated = false;
                    authenticatedUser.isAuthorized = false;
                    Log.i("Database", "Failed authentication.");
                } else {
                    authenticatedUser.id = cursor.getInt(0);
                    authenticatedUser.isAuthenticated = true;
                    authenticatedUser.isAuthorized = (cursor.getInt(3) == 1);
                    Log.i("Database", "Successfully authenticated");
                }
            }
        } catch (Exception e) {
            Log.e("Database", "Error authenticating user:" + e.getMessage());
        }

        db.close();
        cursor.close();
        return authenticatedUser;
    }
}

package com.example.farouk.mapharmconsdz;

/**
 * Created by hp on 03/05/2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

  //  private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_ID_PHAR = "id_phar";
    private static final String KEY_TYPE = "type";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT," + KEY_CREATED_AT + " TEXT," + KEY_ID_PHAR + " TEXT," + KEY_TYPE + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.i("Test", "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser2(String name, String pseudo, String uid, String created_at, String id_phar, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, pseudo); // Pseudo
        values.put(KEY_UID, uid);
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_ID_PHAR, id_phar);
        values.put(KEY_TYPE, type);

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.i("Test", "New user inserted into sqlite: " + id +"the Id_phar = "+id_phar+" type = "+type);
    }
    public void addUser(String name, String pseudo, String uid, String created_at, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, pseudo); // Pseudo
        values.put(KEY_UID, uid);
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_TYPE, type);

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.i("Test", "From SQLiteHandler.class : New user inserted into sqlite: " + id +"name : "+name+" pseudo : "+pseudo+" uid : "+uid+" created_at : "+created_at+ " type : "+type);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
            user.put("id_phar", cursor.getString(5));
            user.put("type", cursor.getString(6));

        }
        cursor.close();
        db.close();
        // return user
        Log.i("Test", "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.i("Test", "Deleted all user info from sqlite");
    }

}

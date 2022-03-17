package com.capstone.autism_training;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SuperMemoTableManager {

    private SuperMemoTableHelper superMemoTableHelper;

    private final Context context;

    private SQLiteDatabase database;

    public SuperMemoTableManager(Context c) {
        context = c;
    }

    public void open(String table_name) throws SQLException {
        superMemoTableHelper = new SuperMemoTableHelper(context, table_name);
        database = superMemoTableHelper.getWritableDatabase();
    }

    public void close() {
        superMemoTableHelper.close();
    }

    public long insert(int repetitions, int interval, double easiness) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SuperMemoTableHelper.REPETITIONS, repetitions);
        contentValues.put(SuperMemoTableHelper.INTERVAL, interval);
        contentValues.put(SuperMemoTableHelper.EASINESS, easiness);
        return database.insert(SuperMemoTableHelper.TABLE_NAME, null, contentValues);
    }

    public Cursor fetch() {
        String[] columns = new String[] { SuperMemoTableHelper._ID, SuperMemoTableHelper.REPETITIONS, SuperMemoTableHelper.INTERVAL, SuperMemoTableHelper.EASINESS };
        Cursor cursor = database.query(SuperMemoTableHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, int repetitions, int interval, double easiness) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SuperMemoTableHelper.REPETITIONS, repetitions);
        contentValues.put(SuperMemoTableHelper.INTERVAL, interval);
        contentValues.put(SuperMemoTableHelper.EASINESS, easiness);
        return database.update(SuperMemoTableHelper.TABLE_NAME, contentValues, SuperMemoTableHelper._ID + " = " + _id, null);
    }

    public void delete(long _id) {
        database.delete(SuperMemoTableHelper.TABLE_NAME, SuperMemoTableHelper._ID + "=" + _id, null);
    }

    public void deleteTable() {
        database.delete(SuperMemoTableHelper.TABLE_NAME, null, null);
    }
}

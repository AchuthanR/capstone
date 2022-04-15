package com.capstone.autism_training.card;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DeckTableManager {

    private DeckTableHelper deckTableHelper;

    private final Context context;

    private SQLiteDatabase database;

    public DeckTableManager(Context c) {
        context = c;
    }

    public void open(String table_name) throws SQLException {
        deckTableHelper = new DeckTableHelper(context, table_name);
        database = deckTableHelper.getWritableDatabase();
        database.execSQL(deckTableHelper.CREATE_TABLE);
    }

    public void close() {
        if (deckTableHelper != null) {
            deckTableHelper.close();
        }
    }

    public long insert(byte[] image, String caption, String answer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DeckTableHelper.IMAGE, image);
        contentValues.put(DeckTableHelper.CAPTION, caption);
        contentValues.put(DeckTableHelper.ANSWER, answer);
        return database.insert(deckTableHelper.TABLE_NAME, null, contentValues);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DeckTableHelper.ID, DeckTableHelper.IMAGE, DeckTableHelper.CAPTION, DeckTableHelper.ANSWER };
        Cursor cursor = database.query(deckTableHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long id, byte[] image, String caption, String answer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DeckTableHelper.IMAGE, image);
        contentValues.put(DeckTableHelper.CAPTION, caption);
        contentValues.put(DeckTableHelper.ANSWER, answer);
        return database.update(deckTableHelper.TABLE_NAME, contentValues, DeckTableHelper.ID + " = " + id, null);
    }

    public void deleteRow(long id) {
        database.delete(deckTableHelper.TABLE_NAME, DeckTableHelper.ID + "=" + id, null);
    }

    public void deleteTable() {
        database.execSQL("DROP TABLE IF EXISTS " + deckTableHelper.TABLE_NAME);
    }
}

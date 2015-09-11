package mesas.martinez.leonor.iBoBlind.model;

/**
 * Created by leonormartinezmesas@gmail.com on 26/01/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PayloadDAO {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_DEVICE_ID,
            MySQLiteHelper.COLUMN_DATE,
            MySQLiteHelper.COLUMN_TYPE,
            MySQLiteHelper.COLUMN_VALUE
    };

    private Payload cursorTo(Cursor cursor) {
        Payload payload = new Payload();
        payload.set_id(cursor.getInt(0));
        payload.setdevice_id(cursor.getInt(1));
        payload.setMdate(cursor.getString(2));
        payload.setMtype(cursor.getString(3));
        payload.setMvalue(cursor.getString(4));
        return payload;
    }

    //    /**
//     * Every payload is from a device*/
//
//    private String[] allColumnsRelational = {
//            MySQLiteHelper.COLUMN_ID,
//            MySQLiteHelper.COLUMN_DATE,
//            MySQLiteHelper.COLUMN_ID_DEVICE,
//            MySQLiteHelper.COLUMN_ID_PAYLOAD
//    };
    public PayloadDAO(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
    /*
    If you create a payload, also will change the relational database DEVICE_PAYLOAD
     */
    public int create(Payload payload) {
        int insertId=-1;
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_DEVICE_ID, payload.getdevice_id());
        values.put(MySQLiteHelper.COLUMN_DATE, payload.getDate());
        values.put(MySQLiteHelper.COLUMN_TYPE, payload.getType());
        values.put(MySQLiteHelper.COLUMN_VALUE, payload.getValue());

        insertId = (int) database.insert(MySQLiteHelper.TABLE_PAYLOAD, null, values);
//        ContentValues valuesRelational = new ContentValues();
//        values.put(MySQLiteHelper.COLUMN_DATE, payload.getDate());
////        values.put(MySQLiteHelper.COLUMN_ID_PAYLOAD, insertId);
//        values.put(MySQLiteHelper.COLUMN_DEVICE_ID, payload.get_device_id());
        return insertId;
    }

    public void update(Payload payload) {
        String filter = MySQLiteHelper.COLUMN_ID + " = " + payload.get_id();
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_DEVICE_ID, payload.getdevice_id());
        values.put(MySQLiteHelper.COLUMN_DATE, payload.getDate());
        values.put(MySQLiteHelper.COLUMN_TYPE, payload.getType());
        values.put(MySQLiteHelper.COLUMN_VALUE, payload.getValue());
        database.update(MySQLiteHelper.TABLE_PAYLOAD, values, filter, null);
    }
/*
If you delete a payload, also will change the relational database DEVICE_PAYLOAD
 */
    public boolean delete(int id) {
       // boolean rt=(database.delete(MySQLiteHelper.TABLE_PAYLOAD, MySQLiteHelper.COLUMN_ID + " = " + id, null) > 0) && (database.delete(MySQLiteHelper.TABLE_DEVICE_PAYLOAD, MySQLiteHelper.COLUMN_ID_PAYLOAD + " = " + id, null) > 0);
        return database.delete(MySQLiteHelper.TABLE_PAYLOAD, MySQLiteHelper.COLUMN_ID + " = " + id, null) > 0;
    }

    public Payload getPayloadByID(int id) {
        Payload payload = new Payload();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PAYLOAD,
        allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            payload = cursorTo(cursor);
        }
        cursor.close();
        return payload;
    }

    public Payload getPayloadByDate(String date) {
        Payload payload = new Payload();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_PAYLOAD,
                allColumns, MySQLiteHelper.COLUMN_DATE + " = " + "\'" + date + "\'", null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            payload = cursorTo(cursor);
        }
        cursor.close();
        return payload;
    }
    public Payload getLastInsert(){
        Payload payload = new Payload();
        String selectQuery= "SELECT * FROM " + MySQLiteHelper.TABLE_PROJECTS+" ORDER BY "+MySQLiteHelper.COLUMN_ID+" DESC LIMIT 1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            payload = cursorTo(cursor);
        }
        cursor.close();
        return payload;
    }
    public Payload getLastDate(){
        Payload payload = new Payload();
        String selectQuery= "SELECT * FROM " + MySQLiteHelper.TABLE_PROJECTS+" ORDER BY "+MySQLiteHelper.COLUMN_DATE+" DESC LIMIT 1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            payload = cursorTo(cursor);
        }
        cursor.close();
        return payload;
    }
    public List<Payload> getAll() {
        List<Payload> payloadList = new ArrayList<Payload>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PAYLOAD,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Payload payload = cursorTo(cursor);
            payloadList.add(payload);
            cursor.moveToNext();
        }
        cursor.close();
        return payloadList;
    }

    public List<Payload> getAllByDeviceId(int device_id) {
        List<Payload> payloadList = new ArrayList<Payload>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PAYLOAD,
                allColumns, MySQLiteHelper.COLUMN_DEVICE_ID+ " = " + device_id, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Payload payload = cursorTo(cursor);
            payloadList.add(payload);
            cursor.moveToNext();
        }
        cursor.close();
        return payloadList;
    }

}

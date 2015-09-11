package mesas.martinez.leonor.iBoBlind.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leonor Martinez Mesas on 26/01/15.
 */
public class DeviceDAO {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_PROJECT_ID,
            MySQLiteHelper.COLUMN_DATE,
            MySQLiteHelper.COLUMN_DEVICE_ADDRESS,
            MySQLiteHelper.COLUMN_DEVICE_NAME,
            MySQLiteHelper.COLUMN_DEVICE_ESPECIFICATION,
            MySQLiteHelper.COLUMN_MAX_RSSI,
            MySQLiteHelper.COLUMN_LATITUDE,
            MySQLiteHelper.COLUMN_LONGITUDE
    };

    private Device cursorTo(Cursor cursor) {
       // Log.i("DeviceDAO","cursorTo id:"+cursor.getCount());
        Device device = new Device();
        if(cursor.getCount()>1) {
            //Log.i("DeviceDAO","setid :"+cursor.getInt(0));
            device.set_id(cursor.getInt(0));
            device.setprojecto_id(cursor.getInt(1));
            device.setDate(cursor.getString(2));
            device.setmDeviceAddress(cursor.getString(3));
            device.setmDeviceName(cursor.getString(4));
            device.setDeviceSpecification(cursor.getString(5));
            device.setMaxRSSI(cursor.getString(6));
            device.setLatitude(cursor.getString(7));
            device.setLongitude(cursor.getString(8));
        }
        return device;
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

    public DeviceDAO(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public int create(Device device) {
        int insertId=-1;
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PROJECT_ID, device.getprojecto_id());
        values.put(MySQLiteHelper.COLUMN_DATE, device.getDate());
        values.put(MySQLiteHelper.COLUMN_DEVICE_ADDRESS, device.getmDeviceAddress());
        values.put(MySQLiteHelper.COLUMN_DEVICE_NAME, device.getmDeviceName());
        values.put(MySQLiteHelper.COLUMN_DEVICE_ESPECIFICATION, device.getDeviceSpecification());
        values.put(MySQLiteHelper.COLUMN_MAX_RSSI, device.getMaxRSSI());
        values.put(MySQLiteHelper.COLUMN_LATITUDE, device.getLatitude());
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, device.getLongitude());
        insertId = (int) database.insert(MySQLiteHelper.TABLE_DEVICES, null, values);
        return insertId;
    }

    public void update(Device device) {
        String filter = MySQLiteHelper.COLUMN_ID + " = " + device.get_id();
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PROJECT_ID, device.getprojecto_id());
        values.put(MySQLiteHelper.COLUMN_DATE, device.getDate());
        values.put(MySQLiteHelper.COLUMN_DEVICE_ADDRESS, device.getmDeviceAddress());
        values.put(MySQLiteHelper.COLUMN_DEVICE_NAME, device.getmDeviceName());
        values.put(MySQLiteHelper.COLUMN_DEVICE_ESPECIFICATION, device.getDeviceSpecification());
        values.put(MySQLiteHelper.COLUMN_MAX_RSSI, device.getMaxRSSI());
        values.put(MySQLiteHelper.COLUMN_LATITUDE, device.getLatitude());
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, device.getLongitude());
        database.update(MySQLiteHelper.TABLE_DEVICES, values, filter, null);
    }

    public boolean delete(int id) {
        return database.delete(MySQLiteHelper.TABLE_DEVICES, MySQLiteHelper.COLUMN_ID + " = " + id, null) > 0;
    }


    public Device getDeviceByID(int id) {
        Device device = new Device();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_DEVICES,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            device = cursorTo(cursor);
        }else{

            Log.i("DeviceDAO", "getDeviceByID, cursor==null");
        }
        cursor.close();
        return device;
    }

    public Device getDeviceByDate(String date) {
        Device device = new Device();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_DEVICES,
                allColumns, MySQLiteHelper.COLUMN_DATE + " = " + "\'" + date + "\'", null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            device = cursorTo(cursor);
        }
        cursor.close();
        return device;
    }

    public Device getDeviceByAddress(String address) {
        Device device = new Device();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_DEVICES,
                allColumns, MySQLiteHelper.COLUMN_DEVICE_ADDRESS + " = " + "\'" + address + "\'", null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            device = cursorTo(cursor);
        }
        cursor.close();
        return device;
    }
    public Device getDeviceByAddressAndProject(String address, int project_id) {
        Device device = new Device();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_DEVICES,
                allColumns, MySQLiteHelper.COLUMN_DEVICE_ADDRESS + " = " + "\'" + address + "\'" + " and " + MySQLiteHelper.COLUMN_PROJECT_ID+ " = " + project_id, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            device = cursorTo(cursor);
        }
        cursor.close();
        return device;
    }
    public List<Device> getAllbyprojectID(int project_id) {
        List<Device> deviceList = new ArrayList<Device>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_DEVICES,
                allColumns,  MySQLiteHelper.COLUMN_PROJECT_ID + " = " + project_id, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Device device = cursorTo(cursor);
            deviceList.add(device);
            cursor.moveToNext();
        }

        cursor.close();
        return deviceList;
    }
    public Device getLastInsert(){
        Device device = new Device();
        String selectQuery= "SELECT * FROM " + MySQLiteHelper.TABLE_PROJECTS+" ORDER BY "+MySQLiteHelper.COLUMN_ID+" DESC LIMIT 1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            device = cursorTo(cursor);
        }
        cursor.close();
        return device;
    }
    public Device getLastDate(){
        Device device = new Device();
        String selectQuery= "SELECT * FROM " + MySQLiteHelper.TABLE_PROJECTS+" ORDER BY "+MySQLiteHelper.COLUMN_DATE+" DESC LIMIT 1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            device = cursorTo(cursor);
        }
        cursor.close();
        return device;
    }

    public List<Device> getAll() {
        List<Device> deviceList = new ArrayList<Device>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_DEVICES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Device device = cursorTo(cursor);
            deviceList.add(device);
            cursor.moveToNext();
        }

        cursor.close();
        return deviceList;
    }

}

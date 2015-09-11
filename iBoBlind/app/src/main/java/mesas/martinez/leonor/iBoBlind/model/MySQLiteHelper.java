package mesas.martinez.leonor.iBoBlind.model;
/**
 * Created by leonormartinezmesas@gmail.com on 26/01/15.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "presencecontrol.db";
    private static final int DATABASE_VERSION = 5;
    public static final String TABLE_PAYLOAD = "payload";
    public static final String TABLE_DEVICES = "devices";
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_DEVICE_PAYLOAD = "devices_payload";
    public static final String TABLE_PROJECTS_DEVICE = "projects_devices";

    /*
    Common columns
     */
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE= "mdate";

    /*
    Table PAYLOAD
     */
//    mtype = null;//type of value, example Integer, double, String, Long ..ect
//    mvalue = null;//The value collected by the sensor
    public static final String COLUMN_DEVICE_ID = "device_id";
    public static final String COLUMN_TYPE= "mtype";
    public static final String COLUMN_VALUE = "mvalue";
    /*
    Table DEVICES
     */
//    address
//    mDeviceName
//    latitude
//    longitude
//    mDeviceSpecification
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_DEVICE_ADDRESS= "address";
    public static final String COLUMN_DEVICE_NAME = "device_name";
    public static final String COLUMN_DEVICE_ESPECIFICATION = "device_specification";
    public static final String COLUMN_MAX_RSSI= "max_rssi";
    public static final String COLUMN_LATITUDE= "latitude";
    public static final String COLUMN_LONGITUDE= "longitude";

    /*
    Table PROJECTS
     */
//    mProjectName
//    mProjectSpecification
    public static final String COLUMN_PROJECT_NAME= "project_name";
    public static final String COLUMN_PROJECT_SPECIFICATION = "project_specification";

    /*
    SQL queries to create tables.
     */
    private static final String DATABASE_CREATE_PAYLOAD = "create table if not exists "
            + TABLE_PAYLOAD + " ( "
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DEVICE_ID + " integer not null, "
            + COLUMN_DATE+ " text not null, "
            + COLUMN_TYPE+ " text default 'rssi', "
            + COLUMN_VALUE + " text); "
            ;

    private static final String DATABASE_CREATE_DEVICES = "create table if not exists "
            + TABLE_DEVICES + " ( "
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_PROJECT_ID + " integer not null, "
            + COLUMN_DATE+ " text, "
            + COLUMN_DEVICE_ADDRESS+ " text not null, "
            + COLUMN_DEVICE_NAME + " text, "
            + COLUMN_DEVICE_ESPECIFICATION + " text, "
            + COLUMN_MAX_RSSI+ " text, "
            + COLUMN_LATITUDE+ " text, "
            + COLUMN_LONGITUDE+ " text, "
            + " CONSTRAINT uc_PersonID UNIQUE ("+COLUMN_PROJECT_ID+","+COLUMN_DEVICE_ADDRESS+") );"
            ;

//    private static final String DATABASE_CREATE_PROJECTS = "create table if not exists "
//            + TABLE_PROJECTS + " ( "
//            + COLUMN_ID + " integer primary key autoincrement, "
//            + COLUMN_DATE+ " text not null, "
//            + COLUMN_PROJECT_NAME+ " text not null, "
//            + COLUMN_PROJECT_SPECIFICATION + " text); "
            ;
//Unique give problem
    private static final String DATABASE_CREATE_PROJECTS = "create table if not exists "
            + TABLE_PROJECTS + " ( "
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DATE+ " text not null, "
            + COLUMN_PROJECT_NAME+ " text not null unique, "
            + COLUMN_PROJECT_SPECIFICATION + " text); "
            ;
//    /*
//    * relational tables
//    */
//    private static final String DATABASE_CREATE_DEVICE_PAYLOAD = "create table if not exists "
//            + TABLE_DEVICE_PAYLOAD + " ( "
//            + COLUMN_ID + " integer primary key autoincrement, "
//            + COLUMN_DATE+ " text not null, "
//            + COLUMN_DEVICE_ID + " integer default 0, "
//            + COLUMN_ID_PAYLOAD + " integer default 0); "
//            ;
//
//    private static final String DATABASE_CREATE_PROJECTS_DEVICE= "create table if not exists "
//            + TABLE_PROJECTS_DEVICE + " ( "
//            + COLUMN_ID + " integer primary key autoincrement, "
//            + COLUMN_DATE+ " text not null, "
//            + COLUMN_PROJECT_ID + " integer not null, "
//            + COLUMN_DEVICE_ID + " integer not null); "
//            ;

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE_DEVICES);
        sqLiteDatabase.execSQL(DATABASE_CREATE_PAYLOAD);
        sqLiteDatabase.execSQL(DATABASE_CREATE_PROJECTS);
//        sqLiteDatabase.execSQL(DATABASE_CREATE_PROJECTS_DEVICE);
//        sqLiteDatabase.execSQL(DATABASE_CREATE_DEVICE_PAYLOAD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYLOAD);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE_PAYLOAD);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS_DEVICE);
        onCreate(sqLiteDatabase);
    }

}

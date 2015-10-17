package mesas.martinez.leonor.iBoBlind.Services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;

import android.os.Looper;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.RecognizerIntent;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


import mesas.martinez.leonor.iBoBlind.R;
import mesas.martinez.leonor.iBoBlind.comunication.HTTP_JSON_POST;
import mesas.martinez.leonor.iBoBlind.model.Constants;
import mesas.martinez.leonor.iBoBlind.model.Device;
import mesas.martinez.leonor.iBoBlind.model.DeviceDAO;
import mesas.martinez.leonor.iBoBlind.model.OrionJsonManager;
import mesas.martinez.leonor.iBoBlind.model.Deviceaux;

/**
 * Created by root on 5/08/15.
 */
public class SpeechBluService extends IntentService implements BluetoothAdapter.LeScanCallback, TextToSpeech.OnInitListener {

    //------------------Variables---------------------//
    private static boolean start;
    private SharedPreferences sharePreference;
    private OrionJsonManager jsonManager;
    //--to-speak-Variables/Contans,enums---//
    private String toSpeak;
    private TextToSpeech tts = null;
    private boolean ttsInit;
    //-------------for text movement----------//
    //private Rotation rotate;
    private Accelerometer accelerometer;
    protected static int MIN_TIME_SENSIVILITI = 100000000;
    protected int time_sensitivity;
    protected long time_difference = 2;
    protected float min_movement = 12;
    private ArrayList<Deviceaux> mDevicesArray;
    private ArrayList<Device> blackListArray;

    //----------------To now if we are near or far ago---to the device----//
    private int measuresFORaverage;
    private int diferAverage;

    //----Bluetooth-Variables/Contans,enums--//
    public static enum State {
        UNKNOWN,
        WAIT,
        WAIT_RESPONSE,
        SCANNING,
        BLUETOOTH_OFF,
        CONNECTING,
        DISCONNECTING
    }

    private static final long SCAN_TIMEOUT = 800;
    private static final long WAIT_PERIOD = 2000;

    private String address;
    private String device_name;
    private String string_rssi;
    private String old_address;
    private String old_string_rssi;
    private BluetoothAdapter mBluetoothAdapter = null;
    //--for target 21

    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filter;
    private mScanCallback myScanCallback;
    //--fin terget21--//
    private State mState;
    private Handler mHandler;
    private HTTP_JSON_POST jsonPost;
    private List<ScanFilter> filters;

    //-----DataBase-Variables--//
    private DeviceDAO deviceDAO;
    private Device deviceaux;

    public SpeechBluService() {
        super(SpeechBluService.class.getName());
        // this.setState(State.UNKNOWN);
        mHandler = new Handler();
        mState = State.CONNECTING;
        ttsInit = false;
        start = true;
        tts = null;
    }

    //-----------------------------------------------Main-Method---------------------------//
    @Override
    protected void onHandleIntent(Intent intent) {
        toSpeak = " ";
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        //Comprobar si estan activos los elementos necesarios

        tts = new TextToSpeech(getBaseContext(), this);

        //tts = new TextToSpeech(this,this);
        //tts.setSpeechRate(0.5f);
        //tts.setPitch(1.5f);
        //to can reproduce the messages

        mDevicesArray = new ArrayList<Deviceaux>();
        blackListArray= new ArrayList<Device>();
        sharePreference = PreferenceManager.getDefaultSharedPreferences(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.DEVICE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.BLACKDEVICE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.DEVICE_MESSAGE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_STOP));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_UNKNOWN_STATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_WAIT_RESPONSE));

//Value for a slow movement
        accelerometer = new Accelerometer();
        setMovementAtributes();
        //rotate=new Rotation();

        //Log.d(Constants.TAG,"------------AFTERONinit----------: /\n--int_movement----"+min_movement+"---diferAverage---"+diferAverage+"----measuresDORaverage---"+measuresFORaverage+"--------time sensitivity-------"+time_sensitivity);

        accelerometer.start();
        //rotate.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myScanCallback = new mScanCallback();
        }

        try {
            this.startScan();
        } catch (InterruptedException e) {
            Log.d("InterrupteException in While", "------------STOP----------");
            start = false;
        }

        //Start detect i-beacons
        while (start) {
            try {
                synchronized (this) {
                    //Log.d("---onHandleIntent WHILE---", "Start Scan");
                    this.startScan();
                    this.wait(WAIT_PERIOD);
                }
            } catch (InterruptedException e) {
                Log.d("InterruptedException in While", "------------STOP----------");
                start = false;
            }
        }
    }

    //----------------------------------------------------Methods---------------------------------//
    private boolean isFastMovement(){
        boolean result=true;
        if (accelerometer.getmMovement() < 2.5f) {result=false;}
        return result;
    }
    private void setMovementAtributes() {
        try {
            if (!isFastMovement()) {
                //Value for a slow movement
                min_movement = sharePreference.getInt(Constants.MOVEMENT, 13);
                time_sensitivity = sharePreference.getInt(Constants.MOVEMENT, 30);
            } else {
                //Value for a fast movement
                min_movement = sharePreference.getInt(Constants.MOVEMENT, 19);
                time_sensitivity = sharePreference.getInt(Constants.MOVEMENT, 15);
            }
            diferAverage = sharePreference.getInt(Constants.DIFER, 3);
            min_movement = (float) min_movement / 10;
            accelerometer.setMin_movement(min_movement);
            time_sensitivity = time_sensitivity * MIN_TIME_SENSIVILITI;
            accelerometer.setTime_sensitivity(time_sensitivity);
        } catch (NullPointerException e) {
            Log.e("OnDeviceDetected nullPointException", e.getLocalizedMessage() + e.getMessage());
            e.printStackTrace();
        }
    }

    //------------------to-Speak--Methods---------------------------//
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locSpainh = new Locale("spa", "ESP");
            int result = tts.setLanguage(locSpainh);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Ready to Speak", Toast.LENGTH_SHORT).show();
                ttsInit = true;
            }
        } else {
            Toast.makeText(this, "Can Not Speak", Toast.LENGTH_LONG).show();
        }

    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {

        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        try{
        tts.speak(text, TextToSpeech.QUEUE_ADD, map);
    }catch(NullPointerException e){
        Log.e("SPEAKTheText", "can connect with tts system, retray");
        tts.stop();
        tts.shutdown();
        tts = null;
        tts = new TextToSpeech(getBaseContext(), this);
        tts.speak(text, TextToSpeech.QUEUE_ADD, map);
    }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsUnder21(String text) {

        String utteranceId = this.hashCode() + "";
        try{
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
    }catch(NullPointerException e){
        Log.e("SPEAKTheText", "can connect with tts system, retray");
        tts.stop();
        tts.shutdown();
        tts = null;
        tts = new TextToSpeech(getBaseContext(), this);
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
    }
    }

    protected void speakTheText() {

        Log.v("--SPEAKtheTEXT---", toSpeak);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsUnder21(toSpeak);
        } else {
            ttsUnder20(toSpeak);
        }


    }

    //-------------------------------------END SPEAK METODS----------------------------------------------//
    public void setState(State state) {
        mState = state;
        //Log.d("--Service--Set State--",mState.name());
        String share = Constants.SERVICE_STATE.toString();
        SharedPreferences config = this.getSharedPreferences(share, MODE_MULTI_PROCESS);
        config.edit()
                .putString(Constants.SERVICE_STATE, mState.name())
                .commit();

    }

    protected void mstop() {
        Log.d("---STOP SERVICE--", " " + start);

        if (start != false) {
            start = false;
            mDevicesArray.clear();
            blackListArray.clear();
            mBluetoothAdapter.stopLeScan(SpeechBluService.this);
            this.setState(State.DISCONNECTING);
            // Stop the TTS Engine when you do not require it
            if (!tts.equals(null)) {
                tts.stop();
                tts.shutdown();
            }
            //Stop accelerometer
            accelerometer.stop();
            //rotate.stop();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            this.stopSelf();//Stop service
            this.onDestroy();
        }

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Constants.BLACKDEVICE:
                    String blackadress=intent.getStringExtra("address");
                    Device blackdevice=new Device(blackadress);
                    blackListArray.add(blackdevice);
                    break;
                case Constants.DEVICE:
                    int id = intent.getIntExtra("id", -1);
                    //Log.i("OnL--INTENT DEVICE received-----------id--",String.valueOf(id));
                    if (id != -1) {
                        int mrssi = intent.getIntExtra("rssi", 0);
                        int mcoberageAlert = intent.getIntExtra("coverageAlert", 0);
                        toSpeak = intent.getStringExtra("message");
                        String maddress = intent.getStringExtra("address");
                        //Log.i("OnL--INTENT DEVICE received-------------",maddress+", mesage: "+toSpeak+", rssi "+String.valueOf(mrssi)+", coberageAlert"+String.valueOf(mrssi));

                        if (!toSpeak.equals(null)) {
                            SpeechBluService.this.speakTheText();
                            //speakTheText(toSpeak);
                            Deviceaux mdeviceaux = new Deviceaux(maddress, mcoberageAlert, toSpeak, mrssi);
                            int index = mDevicesArray.indexOf(mdeviceaux);
                            //Log.d("OnL--INTEN DEVICE--","------ index: "+ String.valueOf(index));
                            if (index < 0) {
                                mDevicesArray.add(mdeviceaux);
                                index = mDevicesArray.indexOf(mdeviceaux);
                                //Log.d("OnL--INTEN DEVICE--","---device added--- index: "+ String.valueOf(index));
                            } else {
                                mDevicesArray.set(index, mdeviceaux);
                                //Log.d("OnL--INTEN DEVICE--","---device update--- index: "+ String.valueOf(index));
                            }
                        }
                    }
                    SpeechBluService.this.setState(State.UNKNOWN);
                    break;
                case Constants.DEVICE_MESSAGE:
                    toSpeak = intent.getStringExtra("message");
                    // Log.i("-----------INTENT received-------------","---DEVICE_MESSAGE--"+toSpeak);
                    if (!toSpeak.equals(null))
                        SpeechBluService.this.speakTheText();
                    //speakTheText(toSpeak);
                    break;
                case Constants.SERVICE_STOP:
                    SpeechBluService.this.mstop();
                    Log.i("-----------INTENT received-------------", "---STOP SERVICE--");
                    break;
                case Constants.SERVICE_WAIT_RESPONSE:
                    SpeechBluService.this.setState(State.WAIT_RESPONSE);
                    //Log.i("-----------INTENT received-------------","--SERVICE_WAIT_RESPONSE--");
                    break;
                case Constants.SERVICE_UNKNOWN_STATE:
                    SpeechBluService.this.setState(State.UNKNOWN);
                    // Log.i("-----------INTENT received-------------","---SERVICE_UNKNOWN_STATE--");
                    break;
                default:
                    //Log.i("-----------INTENT received-------------", action+"---not catched--");
                    break;
            }
        }
    };


    @Override
    public void onDestroy() {
        Log.d("--OnDetroy---", "SpeechBluService");
        this.mstop();
        super.onDestroy();
    }

    //------------------------BLU--Methods---------------------------//

    @SuppressWarnings("deprecation")
    private void stopLeScan() {
        mBluetoothAdapter.stopLeScan(SpeechBluService.this);
    }

    @SuppressWarnings("deprecation")
    private void startLeScan() {
        if(!mBluetoothAdapter.isDiscovering()){
        mBluetoothAdapter.startLeScan(SpeechBluService.this);
        }
    }

    private void start() {
        try {
            // Log.d("startScan:", "------------------Star-----------------------\n\n\n ");
// scan for SCAN_TIMEOUT
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Log.d("Start ","--------------stopLeScan-------------");
                    //SpeechBluService.this.setState(State.WAIT);
                    if (Build.VERSION.SDK_INT < 21) {
                        stopLeScan();
                    } else {
                        mLEScanner.stopScan(myScanCallback);
                    }
                }
            }, SCAN_TIMEOUT);

            //Wait for HTTP_JSON_POST end, before scan again
            // if((!mState.equals(State.SCANNING)) && (!mState.equals(State.WAIT_RESPONSE)) ){
//                        Log.i("---onHandleIntent WHILE--", "WAIT FOR STATE CHANGE");
            SpeechBluService.this.setState(State.SCANNING);
            // Log.d("Start ","--------------startLeScan-------------");
            if (Build.VERSION.SDK_INT < 21) {
                startLeScan();
            } else {
                mLEScanner.startScan(filters, settings, myScanCallback);
            }

            //}
        } catch (NullPointerException e) {
            Log.e("OnDeviceDetected nullPointException", e.getLocalizedMessage() + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startScan() throws InterruptedException {
        if (mBluetoothAdapter == null) {
            Log.d("Start Scan", "mBluetoothAdapter==null");
            final BluetoothManager BluetoothManager = (android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = BluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.d("Start Scan", "mBluetoothAdapter is not Enable");
            Log.i("starScan", "BLUETOOH is OFF");
            this.setState(State.BLUETOOTH_OFF);
            String turn_on = getResources().getString(R.string.turn_on_Bluetooth);
            Toast.makeText(this, turn_on, Toast.LENGTH_LONG).show();
            this.wait(Constants.WAIT_TIME);
            //please trun on Bluetthoth sensor
            Intent intent = new Intent(Constants.BLUETOOTH_OFF);
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
            start = false;
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                filters = new ArrayList<ScanFilter>();
            }
            SpeechBluService.this.start();
        }
    }

    //--------------------------------------------------------------------When a i-becaon is detected---------------------------------------//
    public void OnDeviceDetected(final BluetoothDevice device, int rssi) {
        address = device.getAddress().toString();
        if(!isBlackDevice(address)){
        String Oldmessage = " ";
        string_rssi = String.valueOf(rssi);
        device_name = device.getName();
        Log.d("OnDeviceDetected " + String.valueOf(System.currentTimeMillis()), "" + address + " name:" + device_name + " rssi:" + string_rssi);
        Deviceaux auxdevice = new Deviceaux(rssi, address);
        //det data and old message
        boolean update = true;
        int index = -1;
        try {
            deviceDAO = new DeviceDAO(this);
            deviceDAO.open();
            Device d = deviceDAO.getDeviceByAddress(this.address);
            deviceDAO.close();
            int device_id = d.get_id();
            Log.d("OnDeviceDetected ", "" + address + " ID " + device_id);
            if (device_id != -1) {
                String device_date = d.getDate();
                long date = Long.valueOf(device_date);
                long current_data = System.currentTimeMillis();
                long difer = current_data - date;

                long aweek = 604800000;
                long aday = 86400000;
                Log.d("OnDeviceDetected ", "Device Found in database " + address + " data difer= " + difer + "< " + aday + " " + (difer < aday));
                if (difer < aday) {
                    update = false;
                    Oldmessage = d.getDeviceSpecification();
                    String coverage = d.getMaxRSSI();
                    int OldcoberageAlert = (int) Integer.valueOf(coverage);
                    auxdevice.setText(Oldmessage);
                    auxdevice.setOutOfRegion(OldcoberageAlert);
                }
            }
            //-----------end get data---------//

            index = mDevicesArray.indexOf(auxdevice);
        } catch (NullPointerException e) {
            Log.e("OnDeviceDetected nullPointException", e.getLocalizedMessage() + e.getMessage());
            e.printStackTrace();
        }
        if (index==-1) {
            // The first time the user is in the region we say the  RegionEnter message ; and it save state.
            String message = "----New Device--- address: " + address + " rssi " + string_rssi + " ?>" + auxdevice.getOutOfRegion()+" ¿need Update?-->"+update;
            // writeFile(message);
            Log.d("OnLeScan", message);

            if (update) {
                jsonManager = new OrionJsonManager();
                //String jsonString=jsonManager.SetJSONtoGetMessage("BLE", address);
                String jsonString = jsonManager.SetJSONtoGetAttributes("BLE", address, getApplicationContext());
                if (device_name != null) {
                    jsonManager.setDeviceName(device_name);
                }
                new HTTP_JSON_POST(this, jsonManager, address, rssi).execute();
            } else  if (rssi >= auxdevice.getOutOfRegion()) {
                    toSpeak = Oldmessage;
                    speakTheText();
                    mDevicesArray.add(auxdevice);
            }
        } else {
           // String message = "----Detected device again----" + address + " rssi " + string_rssi + "Time" + String.valueOf(System.currentTimeMillis());
            String message = "----Detected device again----" + address + " rssi " + string_rssi+", Index= "+index;
            //writeFile(message);
            Log.d("OnLeScan", message);
            speakUpdateDeviceauxAgain(index, rssi);

        }}//if black device
    }
private boolean isBlackDevice(String Adrees){
    boolean result=false;
    for(int x=0; x<blackListArray.size();x++){
        Device blackauxdevice=blackListArray.get(x);
        if(blackauxdevice.getmDeviceAddress().equals(Adrees)){
            String device_date = blackauxdevice.getDate();
            long date = Long.valueOf(device_date);
            long current_data = System.currentTimeMillis();
            long difer = current_data - date;
            long aweek = 604800000;
            long aday = 86400000;
            if (difer < aday) {result=true;}else{
              blackListArray.remove(x);
            }
        }

    }

    return result;
}
    //Method from LeScanCallBack
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

        OnDeviceDetected(device, rssi);
    }

    //---------------------For api 21---------------------//
    @TargetApi(21)
    private class mScanCallback extends ScanCallback {
        public mScanCallback() {
            super();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();
            int rssi = result.getRssi();
            OnDeviceDetected(btDevice, rssi);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult-Results", sr.toString());
            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code:" + errorCode);

        }
    }


    //-----------------FIn api 21------------------//
    //---------------------------------------------------------------Fin-----When a i-becaon is detected---------------------------------------//
    //---------------Device again--------------------//
    private void speakUpdateDeviceauxAgain(int index, int rssi) {
        if(mDevicesArray.get(index).RangeOfTime()){
        setMovementAtributes();
        String address = mDevicesArray.get(index).getAddress();
        String speak = mDevicesArray.get(index).getText();
        double limitRegion = mDevicesArray.get(index).getOutOfRegion();
        double lastRSSI =mDevicesArray.get(index).getdBmRSSI();
        mDevicesArray.get(index).setdBmRSSI(rssi);
        double difer =mDevicesArray.get(index).difer();
        if(difer==0.0f){
            difer=rssi-lastRSSI;
            diferAverage =diferAverage*2;
        }
        int count;
        boolean r = accelerometer.RangeOfTime();//wait 3
        double average=mDevicesArray.get(index).average();
        Log.d("SPEECHBLUSERVICE", "----" + address + " ---AVERAGE= "+average+" -------RSSI-----=" + rssi+ ", LastRSSI"+lastRSSI + " difer= " + difer + " diferAverage=" + diferAverage + "accelerometer.RangeOfTimer=" + r);

// While the user is moving in the region , we calculate a mean of 3 updates before Indicate the movement.
// when the user go out of the region. we say RegionOut message and it save state .
        if (r) {
               if ( average > -56 && average !=0) {
                   if(mDevicesArray.get(index).getCount()<2){
                   String Text = getResources().getString(R.string.close_up);
                   toSpeak = Text + speak;
                   mDevicesArray.get(index).plusOneCount();}
               } else if (difer < -diferAverage) {
                    //move away
                    //Out off range?
                    //int b = new Double(dBmRSSI).compareTo(new Double(limitRegion - ajustOutOffRange));
                    int b = new Double(rssi).compareTo(new Double(limitRegion));
                    Log.d("SPEECHBLUSERVICE SpeakupdatedeviceAUX", "dBmAverrage compareTo limitRegion = " + b);
                    if (b == -1) {
                        mDevicesArray.get(index).setdBmRSSI(rssi);
                        String Text = getResources().getString(R.string.Out_Of);
                        toSpeak = Text + speak;
                        mDevicesArray.remove(index);
                        mDevicesArray.get(index).setCounttocero();
                    } else {
                        String Text = getResources().getString(R.string.move_away_to);
                        toSpeak = Text + speak;
                        mDevicesArray.get(index).minusOneCount();
                    }
                } else {
                    Log.d("SPEECHBLUSERVICE SpeakupdatedeviceAUX", "----" + address + "-RangeOfTime----TRUE---difer---=" + difer + "(difer < -diferAverage)= " + (difer < -diferAverage));
                    //approach
                    if (difer > diferAverage) {
                        String Text = getResources().getString(R.string.approach_to);
                        toSpeak = Text + speak;
                        mDevicesArray.get(index).minusOneCount();
                    }
                }
               Log.d("SPEECHBLUSERVICE SpeakupdatedeviceAUX",  " " + address+"--To Speak " + toSpeak );
               SpeechBluService.this.speakTheText();
            }

    }//if Rage Of time OK
    }

    //---------------To now is there is there are movement --------------//

    class Rotation implements SensorEventListener {
        private long current_time;
        float[] orientation = new float[3];
        float[] rMat = new float[9];
        private SensorManager smr;
        private List<Sensor> sensors;
        private int mAzimuth = 0;
        private boolean mdo = false;

        Rotation() {
            smr = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensors = smr.getSensorList(Sensor.TYPE_ROTATION_VECTOR);

        }

        public void start() {
            Log.d("--Rotation start--", "CALL");
            if (sensors.size() > 0) {
                //5 muestras por segun-do

                mdo = smr.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
                Log.d("Rotation registerListener---", String.valueOf(mdo));
            }
        }

        public void stop() {
            if (mdo) {
                try {
                    smr.unregisterListener(this, sensors.get(0));
                } catch (IndexOutOfBoundsException e) {
                    Log.e("Rotation Index OutOfBoundException", e.getStackTrace().toString());
                }
            }
            ;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                current_time = event.timestamp;
                smr.getRotationMatrixFromVector(rMat, event.values);
                mAzimuth = (int) (Math.toDegrees(smr.getOrientation(rMat, orientation)[0]) + 360) % 360;
                String message = "*Rotation---Time:" + current_time + " msg, mAzimuth= " + mAzimuth + "--------------";
                Log.d("OnSensorChange", message);
                //writeFile(message);
            }
        }
    }

    class Accelerometer implements SensorEventListener {

        private long last_update = 0, last_movement = 0;
        private float prevX = 0, prevY = 0, prevZ = 0;
        private float curX = 0, curY = 0, curZ = 0;
        private float movement;
        private float oldmovement;
        private int time_sensitivity;
        private float min_movement = 11;
        private float mMovement = 0;
        //float base_movement = 1E-6f;
        private long time_difference;
        private long current_time;
        private SensorManager sm;
        private List<Sensor> sensors;
        private float[] movements = {0f, 0f, 0f, 0f, 0f, 0f};
        //        int[]tendence={0,0,0,0,0,0,0,0};
        float[] tendenceX = {0f, 0f, 0f, 0f, 0f, 0f};
        float[] tendenceY = {0f, 0f, 0f, 0f, 0f, 0f};
        float[] tendenceZ = {0f, 0f, 0f, 0f, 0f, 0f};
        float X = 0;
        float Y = 0;
        float Z = 0;
        //        float[] speedNew=new float[3];
//        float[] speed=new float[3];
//        float Td;
//        float mspeed;
        boolean mdo = false;
        //        boolean rotate=false;
//        private Sensor msensor;
        int count = 0;
        int a = 0;
        public int getTime_sensitivity() {
            return time_sensitivity;
        }

        public void setTime_sensitivity(int time_sensitivity) {
            this.time_sensitivity = time_sensitivity;
        }

        public float getmMovement() {
            return mMovement;
        }

        public float getMin_movement() {
            return min_movement;
        }

        public void setMin_movement(float min_movement) {
            this.min_movement = min_movement;
        }

        public long getTime_difference() {
            return time_difference;
        }

        public long getCurrent_time() {
            return current_time;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
            return super.clone();
        }

        Accelerometer() {
            sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            //sensors = sm.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
            //msensor=sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
            //Log.d("Linear_Acceleration",msensor.toString());
            sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
            for (int i = 0; i < sensors.size(); i++) {
                Sensor e = sensors.get(i);
                Log.d("SENSOR LIST IN POSITION " + i + " = ", e.getName() + " Maxrange" + e.getMaximumRange() + ", Resolution" + e.getResolution());
            }

        }

        public void start() {
            //Log.d("--aCCELEROMETER start--","CALL");
            if (sensors.size() > 0) {
                //5 muestras por segundo
                mdo = sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
                //Log.d("--Accelerometer registerListener: ",String.valueOf(mdo));
            }
        }

        public boolean RangeOfTime() {
            boolean response = false;
            //*5.0 because discober devices is slow that accelerometer
            if (time_difference > (time_sensitivity)) {
                if(mMovement>min_movement){
                    response = true;
                    last_movement=current_time;
                }   }
            //Log.d(Constants.TAG, "-----RANGE OF TIMER------" + response + "--" + time_difference + " < " + (time_sensitivity));
            return response;
        }

        public void stop() {
            try {
                if (mdo) {
                    sm.unregisterListener(this, sensors.get(0));
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e("accelerometer Index outOfBoundException", e.getStackTrace().toString());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        //        //calculate speend in m/s
//        public void calculateSpeed(float[] acceleration, float deltaTime){
//            for(int i=0; i<acceleration.length; i++){
//                speedNew[i]=acceleration[i]*deltaTime;
//            }
//            speed[0]=(speed[0]+speedNew[0])/2;
//            speed[1]=(speed[1]+speedNew[1])/2;
//            speed[2]=(speed[2]+speedNew[2])/2;
//            mspeed=(float)Math.sqrt((double)(speed[0]*speed[0]+speed[1]*speed[1]+speed[2]*speed[2]));
//        }
        private void push(float value, float[] r) {
            try{
            if(a>0){
            for (int i = (r.length - 1); i > 0; i--) {
                float aux = r[i - 1];
                r[i] = aux;
            }}
            a++;
            r[0] = value;

        }catch(NullPointerException e){
            Log.e("ACCELEROMETER PUSH", "NULL POINT EXCEPTION"+e.getLocalizedMessage());
            e.printStackTrace();
        }
        }

        private float average(float[] r) {
            float d = 0;
            float sum = 0;
            float result=0;
            for (int i = 0; i< r.length; i++) {
                sum = sum + r[i];
                if(r[i]!=0.0f){
                d++;}
            }
            if(d!=0.0f){
            result = sum/d;}
            //Log.d("Average", "SUM= "+sum+"/ por d= "+d+" result= "+result);
            return result;
        }

        //        private void updateAxis(){
//            if(curX>prevX){
//                //acceleration
//                push(1, tendenceX);
//            }else if(curX<prevX){
//                //deceleration
//                push(-1,tendenceX);
//            }else{
//                push(0,tendenceX);
//            }
//            if(curY>prevY){
//                //acceleration
//                push(1,tendenceY);
//            }else if(curY<prevY){
//                //deceleration
//                push(-1,tendenceY);
//            }else{
//                push(0,tendenceY);
//            }
//            if(curZ>prevZ){
//                //acceleration
//                push(1,tendenceX);
//            }else if(curZ<prevZ){
//                //deceleration
//                push(-1,tendenceZ);
//            }else{
//                push(0,tendenceZ);
//            }
//        }
//        public boolean calculateTendence(){
//            boolean result=false;
//            //1 acceleration, constan 0, -1 deceleration.
//          boolean[] b=new boolean[3];
//            b[0]=isChangeDirection(tendenceX);
//            b[1]=isChangeDirection(tendenceY);
//            b[2]=isChangeDirection(tendenceZ);
//            int count=0;
//            for (int i=0;i<b.length;i++){
//                if(b[i]==true){count++;}
//            }
//            if(count>1){result=true;}
//            return result;
//        }
//        public boolean isChangeDirection(int[] r){
//            boolean result=false;
//            int i;
//            int first=0;
//            int second=0;
//            for(i=0; i<(r.length/2);i++){
//                first=first+r[i];
//            }
//            for(int j=r.length-1;j==i;j--){
//                second=first+r[j];
//            }
//            if((first>2 && second<-2)||(first<-2 && second>2)){result=true;}
//            return result;
//        }
        @Override
        public void onSensorChanged(SensorEvent event) {

            synchronized (this) {
                // time_difference=event.timestamp-current_time;
                current_time = event.timestamp;
                // Td=time_difference/1000000000.0f;//seconds
                curX = event.values[0];
                curY = event.values[1];
                curZ = event.values[2];
//initialization of variables
                if (prevX == 0 && prevY == 0 && prevZ == 0) {
                    last_update = current_time;
                    last_movement = current_time;
                    prevX = curX;
                    prevY = curY;
                    prevZ = curZ;
                    //speed[0]=speed[1]=speed[2]=0;
                } else {
                    time_difference = current_time - last_movement;
                    // Log.v(Constants.TAG, " --Time--"+current_time+"---------DIFFER TIME----------"+time_difference);
                    oldmovement = movement;
                    movement = (Math.abs(curX - prevX) + Math.abs(curY - prevY) + Math.abs(curZ - prevZ));
                    push(curX, tendenceX);
                    push(curY, tendenceY);
                    push(curZ, tendenceZ);
                    push(movement, movements);
                    if(count>movements.length){
                        count=0;
                        mMovement = average(movements);
                        X = average(tendenceX);
                        Y = average(tendenceY);
                        Z = average(tendenceZ);
                        //Log.v("Accelerometer RangeOfTime", " average Movement= " + String.valueOf(mMovement) + "-->[X,Y,Z]=  [ " + String.valueOf(X) + " , " + String.valueOf(Y)+ " , " + String.valueOf(Z) + " ]");
                        prevX = curX;
                        prevY = curY;
                        prevZ = curZ;
                    }
                }
                count++;
            }
        }
        //---------------------Fin Accelerometer-------------------//

//    public void writeFile(String log){
//        String date = String.valueOf(System.currentTimeMillis());
//        try{
//
//            OutputStreamWriter file=
//                    new OutputStreamWriter(openFileOutput("iBoblindLog.txt", Context.MODE_WORLD_READABLE));
//            file.write("log");
//            file.write("\n"+date+"\n"+log);
//            file.close();
//        }catch(Exception ex){
//            Log.e("WriteFile","Error al escribir ficheros a memoria interna");
//        }
//    }
    }
}
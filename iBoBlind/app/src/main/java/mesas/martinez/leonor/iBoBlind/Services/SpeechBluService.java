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
public class SpeechBluService extends IntentService implements BluetoothAdapter.LeScanCallback, TextToSpeech.OnInitListener{

//------------------Variables---------------------//
private static boolean start;
private SharedPreferences sharePreference;
private OrionJsonManager jsonManager;
//--to-speak-Variables/Contans,enums---//
private String toSpeak;
private TextToSpeech tts=null;
private boolean ttsInit;
 //-------------for accelerometer----------//
 private Accelerometer accelerometer;
 private int min_timesensitivity = 100000000;
 private int time_sensitivity;
 private float min_movement;
 private ArrayList<Deviceaux> mDevicesArray;
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
    private static final long WAIT_PERIOD = 3000;

    private String address;
    private String device_name;
    private String string_rssi;
    private String old_address;
    private String old_string_rssi;
    private BluetoothAdapter mBluetoothAdapter= null;
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
        mState=State.CONNECTING;
        ttsInit=false;
        start = true;
        tts=null;
    }

    //-----------------------------------------------Main-Method---------------------------//
    @Override
    protected void onHandleIntent(Intent intent) {
        toSpeak = " ";
           if (tts != null) {
          tts.stop();
          tts.shutdown();
          tts=null;
       }
        //Comprobar si estan activos los elementos necesarios

        tts = new TextToSpeech(getBaseContext(),this);
        //tts = new TextToSpeech(this,this);
        tts.setSpeechRate(0.5f);
        tts.setPitch(1.5f);
        //to can reproduce the messages

        mDevicesArray= new ArrayList<Deviceaux>();
        sharePreference = PreferenceManager.getDefaultSharedPreferences(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.DEVICE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.DEVICE_MESSAGE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_STOP));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_UNKNOWN_STATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_WAIT_RESPONSE));

        measuresFORaverage=sharePreference.getInt(Constants.MEASURES,3);
        diferAverage=sharePreference.getInt(Constants.DIFER,2);
        min_movement=sharePreference.getInt(Constants.MOVEMENT,11);
        min_movement=(float)min_movement/10;
        time_sensitivity=sharePreference.getInt(Constants.MOVEMENT,20);
        time_sensitivity=time_sensitivity*min_timesensitivity;

        accelerometer = new Accelerometer();
        accelerometer.setTime_sensitivity(time_sensitivity);
        accelerometer.setMin_movement(min_movement);

        Log.d(Constants.TAG,"------------AFTERONinit----------: /\n--int_movement----"+min_movement+"---diferAverage---"+diferAverage+"----measuresDORaverage---"+measuresFORaverage+"--------time sensitivity-------"+time_sensitivity);

        accelerometer.start();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            myScanCallback=new mScanCallback();
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
                    this.wait(WAIT_PERIOD);}
            } catch (InterruptedException e) {
                Log.d("InterruptedException in While", "------------STOP----------");
                start = false;
            }
        }
    }
    //----------------------------------------------------Methods---------------------------------//
    //------------------to-Speak--Methods---------------------------//
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locSpainh= new Locale("spa","ESP");
            int result = tts.setLanguage(locSpainh);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Ready to Speak", Toast.LENGTH_SHORT).show();
                ttsInit=true;
            }
        } else {
            Toast.makeText(this, "Can Not Speak", Toast.LENGTH_LONG).show();
        }

    }

        @SuppressWarnings("deprecation")
    private void ttsUnder20(String text){
        HashMap<String,String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"MessageId");
        tts.speak(text,TextToSpeech.QUEUE_ADD,map);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsUnder21(String text){
        String utteranceId=this.hashCode()+"";
        tts.speak(text,TextToSpeech.QUEUE_ADD,null,utteranceId);
    }

    protected void speakTheText( ) {
        Log.v("--SPEAKtheTEXT---", toSpeak);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            ttsUnder21(toSpeak);
        }else{
            ttsUnder20(toSpeak);
        }
    }

//-------------------------------------END SPEAK METODS----------------------------------------------//
  public void setState(State state){
      mState = state;
      //Log.d("--Service--Set State--",mState.name());
      String share=Constants.SERVICE_STATE.toString();
      SharedPreferences config=this.getSharedPreferences(share,MODE_MULTI_PROCESS);
      config.edit()
              .putString(Constants.SERVICE_STATE, mState.name())
              .commit();

  }

    protected void mstop(){
        Log.d("---STOP SERVICE--"," "+start);

        if(start!=false){
            start = false;
            mDevicesArray.clear();
            mBluetoothAdapter.stopLeScan(SpeechBluService.this);
            this.setState(State.DISCONNECTING);
            // Stop the TTS Engine when you do not require it
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
           //Stop accelerometer
           accelerometer.stop();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            this.stopSelf();//Stop service
            this.onDestroy();
        }
        
    }

    private BroadcastReceiver mReceiver=new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            switch(action) {
                case Constants.DEVICE:
                    int id=intent.getIntExtra("id",-1);
                    Log.i("OnL--INTENT DEVICE received-----------id--",String.valueOf(id));
                    if(id!=-1){
                    int mrssi=intent.getIntExtra("rssi",0);
                    int mcoberageAlert=intent.getIntExtra("coverageAlert",0);
                    toSpeak = intent.getStringExtra("message");
                    String maddress=intent.getStringExtra("address");
                    Log.i("OnL--INTENT DEVICE received-------------",maddress+", mesage: "+toSpeak+", rssi "+String.valueOf(mrssi)+", coberageAlert"+String.valueOf(mrssi));

                    if(!toSpeak.equals(null)){
                       SpeechBluService.this.speakTheText( );
                        //speakTheText(toSpeak);
                        Deviceaux mdeviceaux=new Deviceaux(maddress,mcoberageAlert,toSpeak,mrssi);
                        int index = mDevicesArray.indexOf(mdeviceaux);
                        Log.d("OnL--INTEN DEVICE--","------ index: "+ String.valueOf(index));
                        if(index<0){
                            mDevicesArray.add(mdeviceaux);
                            index=mDevicesArray.indexOf(mdeviceaux);
                            Log.d("OnL--INTEN DEVICE--","---device added--- index: "+ String.valueOf(index));
                        }else{
                            mDevicesArray.set(index,mdeviceaux);
                            Log.d("OnL--INTEN DEVICE--","---device update--- index: "+ String.valueOf(index));
                        }
                    }}
                    SpeechBluService.this.setState(State.UNKNOWN);
                    break;
                case Constants.DEVICE_MESSAGE:
                    toSpeak = intent.getStringExtra("message");
                    Log.i("-----------INTENT received-------------","---DEVICE_MESSAGE--"+toSpeak);
                    if(!toSpeak.equals(null))
                        SpeechBluService.this.speakTheText( );
                        //speakTheText(toSpeak);
                    break;
                case Constants.SERVICE_STOP:
                    SpeechBluService.this.mstop();
                    Log.i("-----------INTENT received-------------","---STOP SERVICE--");
                    break;
                case Constants.SERVICE_WAIT_RESPONSE:
                    SpeechBluService.this.setState(State.WAIT_RESPONSE);
                    Log.i("-----------INTENT received-------------","--SERVICE_WAIT_RESPONSE--");
                    break;
                case Constants.SERVICE_UNKNOWN_STATE:
                    SpeechBluService.this.setState(State.UNKNOWN);
                    Log.i("-----------INTENT received-------------","---SERVICE_UNKNOWN_STATE--");
                    break;
                default:
                    Log.i("-----------INTENT received-------------", action+"---not catched--");
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
    private void stopLeScan(){
        mBluetoothAdapter.stopLeScan(SpeechBluService.this);
    }
    @SuppressWarnings("deprecation")
    private void startLeScan(){
        mBluetoothAdapter.startLeScan(SpeechBluService.this);
    }
    private void start() {
           // Log.d("startScan:", "------------------Star-----------------------\n\n\n ");
// scan for SCAN_TIMEOUT
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("Start ","--------------stopLeScan-------------");
                    //SpeechBluService.this.setState(State.WAIT);
                    if(Build.VERSION.SDK_INT<21){
                        stopLeScan();
                    }else{
                        mLEScanner.stopScan(myScanCallback);
                    }
                }
            }, SCAN_TIMEOUT);
        //Wait for HTTP_JSON_POST end, before scan again
       // if((!mState.equals(State.SCANNING)) && (!mState.equals(State.WAIT_RESPONSE)) ){
//                        Log.i("---onHandleIntent WHILE--", "WAIT FOR STATE CHANGE");
            SpeechBluService.this.setState(State.SCANNING);
            Log.d("Start ","--------------startLeScan-------------");
        if(Build.VERSION.SDK_INT<21){
            startLeScan();
        }else{
            mLEScanner.startScan(filters,settings,myScanCallback);
        }

        //}
     }

    private void startScan() throws InterruptedException {
        if (mBluetoothAdapter== null) {
            Log.d("Start Scan","mBluetoothAdapter==null");
            final BluetoothManager BluetoothManager = (android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = BluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter== null || !mBluetoothAdapter.isEnabled()) {
            Log.d("Start Scan","mBluetoothAdapter is not Enable");
            Log.i("starScan","BLUETOOH is OFF");
            this.setState(State.BLUETOOTH_OFF);
            String turn_on=getResources().getString(R.string.turn_on_Bluetooth);
            Toast.makeText(this, turn_on, Toast.LENGTH_LONG).show();
            this.wait(Constants.WAIT_TIME);
            //please trun on Bluetthoth sensor
            Intent intent = new Intent(Constants.BLUETOOTH_OFF);
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
            start=false;
        } else {
            if(Build.VERSION.SDK_INT>=21){
                mLEScanner=mBluetoothAdapter.getBluetoothLeScanner();
                settings=new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
                filters=new ArrayList<ScanFilter>();
            }
            //Log.i("starScan","-----------------------------------!!!!!---mDeviceArray-----Clear--!!!!--------------------------------");
           // mDevicesArray.clear();
            SpeechBluService.this.start();
        }
    }
    //--------------------------------------------------------------------When a i-becaon is detected---------------------------------------//
    public void OnDeviceDetected(final BluetoothDevice device, int rssi){
        address = device.getAddress().toString();
         string_rssi = String.valueOf(rssi);
        device_name=device.getName();
        Deviceaux auxdevice=new Deviceaux(rssi,address);
        int index=mDevicesArray.indexOf(auxdevice);
        Log.d("OnLeScan","----New Device--- address: "+ address+ " rssi "+string_rssi);
        jsonManager=new OrionJsonManager() ;
        //String jsonString=jsonManager.SetJSONtoGetMessage("BLE", address);
        String jsonString=jsonManager.SetJSONtoGetAttributes("BLE", address,getApplicationContext());
        if(device_name!=null){
            jsonManager.setDeviceName(device_name);}

        // Log.d("OnLeScan","----Device detected--- index: "+ String.valueOf(index));

        //toSpeak="prueva";
        //Toast.makeText(this, toSpeak, Toast.LENGTH_LONG).show();
        //SpeechBluService.this.speakTheText();

       if(index<0){
           // The first time the user is in the region we say the  RegionEnter message ; and it save state.
                Log.d("OnLeScan","----New Device--- address: "+ address+ " rssi "+string_rssi);
                new HTTP_JSON_POST(this, jsonManager,address,rssi).execute();

           } else {
               Log.d("OnLeScan","----Detected device again----"+ address+ " rssi "+string_rssi);
               speakUpdateDeviceauxAgain(index,rssi);

           }
    }

//Method from LeScanCallBack
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
      OnDeviceDetected(device, rssi);
    }
    //---------------------For api 21---------------------//
    @TargetApi(21)
    private class mScanCallback extends ScanCallback{
            public mScanCallback(){
                super();
            }
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
          BluetoothDevice btDevice=result.getDevice();
          int rssi=result.getRssi();
          OnDeviceDetected(btDevice, rssi);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult sr: results){
                Log.i("ScanResult-Results",sr.toString());
            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code:"+errorCode);

        }
    };
    //-----------------FIn api 21------------------//
    //---------------------------------------------------------------Fin-----When a i-becaon is detected---------------------------------------//
    //---------------Device again--------------------//
    private void speakUpdateDeviceauxAgain(int index,int rssi){
        Deviceaux auxdevice=mDevicesArray.get(index);

        String address=auxdevice.getAddress();
        String speak = auxdevice.getText();
        int state = auxdevice.getState();
        double limitRegion = auxdevice.getOutOfRegion();
        double LastAverage = auxdevice.getdBmAverage();
        auxdevice.setdBmAverage(rssi);
        auxdevice.setLastdBmAverage(LastAverage);
        double dBmAverage =auxdevice.getdBmAverage();
        double difer = LastAverage - dBmAverage;
        int count;
        Log.d("SPEECHBLUSERVICE", "----" + address + "--------dBmAverage-----=" + dBmAverage + ", LastAverage=" + LastAverage + " difer= " + difer + " difer>0" + (difer > 0) + " difer> " + diferAverage + " " + (difer > diferAverage));

// While the user is moving in the region , we calculate a mean of 3 updates before Indicate the movement.
// when the user go out of the region. we say RegionOut message and it save state .

            if (accelerometer.RangeOfTime()) {
                count = auxdevice.getCount() + 1;
                auxdevice.setCount(count);
                Log.d("SPEECHBLUSERVICE", "----" + address + "-RangeOfTime----TRUE---count---=" + count + "(count > measuresFORaverage)= " + (count > measuresFORaverage));
                //text movement of user
                if (count > measuresFORaverage) {
                    //say approach or move away
                    //int a = new Double(dBmAverage).compareTo(new Double(LastAverage));
                    Log.d("SPEECHBLUSERVICE", "----" + address + "-RangeOfTime----TRUE---difer---=" + difer  + "(difer > diferAverage)= " + (difer > diferAverage));
                    if (difer > diferAverage) {
                        //move away
                        auxdevice.setCount(0);
                        //Out off range?
                        //int b = new Double(dBmAverage).compareTo(new Double(limitRegion - ajustOutOffRange));
                        int b = new Double(dBmAverage).compareTo(new Double(limitRegion));
                        if (b == -1) {
                            count = 0;
                           auxdevice.setCount(0);
                            auxdevice.setLastdBmAverage(rssi);
                            auxdevice.setdBmAverage(rssi);
                            String Text=getResources().getString(R.string.Out_Of);
                            toSpeak = Text+ speak;
                            SpeechBluService.this.speakTheText( );
                            mDevicesArray.remove(index);
                           } else {
                            String Text=getResources().getString(R.string.move_away_to);
                            toSpeak = Text+ speak;
                            SpeechBluService.this.speakTheText( );

                        }

                        auxdevice.setLastdBmAverage(dBmAverage);
                    } else {
                        Log.d("SPEECHBLUSERVICE", "----" + address + "-RangeOfTime----TRUE---difer---=" + difer  + "(difer < -diferAverage)= " + (difer < -diferAverage));
                        //approach
                        if (difer < -diferAverage) {
                            count = 0;
                            auxdevice.setCount(0);
                            String Text=getResources().getString(R.string.approach_to);
                            toSpeak = Text+ speak;
                            SpeechBluService.this.speakTheText( );
                            auxdevice.setLastdBmAverage(dBmAverage);
                        }
                    }
                    Log.d("SPEECHBLUSERVICE", "--movin " +toSpeak+" "+ address);
                }

            }//Fin if a
        }


    //---------------To now the movement direction-------------//
    class Accelerometer implements SensorEventListener {

        private long last_update = 0, last_movement = 0;
        private float prevX = 0, prevY = 0, prevZ = 0;
        private float curX = 0, curY = 0, curZ = 0;
        private float movement;
        private int min_timesensitivity = 100000000;
        private int time_sensitivity;
        //float base_movement = 1E-6f;
        private float min_movement=11;
        private long time_difference=2;
        private long current_time;
        private SensorManager sm;
        private List<Sensor> sensors;

        public int getTime_sensitivity() {
            return time_sensitivity;
        }

        public void setTime_sensitivity(int time_sensitivity) {
            this.time_sensitivity = time_sensitivity;
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
            sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);

        }
        public void start(){
            if (sensors.size() > 0) {
                sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_UI);
            }
        }
        public boolean RangeOfTime() {
            boolean response = false;
            //*5.0 because discober devices is slow that accelerometer
            if (time_difference < (time_sensitivity)) {
                response = true;
            }
            //Log.d(Constants.TAG, "-----RANGE OF TIMER------" + response + "--" + time_difference + " < " + (time_sensitivity));
            return response;
        }

        public void stop(){
            sm.unregisterListener(this, sensors.get(0));
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                current_time = event.timestamp;
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
                }
                time_difference = current_time - last_movement;

           //Log.v(Constants.TAG, " --Time--"+current_time+"---------DIFFER TIME----------"+time_difference);
                if (time_difference > time_sensitivity) {
                    movement = (Math.abs(curX - prevX) + Math.abs(curY - prevY) + Math.abs(curZ - prevZ));
                    // Log.v(Constants.TAG, "\n\n-----Movement0------"+movement+" > "+min_movement+" --Time--"+current_time+"---------DIFFER TIME----------"+time_difference);
                    if (movement > min_movement) {
                        last_movement = current_time;
                       // Log.v(Constants.TAG, "\n\n-----Movement1------" + movement + " > " + min_movement + " --Time--" + current_time + "---------DIFFER TIME----------" + time_difference);
                        prevX = curX;
                        prevY = curY;
                        prevZ = curZ;
                    }
                }
            }
        }
    }
    //---------------------Fin Accelerometer-------------------//
}
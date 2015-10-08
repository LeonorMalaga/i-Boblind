package mesas.martinez.leonor.iBoBlind.Activitys;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mesas.martinez.leonor.iBoBlind.R;
import mesas.martinez.leonor.iBoBlind.comunication.HTTP_JSON_POST;
import mesas.martinez.leonor.iBoBlind.model.Constants;
import mesas.martinez.leonor.iBoBlind.model.Device;
import mesas.martinez.leonor.iBoBlind.model.DeviceDAO;
import mesas.martinez.leonor.iBoBlind.model.MyBledevice;
import mesas.martinez.leonor.iBoBlind.model.MySQLiteHelper;
import mesas.martinez.leonor.iBoBlind.model.OrionJsonManager;
import mesas.martinez.leonor.iBoBlind.model.Project;
import mesas.martinez.leonor.iBoBlind.model.ProjectDAO;
import mesas.martinez.leonor.iBoBlind.Services.GPSservice;

/**
 * Created by leonor on 29/01/15.
 */
public class Installer_Activity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {
    //atributtes
    private String project_name;
    private Button new_project_button;
    private Spinner projects_spinner;
    private ListView bluetooth_ListView;
    private Button find_ibeacon_button;
    private EditText specifications;
    private TextView data_validation;
    private Project projectaux;
    private ProjectDAO projectDAO;
    private Device deviceaux;
    private DeviceDAO deviceDAO;
    private int project_id;
    private int device_id;
    private MySQLiteHelper Database;
    private List<Project> projectList;
    private ArrayAdapter<CharSequence> adapter;
    private ArrayList<String> arrayListaux;
    private String auxName;
    private String charaux;
    private static final int RESULT_SETTINGS = 2;
    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean mScanning = false;
    //0 no scann, 1 scann, 2 device faund
    private int deviceFaund = 0;
    private Handler mHandler = new Handler();
    private ListAdapter mLeDeviceListAdapter;
    private static final long SCAN_TIMEOUT = 5000;
    private String mlatitude = "0";
    private String mlongitude = "0";
    private GPSservice gps;
    private MyBledevice ble;
    private String address;
    private String deviceName;
    private String rssi;
    private String specifications_text;
    private String workMode;

    static class ViewHolder {
        public TextView text;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.v("---DEVICE FAUND---", device.getAddress().toString() + ", rss1=" + rssi);
            deviceFaund = 2;
            final MyBledevice mydevice = new MyBledevice(device, rssi);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.myaddDevice(mydevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    //---------------------Principal Methods---------------//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);


    }


    @Override
    protected void onResume( ) {
        super.onResume();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        workMode = sharedPrefs.getString(Constants.WORKMODE, "0");
        Log.d("------------NOT FIRST--WOORK MODE----------: " + workMode.equals("1"), workMode);
        if (workMode.equals("0")) {
            startActivity(new Intent(getApplicationContext(), User_Activity.class));
        } else {
            //stop service if exit
            Intent intent = new Intent(Constants.SERVICE_STOP);
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
            //
            specifications = (EditText) this.findViewById(R.id.device_specification_editText);
            data_validation = (TextView) this.findViewById(R.id.intaller_response_textView);
            data_validation.setVisibility(View.INVISIBLE);
            new_project_button = (Button) this.findViewById(R.id.new_project_button);
            find_ibeacon_button = (Button) this.findViewById(R.id.find_ibeacon_button);
            bluetooth_ListView = (ListView) this.findViewById(R.id.ibeacon_ListView);
            projects_spinner = (Spinner) this.findViewById(R.id.projects_show_spinner);
            projects_spinner.setOnItemSelectedListener(this);

            final BluetoothManager BluetoothManager = (android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = BluetoothManager.getAdapter();
            mLeDeviceListAdapter = new ListAdapter();
            bluetooth_ListView.setAdapter(mLeDeviceListAdapter);

            projectDAO = new ProjectDAO(getApplicationContext());
            projectDAO.open();
            projectList = projectDAO.getAll();
            projectDAO.close();
            // Create an ArrayAdapter using the string array and a default spinner layout
            project_name = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SPINNER_NAME, "Anonymous27");
            arrayListaux = new ArrayList();
            int size = projectList.size();
            if (size < 0) {
                startActivity(new Intent(getApplicationContext(), NewProject_Activity.class));
            }
            ;
            for (int i = 0; i < size; i++) {
                Log.d("------sice_index-----", size + "_" + i);
                projectaux = projectList.get(i);
                charaux = projectaux.getmprojectName();
                if (charaux.equals(project_name) && i != 0) {
                    auxName = arrayListaux.get(0);
                    Log.d("----auxName----->", auxName);
                    arrayListaux.set(0, project_name);
                    arrayListaux.add(i, auxName);
                } else {
                    arrayListaux.add(charaux);
                }

            }

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListaux);
// Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
            projects_spinner.setAdapter(adapter);
//Create a new project
            new_project_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), NewProject_Activity.class));
                }
            });

            bluetooth_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    ble = mLeDeviceListAdapter.mygetDevice(pos);
                    Log.d("------->Device select---Adrees->" + ble.device.getAddress(), "---->rssi-->" + ble.rssi);
                    data_validation.setVisibility(View.VISIBLE);
                    gps = new GPSservice(getApplicationContext());
                    // check if GPS enabled
                    if (gps.canGetLocation()) {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        Log.d("--LocationOk---", "---");
                        mlatitude = String.valueOf(latitude);
                        mlongitude = String.valueOf(longitude);
                    }

                    //set variables to can save ibeacon
                    address = ble.device.getAddress();
                    deviceName = ble.device.getName();
                    rssi = String.valueOf(ble.rssi);
                    specifications_text = specifications.getText().toString();
                    projectDAO = new ProjectDAO(getApplicationContext());
                    projectDAO.open();
                    project_name = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SPINNER_NAME, "Anonymous27");
                    projectaux = projectDAO.getProjectByName(project_name);
                    project_id=projectaux.get_id();
                    projectDAO.close();
                    //Now We want to Now if the device is already registered in the database
                    deviceDAO = new DeviceDAO(getApplicationContext());
                    deviceDAO.open();
                    deviceaux = deviceDAO.getDeviceByAddressAndProject(address, projectaux.get_id());
                    deviceDAO.close();
                    if((project_id==-1)){
                        String cproject=getResources().getString(R.string.new_project_text);
                        data_validation.setText(cproject);
                    }else if ((deviceaux.get_id() != -1)) {
                        data_validation.setText("This device can not be save.The device address already exist in the specified Project");
                    } else {
                        OrionJsonManager jsonManager=new OrionJsonManager();
                        String json=jsonManager.SetJSONtoCreateEntity("BLE", address, mlatitude, mlongitude, specifications_text, rssi, "45713701M", project_name, deviceName) ;
                        //String query="/ngsi10/updateContext";
                        //new HTTP_JSON_POST(getApplicationContext(),data_validation,HTTP_JSON_POST.Gender.UPDATE_CREATE,json).execute();
                        new HTTP_JSON_POST(getApplicationContext(),jsonManager,data_validation).execute();
                    }

                }//onItemClick
            });
        }

    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        String name = parent.getItemAtPosition(pos).toString();
        Log.d("--Spinner select->", name);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putString(Constants.SPINNER_NAME, name)
                .commit();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    //--------------SETTINGS----------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                //showUserSettings();
                break;
        }
    }
//----------------------Auxyliar Metodos--------------------------//
    private void setScanState(boolean value) {
        mScanning = value;
        ((Button) this.findViewById(R.id.find_ibeacon_button)).setText(value ? "Stop": "Scan");
      if(deviceFaund == 1){
          String swo=this.getString(R.string.no_new_device);
        ((TextView) this.findViewById(R.id.intaller_response_textView)).setText(swo);}
    }

    //Set Scan botton
    public void onScan(View view) {
// check Bluetooth is available and on
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }
        scanLeDevice(!mScanning);
    }

    //Start or Stop the scan
    private void scanLeDevice(final boolean enable) {
        deviceFaund = 1;
        if (enable) {
// scan for SCAN_TIMEOUT
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setScanState(false);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_TIMEOUT);
            setScanState(true);
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            setScanState(false);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

        }
    }

    //----------------Auxyliar Class------------------------//

    // adaptor
    private class ListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<MyBledevice> myLeDevices;

        public ListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            myLeDevices = new ArrayList<MyBledevice>();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public void myaddDevice(MyBledevice device) {
            if (!myLeDevices.contains(device)) {
                myLeDevices.add(device);
            }
        }

        public MyBledevice mygetDevice(int position) {
            return myLeDevices.get(position);
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void myclear() {
            myLeDevices.clear();
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            //return mLeDevices.size();
            return myLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            // return mLeDevices.get(i);
            return myLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = Installer_Activity.this.getLayoutInflater().inflate(R.layout.list_row, null);
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) view.findViewById(R.id.textView);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            MyBledevice device = myLeDevices.get(i);
            final String deviceName = device.device.getName();
            String deviceAdrees = device.device.getAddress();
            int rssi = device.rssi;
            if (deviceAdrees != null && deviceAdrees.length() > 0) {
            } else {
                deviceAdrees = "0";
            }
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.text.setText("Name: " + deviceName + ", Address:" + deviceAdrees + ",------>" + rssi + "dB");
            else
                viewHolder.text.setText("Name: Anonymous, Address:" + deviceAdrees + ",------>" + rssi + "dB");
            return view;
        }
    }

}

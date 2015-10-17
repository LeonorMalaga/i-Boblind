package mesas.martinez.leonor.iBoBlind.Activitys;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import mesas.martinez.leonor.iBoBlind.R;
import mesas.martinez.leonor.iBoBlind.Services.SpeechBluService;
import mesas.martinez.leonor.iBoBlind.model.Constants;
import mesas.martinez.leonor.iBoBlind.model.MySQLiteHelper;
import mesas.martinez.leonor.iBoBlind.model.Project;
import mesas.martinez.leonor.iBoBlind.model.ProjectDAO;

import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;

import java.util.List;

/**
 * Created by root on 5/08/15.
 */
public class User_Activity extends ActionBarActivity  {

//--------------------------------Contans----------------------------------//
    private final int ENABLE_BT = 2;
 //   private static final int RESULT_SETTINGS = 1;
//-------------------------------Variables---------------------------------//
    private int first;
    private MySQLiteHelper DatabaseInstaler;
    private MySQLiteHelper DatabaseUser;
    private String workMode;
    private Button stop_start;
    private Button installer;
    private TextView user_first_text;
    private TextView user_secon_text;
    private TextView user_text;
    private String serviceState;
    private SharedPreferences sharedPrefs;
    private String buttonText;
//------------------------------SubClass----------------------------------//
    private BroadcastReceiver mMessageReceiver=new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT);
        }
    };

    @Override
    public void onBackPressed() {
        Log.d("User_Activity","--OnBackPressed--");
        CharSequence text= stop_start.getText();
        if(text.equals(getResources().getString(R.string.stop_service))){
            String back_pressed=getResources().getString(R.string.back_pressed);
            Toast.makeText(this, back_pressed, Toast.LENGTH_LONG).show();
//            try {
//                wait(500);
//            } catch (InterruptedException e) {
//
//            }
        }
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Log.d("User_Activity","--OnBackPressed--");
            CharSequence text= stop_start.getText();
            if(text.equals(getResources().getString(R.string.stop_service))){
                String back_pressed=getResources().getString(R.string.back_pressed);
                Toast.makeText(this, back_pressed, Toast.LENGTH_LONG).show();
//                try {
//                    wait(500);
//                } catch (InterruptedException e) {
//
//                }
            }
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    //----------------------------------------Methods--------------------------------/
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Firststart();
        workMode = sharedPrefs.getString(Constants.WORKMODE, "0");
        Log.d("------------NOT FIRST--WOORK MODE----------: " + workMode.equals("1"), workMode);
        if (workMode.equals("1")) {
            Log.d("------------Installer Activity---","---Launching--");
            startActivity(new Intent(getApplicationContext(), Installer_Activity.class));
        } else {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("BLUETOOTH_OFF"));
            Log.d("--------------WOORK MODE----------: ", "USER");
            user_first_text = (TextView) this.findViewById(R.id.user_first_textView);
            user_secon_text = (TextView) this.findViewById(R.id.user_second_textView);
            user_secon_text.setVisibility(View.INVISIBLE);
            user_text=(TextView) this.findViewById(R.id.user_textView);
            installer=(Button) this.findViewById(R.id.installer_button);
            stop_start = (Button) this.findViewById(R.id.user_button);
            stop_start.setVisibility(View.INVISIBLE);
    }}
    @Override
    protected void onResume( ) {
        super.onResume();
        workMode = sharedPrefs.getString(Constants.WORKMODE, "0");
        Log.d("------------NOT FIRST--WOORK MODE----------: " + workMode.equals("1"), workMode);
        if (workMode.equals("1")) {
            Log.d("------------Installer Activity---","---Launching--");
            startActivity(new Intent(getApplicationContext(), Installer_Activity.class));
        }else{
        serviceState=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SERVICE_STATE, SpeechBluService.State.DISCONNECTING.name());
        if(!serviceState.equals(SpeechBluService.State.SCANNING.name()) && !serviceState.equals(SpeechBluService.State.WAIT.name())) {
            //By default we start the detection service
            this.startService();
            String firstText=getResources().getString(R.string.user_first_text);
            user_first_text.setText(firstText);
            user_secon_text.setVisibility(View.VISIBLE);
            stop_start.setVisibility(View.VISIBLE);
        }else{
            buttonText=getResources().getString(R.string.start_service);
            stop_start.setText(buttonText);
            String firstText=getResources().getString(R.string.user_first_textView);
            user_first_text.setText(firstText);
            user_secon_text.setVisibility(View.INVISIBLE);
        }

        stop_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               CharSequence text= stop_start.getText();
                if(text.equals(getResources().getString(R.string.start_service))){
                  //start Service and change the button text
                 User_Activity.this.startService();
                    String firstText=getResources().getString(R.string.user_first_text);
                    user_first_text.setText(firstText);
                    user_secon_text.setVisibility(View.VISIBLE);
                    stop_start.setVisibility(View.VISIBLE);
                }else{
                   //stop Service and change the button text
                    stopService();
                }
            }
    });

        installer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("clicked","----------installer mode--------");
                LoginDialog password=new LoginDialog(User_Activity.this);
                password.show();

            }
        });}}

private void Firststart(){
        try {
            first = PreferenceManager.getDefaultSharedPreferences(User_Activity.this).getInt(Constants.FIRST, 0);
            Log.d("-FIRST-: ", String.valueOf(first));
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            if (first != 168451239) {              
                workMode = sharedPrefs.getString(Constants.WORKMODE, "0");
                Log.d("--FIRST-WOORK MODE-: ", workMode);
                DatabaseInstaler = new MySQLiteHelper(getApplicationContext());
                PreferenceManager.getDefaultSharedPreferences(User_Activity.this)
                        .edit()
                        .putInt(Constants.FIRST, 168451239)
                        .commit();
                //Create a Default Project
                Project project = new Project("Default", "Create by the app");
                ProjectDAO projectDAO = new ProjectDAO(getApplicationContext());
                projectDAO.open();
                int project_id = projectDAO.create(project);
                projectDAO.close();
                project.set_id(project_id);
            }

        }catch(Exception e){
            Intent i = new Intent();
            i.setClass(getApplicationContext(),User_Activity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            // Show toast to the user
            Toast.makeText(getApplicationContext(), "Data lost due to excess use of other apps", Toast.LENGTH_LONG).show();
        }
    }


    
private void stopService(){
    //stop Service and change the button text
    Intent intent = new Intent(Constants.SERVICE_STOP);
    LocalBroadcastManager.getInstance(User_Activity.this).sendBroadcastSync(intent);
    buttonText=getResources().getString(R.string.start_service);
    stop_start.setText(buttonText);
    String firstText=getResources().getString(R.string.user_first_textView);
    user_first_text.setText(firstText);
    user_secon_text.setVisibility(View.INVISIBLE);
}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
//            case RESULT_SETTINGS:
//                super.onActivityResult(requestCode, resultCode, data);
//                //showUserSettings();
//                break;
            case ENABLE_BT:
                if(resultCode!=RESULT_OK){finish();}
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
//    //----------------------Settings----------------------//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Intent i = new Intent(this, SettingsActivity.class);
//                startActivityForResult(i, RESULT_SETTINGS);
//                break;
//        }
//        return true;
//    }
        //---------------My Methods---------------------//
        private void startService(){
            //Comprobar si estan activos los elementos necesarios
            PackageManager pm=getPackageManager();
            List<ResolveInfo> activities=pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
            //Is TTS intaller
            if(activities.size()!=0){
                Log.d("User_Activity/\n", "                           --------------------SPEECH RECOGNIZE---------------               ");
                //Is bluetooth on
                final BluetoothManager BluetoothManager = (android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter mBluetoothAdapter = BluetoothManager.getAdapter();

                if (mBluetoothAdapter== null || !mBluetoothAdapter.isEnabled()) {
                    Log.i("starScan","BLUETOOH is OFF");
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, ENABLE_BT);
                }else{
                    Log.d("User_Activity/\n", "                          ---------------------BLUETOOH ON----------------               ");
                    serviceState = SpeechBluService.State.CONNECTING.name();
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putString(Constants.SERVICE_STATE, serviceState)
                            .commit();
                    Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SpeechBluService.class);
                    startService(intent);
                    buttonText=getResources().getString(R.string.stop_service);
                    stop_start.setText(buttonText);
                }
            }else{
                Log.i("starScan","SPEECH NOT INSTALLED");
                Intent installTTS= new Intent();
                installTTS.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTS);
            }
        //
    }
    //-------------------------Dialog to ask for Paswword------------------------//

    public class LoginDialog extends Dialog implements android.view.View.OnClickListener {
        private EditText password;
        private String password_text;
        public Activity mactivity;
        public Button cancelButton;
        public Button continueButton;

        public LoginDialog(Activity activity){
                super(activity);
                this.mactivity=activity;
            }
    @Override
     protected void onCreate(Bundle savedInstanceState){
                super.onCreate(savedInstanceState);
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.installer_password);
                cancelButton=(Button)findViewById(R.id.cancel_button);
                continueButton=(Button)findViewById(R.id.continue_button);
                password=(EditText)findViewById(R.id.password);
                cancelButton.setOnClickListener(this);
                continueButton.setOnClickListener(this);
            }
     @Override
     public void onClick(View v){
                      switch(v.getId()){
                          case R.id.cancel_button:
                              dismiss();
                           break;
                          case R.id.continue_button:
                        password_text=password.getText().toString();
                        Log.i("OnContinueClick PASSWORD read: ","----------->"+password_text) ;
                        if(password_text.equals("iBoblind")){
                            User_Activity.this.stopService();
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                    .edit()
                                    .putString(Constants.WORKMODE, "1")
                                    .commit();
                            startActivity(new Intent(getApplicationContext(), Installer_Activity.class));
                        }else{
                            dismiss();}
                          break;
                          default:
                              dismiss();
                              break;

                      }


    }
}



}



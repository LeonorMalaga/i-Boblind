package mesas.martinez.leonor.iBoBlind.Activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import mesas.martinez.leonor.iBoBlind.R;
import mesas.martinez.leonor.iBoBlind.model.Constants;
import mesas.martinez.leonor.iBoBlind.model.MySQLiteHelper;

/**
 * Created by leonor martinez mesas on 21/01/15.
 */
public class FirstActivity extends ActionBarActivity {
//public class FirstActivity extends AppCompatActivity{
    private static final int RESULT_SETTINGS = 1;
    //attributes
    int first;
    private Button startDefaultButton;
    MySQLiteHelper DatabaseInstaler;
    MySQLiteHelper DatabaseUser;
    String workMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        startDefaultButton = (Button) this.findViewById(R.id.startDefault_button);
        //showUserSettings();


    }
    @Override
    protected void onResume( ) {
        super.onResume();
        try {
            first = PreferenceManager.getDefaultSharedPreferences(FirstActivity.this).getInt(Constants.FIRST, 0);
            Log.d("-FIRST-: ", String.valueOf(first));
            if (first == 168451239) {
                //Itś not the first time, look the work mode and jump to the correct activity
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                workMode = sharedPrefs.getString(Constants.WORKMODE, "0");
                Log.d("-NOT FIRST-: ", workMode);
                if (workMode.equals("0")) {

                    startActivity(new Intent(getApplicationContext(), User_Activity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), Installer_Activity.class));
                }

            } else {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                workMode = sharedPrefs.getString(Constants.WORKMODE, "0");
                Log.d("--FIRST-WOORK MODE-: ", workMode);
                DatabaseInstaler = new MySQLiteHelper(getApplicationContext());
                PreferenceManager.getDefaultSharedPreferences(FirstActivity.this)
                        .edit()
                        .putInt(Constants.FIRST, 168451239)
                        .commit();
            }

            startDefaultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (workMode.equals("0")) {
                        startActivity(new Intent(getApplicationContext(), User_Activity.class));
                    } else {
                        startActivity(new Intent(getApplicationContext(), Installer_Activity.class));
                    }
                }
            });
        }catch(Exception e){
            Intent i = new Intent();
            i.setClass(getApplicationContext(),FirstActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            // Show toast to the user
            Toast.makeText(getApplicationContext(), "Data lost due to excess use of other apps", Toast.LENGTH_LONG).show();
        }
    }

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
                break;
        }
    }

}

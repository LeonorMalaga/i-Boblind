package mesas.martinez.leonor.iBoBlind.Activitys;


import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import mesas.martinez.leonor.iBoBlind.R;
import mesas.martinez.leonor.iBoBlind.model.Constants;
import mesas.martinez.leonor.iBoBlind.model.Project;
import mesas.martinez.leonor.iBoBlind.model.ProjectDAO;

/**
 * Created by leonormartinezmesas on 29/01/15.
 */
public class NewProject_Activity extends ActionBarActivity {

    private static final int RESULT_SETTINGS = 3;
    //attributes
    int firstInstaller;
    private Button saveButton;
    private EditText projectName;
    private String projectName_string;
    private EditText projectSpecification;
    private String projectSpecification_string;
    private TextView save;
    private Project project;
    private ProjectDAO projectDAO;
    private int project_id;
    private Toast error;
    private TextView menssage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_project);
        menssage = (TextView) this.findViewById(R.id.message_textView);
        //showUserSettings();
        firstInstaller = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(Constants.FIRSTINSTALLER, 0);
        if (firstInstaller == 278456289) {
            menssage.setVisibility(View.INVISIBLE); //Itś not the first time,
        }
        saveButton = (Button) this.findViewById(R.id.first_installer_button);
        projectName = (EditText) this.findViewById(R.id.project_name_editText);
        projectSpecification = (EditText) this.findViewById(R.id.project_specification_editText);
        save = (TextView) this.findViewById(R.id.first_intaller_response_textView);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                projectSpecification_string = projectSpecification.getText().toString();
                projectName_string = projectName.getText().toString();
                Log.d("-----Project Name-----------", projectName_string);
                Log.d("-----Project Specification-----------", projectSpecification_string);
                project = new Project(projectName_string, projectSpecification_string);
                projectDAO = new ProjectDAO(getApplicationContext());
                projectDAO.open();
                project_id = projectDAO.create(project);
                projectDAO.close();
                project.set_id(project_id);
                if (project_id == -1) {
                    save.setText("This project can not be save. Maybe: the project name exist,or it is not enough space in the database ");
                } else {
                    startActivity(new Intent(getApplicationContext(), Installer_Activity.class));
                }
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putInt(Constants.FIRSTINSTALLER, 278456289)
                .commit();
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
                //showUserSettings();
                break;
        }
    }

}


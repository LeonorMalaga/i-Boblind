package mesas.martinez.leonor.iBoBlind.comunication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.io.BufferedReader;

import mesas.martinez.leonor.iBoBlind.R;
import mesas.martinez.leonor.iBoBlind.Services.GPSservice;
import mesas.martinez.leonor.iBoBlind.model.Constants;
import mesas.martinez.leonor.iBoBlind.model.Device;
import mesas.martinez.leonor.iBoBlind.model.DeviceDAO;
import mesas.martinez.leonor.iBoBlind.model.OrionJsonManager;
import mesas.martinez.leonor.iBoBlind.model.Project;
import mesas.martinez.leonor.iBoBlind.model.ProjectDAO;

/**
 * Created by Leonor Martinez Mesas on 24/07/15.
 * Entries <context, Header, body>
 */
public class HTTP_JSON_POST extends AsyncTask<String,Void,String>{
    //---------------------------Variables/Structures---------------------------------------------//
   public enum Gender{
      UPDATE_CREATE("/ngsi10/updateContext",0),
      GET_MESSAGE("/ngsi10/queryContext",1),
      GET("/ngsi10/queryContext",2);
        private String query;
        private int index;
        private Gender(String query, int index){
            this.query=query;
            this.index=index;
        }

        @Override
        public String toString() {
            return query;
        }

    }


    private URL url;
    private String body;
    private String stringUrl;
    private String date;
    private String error=" ";
    private Gender gender;
    private JSONObject json;
    TextView data_validation;
    private String address="0";
    private String message;
    private int rssi;
    private int coberageAlert;
    //Variables to work with Database
    private Project projectaux;
    private ProjectDAO projectDAO;
    private int project_id;
    private int device_id;
    private Device mDevice;
    private DeviceDAO deviceDAO;
    private OrionJsonManager objectJsonManager;
    private Context context;

    //--------------------------Constructor-------------------------------//
    public HTTP_JSON_POST(Context context, OrionJsonManager object, String address, int rssi){

        Intent intent = new Intent(Constants.SERVICE_WAIT_RESPONSE);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);

        this.address=address;
        this.context=context;

        this.gender=object.JsonGender;

        this.objectJsonManager=object;
        this.body=objectJsonManager.getStringJson();
        this.rssi=rssi;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultUrl=context.getApplicationContext().getResources().getString(R.string.default_import_editText);
        this.stringUrl = sharedPrefs.getString(Constants.SERVER, defaultUrl);
        //Http petition to get information from server
        stringUrl="http://"+stringUrl+gender.query;
        Log.i("-----HTTP_JSON_POST url:----",stringUrl+" , "+defaultUrl);
        try{
            url=new URL(stringUrl);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
    }
    public HTTP_JSON_POST(Context context, OrionJsonManager object,TextView data_validation){
        this.context=context;
        this.gender=object.JsonGender;
        this.objectJsonManager=object;
        this.body=objectJsonManager.getStringJson();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.stringUrl = sharedPrefs.getString(Constants.SERVER, "@string/default_import_editText");
        this.data_validation=data_validation;
        //Http petition to create a new instance in Orion
        //query="/ngsi10/updateContext";
        stringUrl="http://"+stringUrl+gender.query;
        Log.i("-----HTTP_JSON_POST url:----",stringUrl);
        try{
            url=new URL(stringUrl);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }

    }
    //-------------------------Override-Methods-------------------------------//
    @Override
    protected String doInBackground(String... params) {
        String result="0";
        try{
            DefaultHttpClient client=new DefaultHttpClient();
            HttpPost httpPostFinal=new HttpPost(url.toURI());
            //Build the header
            Header[] headers=new Header[2];
            headers[0]=new BasicHeader("Content-Type","application/json");
            headers[1]=new BasicHeader("Accept","application/json");
            httpPostFinal.setHeaders(headers);
            //Build the body
            httpPostFinal.setEntity(new StringEntity(body));
            //Log.i("HTTP_JSON_POST body:----",body);
            //execute and get response
            HttpResponse response=client.execute(httpPostFinal);
            result=inputStreamToString(response.getEntity().getContent());
            if(gender.index >0) {
                this.onPostExecute(result);
            }
        }catch(HttpHostConnectException e){
            error=e.getMessage();
        }catch(URISyntaxException e){
            error=e.getMessage();

        } catch (UnsupportedEncodingException e) {

            error=e.getMessage();
        } catch (ClientProtocolException e) {

            error=e.getMessage();
        } catch (IOException e) {

            error=e.getMessage();
        }catch(Exception e){

            error=e.getMessage();
        }finally {
  //          Log.e("doInBackground",error.toString());
           return result;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.i("-------------HTTP_JSON_POST:--------------","Do onPostExecute");
        super.onPostExecute(s);
        //Log.i("HTTP_JSON_POST onPostExecute",s);
        String message=" ";
        //If all go well, save the device in the Database
        if(s!="0"){
            switch(gender.index) {
                case 0:
                //obtain values from object OrionJsonManager
                String address = objectJsonManager.getId();
                String mlatitude = objectJsonManager.getLatitude();
                String mlongitude = objectJsonManager.getLongitude();
                String name = objectJsonManager.getDeviceName();
                message= objectJsonManager.getMessage();
                String rssi = objectJsonManager.getCoverageAlert();
                String project_name = objectJsonManager.getProjectName();
                String text1=" ";
                String text2=" ";
                String text3=" ";
             try {
                        //Work with Database
                        projectDAO = new ProjectDAO(context);
                        projectDAO.open();
                        projectaux = projectDAO.getProjectByName(project_name);
                        projectDAO.close();
                        mDevice =new Device(projectaux.get_id(), address, mlatitude, mlongitude, name, message, rssi);
                        deviceDAO = new DeviceDAO(context);
                        deviceDAO.open();
                        int device_id = deviceDAO.create(mDevice);
                        deviceDAO.close();

                mDevice.set_id(device_id);
                if (device_id == -1) {
                    text1=context.getString(R.string.saved_error);
                    text2=context.getString(R.string.with_text);
                    text3=context.getString(R.string.saved_not_local);
                } else {
                    text1=context.getString(R.string.saved);
                    text2=context.getString(R.string.with_text);
                    text3=context.getString(R.string.saved_localandremote);
                         }
              }catch(SQLiteConstraintException e){
                 text1=context.getString(R.string.saved_error);
                 text2=context.getString(R.string.with_text);
                 text3=context.getString(R.string.saved_conflict_in_the_project);
              }finally{
                  data_validation.setText(text1 + address + text2 + message + text3);
               }
                    break;
                case 1:
                    try {
                        JSONObject json = new JSONObject(s);
                        if(json.has("errorCode")){
                            //find text in database
                          message=getFromDatabase(s);
                        }else {
                            Log.i("JSON", json.toString());
                            message = objectJsonManager.getMessageFromStringJson(json.toString());
                            if (existDevice()) {
                                //update text in database
                                this.updateMessage(message);
                            } else {
                                //Create Device
                                //--------1 create mdevice------------
                                double latitude = 0;
                                double longitude = 0;
                                GPSservice gps = new GPSservice(context);
                                // check if GPS enabled
                                if (gps.canGetLocation()) {
                                    latitude = gps.getLatitude();
                                    longitude = gps.getLongitude();
                                   // Log.d("--LocationOk---", "---");
                                    mlatitude = String.valueOf(latitude);
                                    mlongitude = String.valueOf(longitude);
                                }
                                mDevice = new Device(-1, this.address, String.valueOf(latitude),String.valueOf(longitude), "Anonimous", message, "-78");
                                //--------------Fin create mdevice---------//
                                this.updateProject();
                            }
                        }
                        }catch(JSONException e){
                        //Log.i("-----------ERROR Convirtiendo a JSON-------------",s);
                        message=getFromDatabase(s);
                        //e.printStackTrace();
                    }finally {
                        sendtoSpeechBluService(message);
                    }
                    break;
                case 2:
                    String answerd=" ";
                    try {
                        JSONObject json = new JSONObject(s);
                          answerd= json.toString();
                        //Log.i("case 2",s);
                        if(json.has("errorCode")){
                            //find text in database
                            message=getFromDatabase(s);
                            if(message==null){message=" ";}
                            //Log.e("Case2-----------Json.has ERROR code-------------",message);
                        }else{
                            //Log.i(" case 2 JSON",json.toString());
                            mDevice=objectJsonManager.getDeviceFromStringJson(answerd);
                            message=mDevice.getDeviceSpecification();
                            String coverage=mDevice.getMaxRSSI();
                            coberageAlert=(int)Integer.valueOf(coverage);
                            project_id=mDevice.getprojecto_id();
                            //Log.i("Case2-----------Wait update database-----------project_id: ",String.valueOf(project_id));
                            if(existDevice(project_id)){
                                //update text in database
                                this.device_id=this.updateDevice();
                            }else{
                                //Log.i("Case2---------Np exist Device--: ","Update Project");
                                this.updateProject();
                            }

                            //Log.i("Case2-----------FIN update database-----------device_id: ",String.valueOf(this.device_id));

                        }
                    }catch(JSONException e){
                        //Log.e("-----------ERROR Convirtiendo a JSON-------------",s);
                        message=getFromDatabase(s);
                        //e.printStackTrace();
                    }catch(NumberFormatException e){
                        //Log.i("HTTP_JSON_POST ","!!NUMBERFORMATEXCEPTION!!");
                        coberageAlert=-85;
                    }catch(Exception e) {
                        //Log.i("JSON exception case 2:","Excepcion no controlada");
                        e.printStackTrace();
                        message=" ";
                    }finally {
                        if(answerd.indexOf("No context element found")!=-1){
                            message="Not found";
                        }
                         sendtoSpeechBluService(message);
                    }
                    break;

            }//fin switch
        }else{
            String text=context.getString(R.string.error_Server);
            if(gender.index==0){
            data_validation.setText(text+error);}else{
                Log.e(text,error);
            }
//            Intent intent2 = new Intent(Constants.SERVICE_UNKNOWN_STATE);
//            LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent2);
        }
    }
//-------------------Mi-Methods-------------------------------//

    public Device getmDevice() {
        return mDevice;
    }

    private String inputStreamToString(InputStream is) throws IOException {
        String line;
        StringBuilder total=new StringBuilder();
        //Wrap a BufferedReader around the InputStream
        BufferedReader rd= new BufferedReader (new InputStreamReader(is));
        //Read response until the end
       while((line=rd.readLine())!=null){
           total.append(line);
       }
        //Return full string
        return total.toString();
    }
    //return the text associate to a device address
    private String getFromDatabase(String s){
        String text=" ";
        try {
            deviceDAO = new DeviceDAO(context);
            deviceDAO.open();
            mDevice = deviceDAO.getDeviceByAddress(this.address);
            text = mDevice.getDeviceSpecification();
        }catch(Exception e) {
            //Log.i("HTTP_JSON_POST:",s+"ERROR CODE Database "+e.getMessage());
            text = " ";
        }finally {
            deviceDAO.close();
        }
        return text;
    }
    //
    private void updateProject(){
        int result=-1;
        Device deviceaux;
        Project mprojectaux;
        //update text in database
        try {
            deviceDAO = new DeviceDAO(context);
            deviceDAO.open();
            projectDAO=new ProjectDAO(context);
            projectDAO.open();
            //is Device in my Database?
            if(project_id==-1){
                deviceaux = deviceDAO.getDeviceByAddress(this.address);
                mDevice.set_id(deviceaux.get_id());
                mDevice.setprojecto_id(deviceaux.getprojecto_id());
               // Log.i("HTTP_JSON_POST","get device by addres, id proyect: " +String. valueOf(mDevice.getprojecto_id())+", id device="+String. valueOf(mDevice.get_id()));
            }else{
                deviceaux=deviceDAO.getDeviceByAddressAndProject(this.address,mDevice.getprojecto_id());
                mDevice.set_id(deviceaux.get_id());
                mDevice.setprojecto_id(deviceaux.getprojecto_id());
                //Log.i("HTTP_JSON_POST","get device by addres, id proyect: " +String. valueOf(mDevice.getprojecto_id())+", id device="+String. valueOf(mDevice.get_id()));
            }


            if (mDevice.get_id() != -1) {
                this.device_id=mDevice.get_id();
                this.project_id=mDevice.getprojecto_id();
                //The device exists
                deviceDAO.update(mDevice);
                //Log.i("JSON:","DEVICE UPDATE "+this.address);
            }else {

                if(mDevice.getprojecto_id()==-1){
                    //Log.i("HTTP_JSON_POST","project_id==-1");
                        //get Default project
                        mprojectaux=projectDAO.getProjectByName("Default");
                        mDevice.setprojecto_id(mprojectaux.get_id());
                        //Log.i("HTTP_JSON_POST","--Get Default..project_id : "+String.valueOf(mDevice.getprojecto_id()));
                        if(mDevice.getprojecto_id()==-1){
                            //Create new project
                           // Log.i("HTTP_JSON_POST","--Creating Default project.. : ");
                            mprojectaux=new Project("Default","0034667442487");
                            this.project_id=projectDAO.create(mprojectaux);
                            mDevice.setprojecto_id(this.project_id);
                            //Log.i("HTTP_JSON_POST","--Created Default project..project_id : "+String.valueOf(mDevice.getprojecto_id()));
                        }

                    }else{
                 deviceaux = deviceDAO.getDeviceByAddressAndProject(this.address, mDevice.getprojecto_id());
                 mDevice.set_id(deviceaux.get_id());
               // Log.i("HTTP_JSON_POST","--Is device in project? id_device:"+String.valueOf(mDevice.get_id())+" project_id"+String.valueOf(mDevice.getprojecto_id()));
                }

                if (mDevice.get_id()!= -1) {
                            this.device_id=mDevice.get_id();
                            //Log.i("HTTP_JSON_POST","--Device exits in project, device id:  "+String.valueOf(this.device_id));
                            deviceDAO.update(mDevice);
                            //Log.i("HTTP_JSON_POST","--Device undated--");
                        }else {
                        deviceaux = deviceDAO.getDeviceByAddress(this.address);
                        project_id=deviceaux.getprojecto_id();
                        device_id=deviceaux.get_id();
                        //Log.i("HTTP_JSON_POST","--Exit device--:"+String.valueOf(this.device_id)+" project_id"+String.valueOf(this.project_id));
                        mDevice.setprojecto_id(project_id);
                        mDevice.set_id(device_id);
                    if(this.device_id==-1){
                        //Log.i("HTTP_JSON_POST","--Device exits, device id :  "+String.valueOf(this.device_id));
                        int project_id=mDevice.getprojecto_id();

                          Device nAux=deviceDAO.getDeviceByAddress(mDevice.getmDeviceAddress());
                            if(nAux.get_id()!=-1) {
                                mDevice.set_id(nAux.get_id());
                                if(project_id==-1){mDevice.setprojecto_id(nAux.getprojecto_id());}
                                deviceDAO.update(mDevice);
                            }else{
                                if(project_id==-1){mDevice.setprojecto_id(0);}
                                this.device_id = deviceDAO.create(mDevice);

                                mDevice.set_id(device_id);
                            }

                        //Log.i("HTTP_JSON_POST","--Device undated--");
                    }else{
                        deviceDAO.update(mDevice);
                            //Log.i("HTTP_JSON_POST:", "NEW DEVICE SAVE with id :" + String.valueOf(this.device_id) + "address" + this.address + " " + message + ", in Project:" + projectaux.getmprojectName());
                        }}
            }

        }catch(SQLiteConstraintException e){
            Device nAux=deviceDAO.getDeviceByAddress(mDevice.getmDeviceAddress());
            deviceDAO.delete(nAux.get_id());

        }catch(CursorIndexOutOfBoundsException e){
            //Log.i("HTTP_JSON_POST:","CursorIndexOfBoundException device_id=-1");
             this.device_id=-1;
        }finally {
            projectDAO.close();
            deviceDAO.close();
            //Log.i("HTTP_JSON_POST UPDATE_PROJECT","set id= "+this.device_id);

        }

    }

    private boolean existDevice(){
        Device deviceaux;
        boolean exist=false;
        deviceDAO = new DeviceDAO(context);
        deviceDAO.open();
        deviceaux = deviceDAO.getDeviceByAddress(this.address);
        project_id=deviceaux.getprojecto_id();
        device_id=deviceaux.get_id();
        if (device_id!= -1) {
            exist=true;
           // Log.i("EXISTDevice-true----------device_id: ",String.valueOf(this.device_id));
        }
        deviceDAO.close();
        return exist;
    }
    private boolean existDevice(int id_project){
        Device mdeviceaux;
        boolean exist=false;
        deviceDAO = new DeviceDAO(context);
        deviceDAO.open();
        mdeviceaux = deviceDAO.getDeviceByAddressAndProject(this.address, id_project);
        project_id=mdeviceaux.getprojecto_id();
        device_id=mdeviceaux.get_id();
        if (device_id!= -1) {
            exist=true;
            //Log.i("EXISTDevice-true----------device_id: ",String.valueOf(this.device_id));
        }
        deviceDAO.close();
        return exist;
    }
    private int updateMessage(String message){
        int result=-1;
        deviceDAO = new DeviceDAO(context);
        deviceDAO.open();
        mDevice = deviceDAO.getDeviceByAddress(this.address);
        mDevice.setDeviceSpecification(message);
        project_id=mDevice.getprojecto_id();
        deviceDAO.update(mDevice);
        deviceDAO.close();
        return mDevice.get_id();
    }
    private int updateDevice(){
        //Log.i("HTTP JSON POST UPDATE DEVICE",this.address);
        Device deviceaux;

        int result=-1;
        deviceDAO = new DeviceDAO(context);
        deviceDAO.open();
        deviceaux = deviceDAO.getDeviceByAddress(this.address);
        int device_id=deviceaux.get_id();
        //Log.i("UPDATEDEVICE","ser id= "+device_id);
        mDevice.set_id(device_id);
        deviceDAO.update(mDevice);
        deviceDAO.close();
        //Log.i("HTTP_JSON_POST","updateDevice id= "+device_id);
        return device_id;
    }
    private void sendtoSpeechBluService(String message){
        //Log.i("HTTP_JSON_POST /\n SENDtoSpeechBluService , ¿es? rssi>=coberageAlert -->                    ", this.rssi+" >"+this.coberageAlert);
        double valanz=this.coberageAlert-2;
        if(message.equals("Not found") && !this.address.equals("0")){
            Intent intent = new Intent(Constants.BLACKDEVICE);
            intent.putExtra("address", this.address);
            LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);

        }else
        if(this.rssi>=valanz){
        int device_id=mDevice.get_id();
//        if(message==null){message=" ";}
//        Intent intent = new Intent(Constants.DEVICE_MESSAGE);
//         intent.putExtra("message", message);
//        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
//       Log.i("-----------INTENT Was SEND-------------",message);
                //Log.i("HTTP_JSON_POST ","deviceid !=-1 ");
                Intent intent = new Intent(Constants.DEVICE);
                this.address = mDevice.getmDeviceAddress();
                this.message= mDevice.getDeviceSpecification();
                intent.putExtra("id",device_id);
                intent.putExtra("rssi",rssi);
                intent.putExtra("coberageAlert",coberageAlert);
                intent.putExtra("message",this.message);
                intent.putExtra("address", this.address);
                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
                //Log.i("-----------INTENT DEVICE Was SEND-------------", String.valueOf(mDevice.get_id()));
        }else{
       Intent intent2 = new Intent(Constants.SERVICE_UNKNOWN_STATE);
       LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent2);}

    }

}

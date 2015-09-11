package mesas.martinez.leonor.iBoBlind.model;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import mesas.martinez.leonor.iBoBlind.comunication.HTTP_JSON_POST;

/**
 * Created by paco on 22/07/2015.
 */
public class OrionJsonManager {
    private String type;
    private String id;
    private String latitude;
    private String longitude;
    private String message;
    private String coverageAlert;
    private String projectName;
    private String installerDNIorNIF;
    private String date;
    private String deviceName;
    private int project_id;
    private Context context;
    public HTTP_JSON_POST.Gender JsonGender;
   // private JSONObject json;
   private String json;
    //Default Constructor
    public OrionJsonManager(){
        type="Anonymous";
        id="-1";
        latitude="0";
        longitude="0";
        message=" ";
        coverageAlert="0";
        projectName="Anonymous";
        installerDNIorNIF="Anonymous";
        date="00000000";
        deviceName="Anonymous";
        project_id=-1;
    }
  //Constructor to query message from id
    public String SetJSONtoGetMessage(String type, String id){
        this.type = type;
        this.id = id;
        this.JsonGender=HTTP_JSON_POST.Gender.GET_MESSAGE;
        json="{  \n" +
                "\"entities\": [\n" +
                "  {\n" +
                "    \"type\": \"" + type + "\",\n" +
                "    \"isPattern\": \"false\",\n" +
                "    \"id\": \"" + id + "\"\n" +
                "  }\n" +
                "  ],\n" +
                "    \"attributes\": [\n" +
                "     \"message\"\n" +
                "    ]\n" +
                " }\n" ;
        return json;
    };

    //Constructor to query message from id
    public String SetJSONtoGetAttributes(String type, String id,Context context){
        this.type = type;
        this.id = id;
        this.JsonGender=HTTP_JSON_POST.Gender.GET;
        this.context=context;
        json="{  \n" +
                "\"entities\": [\n" +
                "  {\n" +
                "    \"type\": \"" + type + "\",\n" +
                "    \"isPattern\": \"false\",\n" +
                "    \"id\": \"" + id + "\"\n" +
                "  }\n" +
                "  ],\n" +
                "    \"attributes\": [ ]\n" +
                " }\n" ;
        return json;
    };
//Constructor to create entity
    public String SetJSONtoCreateEntity(String type, String id, String latitude, String longitude, String message, String coverageAlert, String installerDNIorNIF, String projectName, String deviceName) {
        this.type = type;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.message = message;
        this.coverageAlert = coverageAlert;
        this.projectName = projectName;
        this.installerDNIorNIF = installerDNIorNIF;
        this.deviceName=deviceName;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
        Date d=new Date();
        this.date = sdf.format(d);
        String shortDate=s.format(d);
        this.JsonGender=HTTP_JSON_POST.Gender.UPDATE_CREATE;
               json="{  \n" +
                    "\"contextElements\": [\n" +
                    "  {\n" +
                    "    \"type\": \"" + type + "\",\n" +
                    "    \"isPattern\": \"false\",\n" +
                    "    \"id\": \"" + id + "\",\n" +
                    "    \"attributes\": [\n" +
                    "    {\n" +
                    "      \"name\": \"position\",\n" +
                    "      \"type\": \"coords\",\n" +
                    "      \"value\": \""+latitude+","+longitude+"\",\n" +
                    "      \"metadatas\": [\n" +
                    "      {\n" +
                    "        \"name\": \"location\",\n" +
                    "        \"type\": \"string\",\n" +
                    "        \"value\": \"WGS84\"\n" +
                    "      }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"message\",\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \""+message+"\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"coberageAlert \",\n" +
                    "      \"type\": \"dBm\",\n" +
                    "      \"value\": \""+coverageAlert+"\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"LastUpdate\",\n" +
                    "      \"type\": \"date\",\n" +
                    "      \"value\": \""+date+"\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \""+projectName+"\",\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \"ProjectName\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \""+installerDNIorNIF+"\",\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \"InstallerDNIorNIF\"\n" +
                    "    }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "  ],\n" +
                    "  \"updateAction\": \"APPEND\"\n" +
                    " }\n" ;
        return json;
   }

    public String getStringJson() {
        return json;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getMessage() {
        return message;
    }

    public String getCoverageAlert() {
        return coverageAlert;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getInstallerDNIorNIF() {
        return installerDNIorNIF;
    }

    public String getDate() {
        return date;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    //You can use this method only if you has used  SetJSONtoGetMessage
    public String getMessageFromStringJson(String answer){
        String message="\n";
        JSONObject reader;
        JSONObject contextElement;
        JSONObject JsonMessage;
        JSONObject JsoncontextResponses;
        JSONArray contextResponses;
        JSONArray attributes;
        try {
            reader= new JSONObject(answer);
       if(reader.has("errorCode")){
           Log.i("Received Server Error",answer);
           return "-1";
       }else{
           contextResponses=reader.getJSONArray("contextResponses");
           message=contextResponses.getString(0);
           //Log.i("JSON ARRAY 0 ",message);
           JsoncontextResponses= new JSONObject(message);
           contextElement=JsoncontextResponses.getJSONObject("contextElement");
           attributes=contextElement.getJSONArray("attributes");
           message=attributes.getString(0);
           //Log.i("JSON ARRAY 1 ",message);
           JsonMessage= new JSONObject(message);
           message=JsonMessage.getString("value");
           Log.i("JSON ARRAY 2 ",message);
       return message;}
        } catch (JSONException e) {
            e.printStackTrace();
            return "-1";
        }
    }
    //You can use this method only if you has used  SetJSONtoGetAttributes
    public Device getDeviceFromStringJson(String answer){
        String message="\n";
        JSONObject reader;
        JSONObject contextElement;
        JSONObject JsoncontextResponses;
        JSONArray contextResponses;
        JSONArray attributes;
        Device device=new Device();
        device.set_id(-1);
        try {
            reader= new JSONObject(answer);
            if(reader.has("errorCode")){
                Log.i("Received Server Error",answer);
            }else{
                contextResponses=reader.getJSONArray("contextResponses");
                message=contextResponses.getString(0);
                Log.i("OrionJsonManager_getDeviceFromStringJSON",message);
                JsoncontextResponses= new JSONObject(message);
                contextElement=JsoncontextResponses.getJSONObject("contextElement");
                attributes=contextElement.getJSONArray("attributes");
                device=getDeviceFromJsonArray(attributes);
            }
        } catch (JSONException e) {
            Log.i("Can¨t combert response to JSOM, response:",answer);
            //e.printStackTrace();
        }finally {
            return device;
        }
    }
    public Device getDeviceFromJsonArray(JSONArray array) throws JSONException {
        Log.i("OrionJsonManager_getDeviceFromStringArray","--");
        int length= array.length();
        this.message=" ";
        for(int i=0; i<length; i++){
            //Log.i("OrionJsonManager_getDeviceFromStringArray",length+" i"+i);
            String m=array.getString(i);
            //Log.i("OrionJsonManager_getDeviceFromStringArray",length+" message"+message);
            String[] r=m.split(",");
            String[] aux;
            String aux2;
            String ms=r[1];
           // Log.i("OrionJsonManager_getDeviceFromStringArray",length+" switch"+ms);
            switch (ms){
                case "\"type\":\"text\"":
                    switch(r[0]){
                        case "{\"value\":\"InstallerDNIorNIF\"":
                            aux=r[2].split(":");
                            aux2=aux[1].substring(0,aux[1].length()-1);
                            aux2=aux2.replace("\"","");
                            this.installerDNIorNIF=aux2;
                            Log.i("---[ "+i+" , type: InstallerDNIorNIF]----",this.installerDNIorNIF);
                            break;
                        case "{\"value\":\"ProjectName\"":
                            aux=r[2].split(":");
                            aux2=aux[1].substring(0,aux[1].length()-1);
                            aux2=aux2.replace("\"","");
                            this.projectName=aux2;
                            Log.i("---[ "+i+" , type: ProjectName]----",aux2);
                            ProjectDAO projectDAO=new ProjectDAO(context);
                            projectDAO.open();
                            Project projectaux=projectDAO.getProjectByName(projectName);
                            projectDAO.close();
                            this.project_id=projectaux.get_id();

                            break;
                        default:

                            Log.i("---[r2]----",r[2]);
                            if(r[2].equals("\"name\":\"message\"}")){
                            //message
                            aux=r[0].split(":");
                            String aux1=aux[1].replace("\"","");
                            this.message=aux1;
                            Log.i("---[ "+i+" , type: message]----",this.message);
                            }
                            break;
                    }

                    break;
                case "\"type\":\"date\"":
                    aux=r[0].split(":");
                    String aux1=aux[1].replace("\"","");
                    this.date=aux1;
                    Log.i("---[ "+i+" , type: date]----",aux[1]);
                    break;
                case "\"type\":\"dBm\"":
                    aux=r[0].split(":");
                    String aux0r=aux[1].replace("\"","");
                    this.coverageAlert=aux0r;
                   Log.i("---[ "+i+" , type: dBm]----",this.coverageAlert);
                    break;
                default:
                    //coor case
                    aux=r[0].split(":");
                    String aux1r=aux[1].replace("\"","");
                    this.latitude=aux1r;
                    String r1=r[1].replace("\"","");
                    this.longitude=r1;
                     Log.i("---[ "+i+" , type: coor]----",this.latitude+","+this.longitude);
                    break;
            }
//            for(int j=0; j<r.length; j++){
//                Log.i("---[ "+i+" , "+j+" ]----",r[j]);
//            }
        }
        Device device=new Device(this.project_id, this.id, this.latitude, this.longitude,this.deviceName, this.message, this.coverageAlert);
        Log.i("OrionJsonManager:--DEVICE--/\n ",device.toString()+"/\n");
        return device;
    }
}

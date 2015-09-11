package mesas.martinez.leonor.iBoBlind.comunication;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import mesas.martinez.leonor.iBoBlind.model.Constants;

/**
 * author :Leonor Martinez Mesas
 * This class implement in a separate handler thread.
 * The Orion communication
 */
public class GETPOSTIntentService extends IntentService {
    //atributes
    URL url;
    String getResponse;
    String postResponse;
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET = "com.presencecontrol.m2m.m2m_presencecontrol.action.GET";
    private static final String ACTION_POSTHEADER = "com.presencecontrol.m2m.m2m_presencecontrol.action.POSTHEADER";
    private static final String ACTION_POST = "com.presencecontrol.m2m.m2m_presencecontrol.action.POST";
    // parameters
    private static final String EXTRA_URL = "com.presencecontrol.m2m.m2m_presencecontrol.extra.url";
    private static final String EXTRA_BODY = "com.presencecontrol.m2m.m2m_presencecontrol.extra.PARAM2";
    /**
     * Starts this service to perform action POST with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
   public static void startActionPOST(Context context, String stringUrl, String body) {
       Intent intent = new Intent(context, GETPOSTIntentService.class);
       intent.setAction(ACTION_POST);
       intent.putExtra(EXTRA_URL, stringUrl);
       intent.putExtra(EXTRA_BODY, body);
        context.startService(intent);
    }



    /**
     * Starts this service to perform action GET with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    public static void startActionGET(Context context, String stringUrl) {
        Intent intent = new Intent(context, GETPOSTIntentService.class);
        intent.setAction(ACTION_GET);
        intent.putExtra(EXTRA_URL, stringUrl);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action POSTHEADER with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    // TODO: Customize helper method
    public static void startActionPOSTHEADER(Context context, String stringUrl) {
        Intent intent = new Intent(context, GETPOSTIntentService.class);
        intent.setAction(ACTION_POSTHEADER);
        intent.putExtra(EXTRA_URL, stringUrl);
        context.startService(intent);
    }

    public GETPOSTIntentService() {
        super("GETPOSTIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET.equals(action)) {
                final String stringUrl = intent.getStringExtra(EXTRA_URL);
                handleActionGET(stringUrl);
            } else if (ACTION_POSTHEADER.equals(action)) {
                final String stringUrl = intent.getStringExtra(EXTRA_URL);
                handleActionPOSTHEADER(stringUrl);
            }
        }
    }

    /**
     * Handle action GET execute in background thread a get petition with the provided
     * url.
     */
    private void handleActionGET(String stringUrl) {
        Log.d(" handleActionGET:", "--" + stringUrl + "--");
        try {
            url = new URL(stringUrl);
            URLConnection urlConnection = url.openConnection();
            InputStream in0 = urlConnection.getInputStream();
            Log.d("-getInputStreamOk-", "--");
            InputStreamReader in = new InputStreamReader(in0);
            BufferedReader rd = new BufferedReader(in);
            String line;
            StringBuilder total = new StringBuilder();
            while ((line = rd.readLine()) != null){
                    total.append(line);
            }
            getResponse= total.toString();
            Log.d("--RESPUESTA--", "--" + getResponse + "-");
                              /*
     * Creates a new Intent containing a Uri object
     * GETINTENTSERVICE_BROADCAST is a custom Intent action
     * with the response of the get petition
     */
            Intent localIntent =new Intent(Constants.INTENTSERVICE_BROADCAST_GET)
                            // Puts the status into the Intent
                      .putExtra(Constants.INTENTSERVICE_EXTRA, getResponse);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            Log.d("-Error IOException", e.getMessage());
        }
           }
    /**
     * Handle action POST without body in the provided background thread with the provided
     * Url.
     */
    private void handleActionPOSTHEADER(String stringUrl) {
        Log.d("handleActionPOSTHEADER:", "--" + stringUrl + "--");
        try {
            url = new URL(stringUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);
            InputStream in0 = urlConnection.getInputStream();
            Log.d("-getInputStreamOk-", "--");
            InputStreamReader in = new InputStreamReader(in0);
            BufferedReader rd = new BufferedReader(in);
            String line;
            StringBuilder total = new StringBuilder();
            try {
                while ((line = rd.readLine()) != null)
                    total.append(line);            } catch (IOException e) {
                e.printStackTrace();
            }
            postResponse = total.toString();
            Log.d("--RESULTADO--", "--" + postResponse + "-");
    /*
     * Creates a new Intent containing a Uri object
     * POSTHEADERINTENTSERVICE_BROADCAST is a custom Intent action
     * with the response of the post petition
     */
            Intent localIntent =new Intent(Constants.INTENTSERVICE_BROADCAST_POSTHEADER)
            // Puts the status into the Intent
                   .putExtra(Constants.INTENTSERVICE_EXTRA,  postResponse);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            Log.d("-Error IOException", e.getMessage());
        }
    }
}

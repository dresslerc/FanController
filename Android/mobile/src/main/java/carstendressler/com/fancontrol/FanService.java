package carstendressler.com.fancontrol;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import carstendressler.com.util.SimpleHttpClient;
import carstendressler.com.util.SparkResponse;

/**
 * Created by Carsten on 7/27/2014.
 */
public class FanService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        //  Needed for communication between watch and device.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        //Log.d(TAG, "onConnected: " + connectionHint);
                        //tellWatchConnectedState("connected");
                        //  "onConnected: null" is normal.
                        //  There's nothing in our bundle.
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        //Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        //Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
        String path = messageEvent.getPath();

        if (path.equals("/command")) {
            String DeviceName = dataMap.getString("Device");
            String DeviceMode = dataMap.getString("Mode");

            FanEntity fan = findFanEntity(SettingsActivity.SettingsFragment.getConfiguredFans(this),DeviceName);
            SendCommand(String.valueOf(fan.Address),DeviceMode);
        }
    }

    private FanEntity findFanEntity(List<FanEntity> devices, String Name) {
        for (FanEntity device : devices) {
            if (device.Name.toLowerCase().contains(Name.toLowerCase())) {
                return device;
            }
        }
        return null;
    }

    private void SendCommand(final String Address, final String Mode) {

        final Context context = getApplicationContext();
        final String deviceID = SettingsActivity.SettingsFragment.getDeviceID(this);

        new Thread(new Runnable() {
            public void run() {

                Gson gson = new Gson();
                String buffer = null;

                try {
                    buffer = SimpleHttpClient.httpPost("https://api.spark.io/v1/devices/" + deviceID + "/fancontrol", "access_token=" + SettingsActivity.SettingsFragment.getAccessToken(context) + "&args=" + Address + Mode);

                    SparkResponse resp = gson.fromJson(buffer, SparkResponse.class);



                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();

    }
}

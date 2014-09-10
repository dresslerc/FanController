package carstendressler.com.fancontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import carstendressler.com.util.HTTPHeader;
import carstendressler.com.util.SimpleHttpClient;
import carstendressler.com.util.SparkResponse;
import io.spark.core.cloud.AuthenicationResponse;

/**
 * Created by Carsten on 7/25/2014.
 */
public class MainActivity extends ListActivity {

    private FanAdapter _adapter;
    private List<FanEntity> _fans = null; //new ArrayList<FanEntity>();
    private Context _context;
    private Activity _activity;

    //public String _accessToken = "9f144d072f915ab729d60e724282d36ce44bc310";
    //public String _deviceId = "48ff70065067555030231387";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        Log.v("FANCTRL", "appcreated");
        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        _context = this;
        _activity = this;

        _fans =  SettingsActivity.SettingsFragment.getConfiguredFans(_context);

        _adapter = new FanAdapter(this, R.layout.listitem, _fans);
        this.setListAdapter(_adapter);

        _adapter.notifyDataSetChanged();

        getStatus();

    }

    @Override
    protected void onResume() {
        super.onResume();

        _fans.clear();
        _fans.addAll(SettingsActivity.SettingsFragment.getConfiguredFans(_context));

        _adapter.notifyDataSetChanged();

        getStatus();
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position,
                                   long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("Set Fan Mode");
        builder.setItems(R.array.mode_array,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // _fans.get(position).Name
                        String mode = null;

                        if (which == 0)
                            mode = "hi";

                        if (which == 1)
                            mode = "me";

                        if (which == 2)
                            mode = "lo";

                        if (which == 3)
                            mode = "of";

                        SendCommand(
                                String.valueOf(_fans.get(position).Address),
                                mode);

                        // Toast.makeText(_context,
                        // String.valueOf(_fans.get(position).Address),
                        // Toast.LENGTH_LONG).show();

                    }
                });
        builder.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:

                Intent SettingsIntent = new Intent(_context, SettingsActivity.class);
                this.startActivity(SettingsIntent);
                break;

            case R.id.action_refresh:

                getStatus();

                break;

        }

        return super.onMenuItemSelected(featureId, item);
    }



    private void SendCommand(final String Address, final String Mode) {

        final String deviceID = SettingsActivity.SettingsFragment.getDeviceID(_context);

        Toast.makeText(_context, Address + Mode, Toast.LENGTH_LONG).show();

        new Thread(new Runnable() {
            public void run() {

                Gson gson = new Gson();
                String buffer = null;

                try {
                    buffer = SimpleHttpClient.httpPost("https://api.spark.io/v1/devices/" + deviceID + "/fancontrol","access_token=" + SettingsActivity.SettingsFragment.getAccessToken(_context) + "&args=" + Address + Mode);

                    SparkResponse resp = gson.fromJson(buffer, SparkResponse.class);

                    int ArrayAddress = 0;
                    for (int i = 0; i < _fans.size(); i++) {
                        if (_fans.get(i).Address == Integer.valueOf(Address))
                            ArrayAddress = i;
                    }

                    if (resp.return_value == -1)
                        _fans.get(ArrayAddress).Status = FanEntity.Status1.UNKNOWN;

                    if (resp.return_value == 0)
                        _fans.get(ArrayAddress).Status = FanEntity.Status1.OFF;

                    if (resp.return_value == 1)
                        _fans.get(ArrayAddress).Status = FanEntity.Status1.LOW;

                    if (resp.return_value == 2)
                        _fans.get(ArrayAddress).Status = FanEntity.Status1.MEDIUM;

                    if (resp.return_value == 3)
                        _fans.get(ArrayAddress).Status = FanEntity.Status1.HIGH;

                    // Runs next code in UI Thread
                    _activity.runOnUiThread(new Runnable() {
                        public void run() {
                            _adapter.notifyDataSetChanged();

                        }
                    });

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private void getStatus() {

        final String deviceID = SettingsActivity.SettingsFragment.getDeviceID(_context);

        if (deviceID.length() == 0)
            return;

        setProgressBarIndeterminateVisibility(Boolean.TRUE);

        new Thread(new Runnable() {
            public void run() {

                Gson gson = new Gson();
                String buffer = null;

                for (int i = 0; i < _fans.size(); i++) {

                    buffer = null;

                    try {
                        String accessToken = SettingsActivity.SettingsFragment.getAccessToken(_context);

                        if (accessToken.length() == 0) {
                            // Runs next code in UI Thread
                            _activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                }
                            });

                            return;

                        }


                        buffer = SimpleHttpClient.httpPost("https://api.spark.io/v1/devices/" + deviceID + "/fanstatus" ,"access_token=" + accessToken + "&args=" + String.valueOf(_fans.get(i).Address));
                        SparkResponse resp = gson.fromJson(buffer, SparkResponse.class);

                        if (resp.return_value == -1)
                            _fans.get(i).Status = FanEntity.Status1.UNKNOWN;

                        if (resp.return_value == 0)
                            _fans.get(i).Status = FanEntity.Status1.OFF;

                        if (resp.return_value == 1)
                            _fans.get(i).Status = FanEntity.Status1.LOW;

                        if (resp.return_value == 2)
                            _fans.get(i).Status = FanEntity.Status1.MEDIUM;

                        if (resp.return_value == 3)
                            _fans.get(i).Status = FanEntity.Status1.HIGH;

                        // Runs next code in UI Thread
                        _activity.runOnUiThread(new Runnable() {
                            public void run() {
                                _adapter.notifyDataSetChanged();
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            }
                        });

                    } catch (final IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                        final String errorMsg = buffer;

                        // Runs next code in UI Thread
                        _activity.runOnUiThread(new Runnable() {
                            public void run() {
                                _adapter.notifyDataSetChanged();
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                Toast.makeText(_context, e.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        });


                    }
                }
            }
        }).start();
    }

    private void getAccessToken(final String Email, final String Password) {

        setProgressBarIndeterminateVisibility(Boolean.TRUE);

        new Thread(new Runnable() {
            public void run() {

                Map<String, String> jsonMap = new HashMap<String, String>();

                jsonMap.put("username",Email);
                jsonMap.put("password", Password);

                String json = new Gson().toJson(jsonMap);

                List<HTTPHeader> headers = new ArrayList<HTTPHeader>();
                headers.add(new HTTPHeader("Content-Type","application/json"));

                String respBuffer = null;
                try {
                    respBuffer = SimpleHttpClient.httpPost("https://www.spark.io/sign-in", json, headers);
                } catch (IOException e) {
                }
                AuthenicationResponse authResp = new Gson().fromJson(respBuffer, AuthenicationResponse.class);

                headers.add(new HTTPHeader("Authorization","Bearer " + authResp.auth_token.token));
                try {
                    respBuffer = SimpleHttpClient.httpGet("https://www.spark.io/access-token", headers);
                } catch (IOException e) {
                }
                LinkedTreeMap accessTokenResp = new Gson().fromJson(respBuffer, LinkedTreeMap.class);


            }
        }).start();

    }

}

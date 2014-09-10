package carstendressler.com.fancontrol;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import io.spark.core.cloud.Client;
import io.spark.core.cloud.Device;

/**
 * Created by Carsten on 7/25/2014.
 */
public class SettingsActivity extends Activity {

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets Action Bar color
        ActionBar bar = getActionBar();

        // Replaces Fragment with settings fragment
        mContext = this;
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }


    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

        private static Context mContext;
        private List<FanEntity> fanList = null;
        private GoogleApiClient mGoogleApiClient;

        @Override
        public void onPause() {
            super.onPause();
            setConfiguredFans(fanList);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.add(0, 0, 0, "Add Fan").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            // Handles the "Add Fan" button in the Settings Menu

            LayoutInflater inflater = getActivity().getLayoutInflater();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(inflater.inflate(R.layout.dialog_fansetup, null))
                    // Add action buttons
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            final AlertDialog alertDialog = (AlertDialog) dialog;
                            final EditText fanName = (EditText) alertDialog.findViewById(R.id.etName);
                            final EditText fanAddress = (EditText) alertDialog.findViewById(R.id.etAddress);


                            FanEntity fan = new FanEntity();
                            fan.Name = fanName.getText().toString();
                            fan.Address = Integer.parseInt(fanAddress.getText().toString());

                            fanList.add(fan);

                            UpdateFanCategory(fanList);

                            alertDialog.dismiss();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    final AlertDialog alertDialog = (AlertDialog) dialog;
                    alertDialog.cancel();

                }
            });

            Dialog PasswordEntry = builder.create();
            PasswordEntry.setTitle("Add Fan");
            PasswordEntry.show();

            return super.onOptionsItemSelected(item);
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mContext = this.getActivity();
            addPreferencesFromResource(R.xml.preference);

            // Gets Fans from Settings and Updates UI (PreferenceCategory)
            fanList = getConfiguredFans(mContext);
            UpdateFanCategory(fanList);

            initDefaults();

            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Wearable.API)
                    .build();

            mGoogleApiClient.connect();





        }

        public void initDefaults() {

            PreferenceScreen prefSet = getPreferenceScreen();

            SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Handles click for "Spark Credentials" menu option
            Preference mclickListenerSparkAccount = (Preference) prefSet.findPreference("spark_credential");
            mclickListenerSparkAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    LayoutInflater inflater = getActivity().getLayoutInflater();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setView(inflater.inflate(R.layout.dialog_sparksignin, null))
                            // Add action buttons
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {

                                    final AlertDialog alertDialog = (AlertDialog) dialog;
                                    final EditText userName = (EditText) alertDialog.findViewById(R.id.username);
                                    final EditText password = (EditText) alertDialog.findViewById(R.id.password);

                                    setSparkEmail(userName.getText().toString());
                                    setSparkPassword(password.getText().toString());

                                    alertDialog.dismiss();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final AlertDialog alertDialog = (AlertDialog) dialog;
                            alertDialog.cancel();

                        }
                    });

                    Dialog PasswordEntry = builder.create();
                    PasswordEntry.setTitle("Spark Account");
                    PasswordEntry.show();

                    return false;
                }
            });

            // Handles click for "Spark Devices" menu option
            Preference mclickListener = (Preference) prefSet.findPreference("spark_device");
            mclickListener.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    getSparkDevices();
                    return false;
                }
            });

        }

        private void getSparkDevices() {

            final ProgressDialog progress = ProgressDialog.show(mContext, null, "Getting devices, Please wait... ", true);
            progress.setCancelable(true);

            Thread t = new Thread() {
                public void run() {
                    Client client = new Client();

                    // Gets Access Token
                    String AccessToken = client.getAccessToken(mContext, getSparkEmail(), getSparkSparkPassword());
                    setAccessToken(AccessToken);

                    // Gets Devices associated with the account
                    final List<Device> devices = client.GetDevices(mContext, AccessToken);

                    List<String> deviceNames = new ArrayList<String>();

                    for (Device dev : devices) {
                        deviceNames.add(dev.name);
                    }
                    final CharSequence[] items = deviceNames.toArray(new String[deviceNames.size()]);

                    Activity activity = (Activity) mContext;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Select Spark Device").setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    for (Device dev : devices) {
                                        if (dev.name.equals(items[which])) {
                                            Toast.makeText(mContext, dev.name + " " + dev.id, Toast.LENGTH_LONG).show();
                                            setDeviceID(dev.id);

                                            return;
                                        }
                                    }
                                }
                            });
                            progress.dismiss();
                            builder.show();
                        }
                    });
                }
            };
            t.start();
        }

        public static List<FanEntity> getConfiguredFans(Context context) {
            List<FanEntity> rtnList = new ArrayList<FanEntity>();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String buffer = prefs.getString("ConfiguredFans", "");
            String[] fans = buffer.split("~");

            if (buffer.length() == 0)
                return rtnList;

            for (String fan : fans) {

                if (fan.length() == 0)
                    continue;

                String[] splits = fan.split(":");

                FanEntity newFan = new FanEntity();
                newFan.Name = splits[0];
                newFan.Address = Integer.parseInt(splits[1]);

                rtnList.add(newFan);
            }

            return rtnList;
        }

        private void UpdateFanCategory(List<FanEntity> fans) {
            PreferenceCategory fansCategory = ((PreferenceCategory)findPreference("fans"));
            fansCategory.removeAll();

            int count = 0;

            for (FanEntity fan : fans) {
                Preference p = new Preference(mContext);
                p.setTitle(fan.Name);
                p.setSummary("Address " + String.valueOf(fan.Address));
                p.setKey("lfan" + count);
                p.setOnPreferenceClickListener(this);

                fansCategory.addPreference(p);
                count += 1;

            }

        }


        @Override
        public boolean onPreferenceClick(Preference preference) {

            if (preference.getKey().startsWith("lfan")) {

                final int location = Integer.parseInt(preference.getKey().replace("lfan", ""));

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Delete item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                fanList.remove(location);
                                UpdateFanCategory(fanList);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                builder.show();

            }


            return false;
        }

        public void setSparkEmail(String Email) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            prefs.edit().putString("SparkEmail", Email).apply();
        }

        public String getSparkEmail() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            return prefs.getString("SparkEmail", "");
        }

        public void setSparkPassword(String Email) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            prefs.edit().putString("SparkPassword", Email).apply();
        }

        public String getSparkSparkPassword() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            return prefs.getString("SparkPassword", "");
        }

        public void setAccessToken(String Token) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            prefs.edit().putString("AccessToken", Token).apply();
        }

        public static String getAccessToken(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString("AccessToken", "");
        }

        public void setDeviceID(String deviceID) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            prefs.edit().putString("DeviceID", deviceID).apply();
        }

        public static String getDeviceID(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString("DeviceID", "");
        }

        public void setConfiguredFans(List<FanEntity> fans) {

            String Pattern = "";

            for (FanEntity fan : fans) {
                Pattern += fan.Name + ":" + String.valueOf(fan.Address) + "~";
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            prefs.edit().putString("ConfiguredFans", Pattern).apply();

            // Android Wear Stuff
            final PutDataMapRequest putRequest = PutDataMapRequest.create("/devices");

            DataMap map = putRequest.getDataMap();
            map.putString("devices",Pattern);

            new Thread(new Runnable() {
                public void run() {
                    DataApi.DataItemResult result = Wearable.DataApi.putDataItem(
                            mGoogleApiClient, putRequest.asPutDataRequest())
                            .await();

                }
            }).start();





        }

    }
}

package carstendressler.com.fancontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import carstendressler.com.fancontroller.R;

/**
 * Created by Carsten on 8/3/2014.
 */
public class MainActivity extends Activity  implements
        GoogleApiClient.ConnectionCallbacks, WearableListView.ClickListener {

    private Context mContext;
    private Activity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private Node peerNode;

    private WearableListView mListView;
    private FanAdapter mAdapter;

    private float mDefaultCircleRadius;
    private float mSelectedCircleRadius;

    private static ArrayList<String> items = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;
        mContext = this;
        items.add("Loading...");


        mDefaultCircleRadius = getResources().getDimension(R.dimen.default_settings_circle_radius);
        mSelectedCircleRadius = getResources().getDimension(R.dimen.selected_settings_circle_radius);
        mAdapter = new FanAdapter();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mListView = (WearableListView) stub.findViewById(R.id.sample_list_view);
                mListView.setAdapter(mAdapter);
                mListView.setClickListener(MainActivity.this);
            }
        });

        //  Is needed for communication between the wearable and the device.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
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
    public void onConnectionSuspended(int i) {
    }


    @Override
    public void onClick(final WearableListView.ViewHolder viewHolder) {
        //Toast.makeText(this, items.get(viewHolder.getPosition()), Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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

                        DataMap map = new DataMap();
                        map.putString("Device", items.get(viewHolder.getPosition()));
                        map.putString("Mode", mode);


                        PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                                mGoogleApiClient,
                                peerNode.getId(),
                                "/command",
                                map.toByteArray()
                        );

                        //SendCommand(
                        //        String.valueOf(_fans.get(position).Address),
                        //        mode);

                        // Toast.makeText(_context,
                        // String.valueOf(_fans.get(position).Address),
                        // Toast.LENGTH_LONG).show();

                    }
                });
        builder.show();
    }

    @Override
    public void onTopEmptyRegionClick() {
        Toast.makeText(this, "You tapped into the empty area above the list", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("APP", "connected to Google Play Services on Wear!");

        new Thread(new Runnable() {
            public void run() {

                // Finds Node
                NodeApi.GetConnectedNodesResult rawNodes =  Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

                for (final Node node : rawNodes.getNodes()) {
                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient,
                            node.getId(),
                            "/start",
                            null
                    );

                    peerNode = node;

                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            //  The message is done sending.
                            //  This doesn't mean it worked, though.
                            //Log.v(TAG, "Our callback is done.");
                            peerNode = node;    //  Save the node that worked so we don't have to loop again.
                        }
                    });

                }

                PendingResult<DataApi.DataItemResult> pending;
                pending = Wearable.DataApi.getDataItem(mGoogleApiClient, getUriForDataItem());
                pending.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        DataItem dataItem = dataItemResult.getDataItem();
                        if (dataItem != null) {
                            Log.v("APP", "Data found!!");
                            final DataMap dataMap = DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap();
                            dataMap.getString("devices");

                            // Runs next code in UI Thread
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {

                                    items.clear();
                                    items.addAll(getConfiguredFans(dataMap.getString("devices")));
                                    mAdapter.notifyDataSetChanged();

                                }
                            });



                        }
                        else
                        {
                            Log.v("APP", "No Data found");
                            // Runs next code in UI Thread
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {

                                    items.clear();
                                    items.add("No Devices found");
                                    mAdapter.notifyDataSetChanged();

                                }
                            });


                        }

                    }
                });

            }
        }).start();

    }

    public static List<String> getConfiguredFans(String savedData) {
        List<String> rtnList = new ArrayList<String>();

        String buffer = savedData;
        String[] fans = buffer.split("~");

        if (buffer.length() == 0)
            return rtnList;

        for (String fan : fans) {

            if (fan.length() == 0)
                continue;

            String[] splits = fan.split(":");
            rtnList.add(splits[0]);
        }

        return rtnList;
    }
    private Uri getUriForDataItem() {
        // If you've put data on the local node
        String nodeId = peerNode.getId();
        // Or if you've put data on the remote node
        // String nodeId = getRemoteNodeId();
        // Or If you already know the node id
        // String nodeId = "some_node_id";
        return new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority(nodeId).path("/devices").build();
    }

    private String getLocalNodeId() {
        NodeApi.GetLocalNodeResult nodeResult = Wearable.NodeApi.getLocalNode(mGoogleApiClient).await();
        return nodeResult.getNode().getId();
    }

    private String getRemoteNodeId() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodesResult =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        List<Node> nodes = nodesResult.getNodes();
        if (nodes.size() > 0) {
            return nodes.get(0).getId();
        }
        return null;
    }

    public class FanAdapter extends WearableListView.Adapter {

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new WearableListView.ViewHolder(new FanView(MainActivity.this));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {
            FanView myItemView = (FanView) viewHolder.itemView;

            TextView textView = (TextView) myItemView.findViewById(R.id.text);
            textView.setText(items.get(i));

            CircledImageView imageView = (CircledImageView) myItemView.findViewById(R.id.image);
            imageView.setImageResource(R.drawable.ic_action_star);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private final class FanView extends FrameLayout implements WearableListView.Item {

        final CircledImageView image;
        final TextView text;
        private float mScale;

        public FanView(Context context) {
            super(context);
            View.inflate(context, R.layout.wearablelistview_item, this);
            image = (CircledImageView) findViewById(R.id.image);
            text = (TextView) findViewById(R.id.text);
        }

        @Override
        public float getProximityMinValue() {
            return mDefaultCircleRadius;
        }

        @Override
        public float getProximityMaxValue() {
            return mSelectedCircleRadius;
        }

        @Override
        public float getCurrentProximityValue() {
            return mScale;
        }

        @Override
        public void setScalingAnimatorValue(float value) {
            mScale = value;
            image.setCircleRadius(mScale);
            image.setCircleRadiusPressed(mScale);
        }

        @Override
        public void onScaleUpStart() {
            image.setAlpha(1f);
            text.setAlpha(1f);
        }

        @Override
        public void onScaleDownStart() {
            image.setAlpha(0.5f);
            text.setAlpha(0.5f);
        }
    }
}

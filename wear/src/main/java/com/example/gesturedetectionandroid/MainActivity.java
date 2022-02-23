package com.example.gesturedetectionandroid;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gesturedetectionandroid.databinding.ActivityMainBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView mTextView;
    private Button startbutton;
    private Button stopbutton;
    private TextView status;
    private ActivityMainBinding binding;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;

    private ArrayList<String> listItems;



    String datapath = "/message_path";

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError=false;

    public static String SERVICE_CALLED_WEAR = "WearListClicked";
    public static String TAG = "WearListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        accelerometer = new Accelerometer(this);
        gyroscope = new Gyroscope(this);

        accelerometer.setListener(new Accelerometer.Listener() {
            //on translation method of accelerometer
            @Override
            public void onTranslation(long timestamp,float tx, float ty, float ts) {
                // set the color red if the device moves in positive x axis
               // System.out.println(System.currentTimeMillis()+" ACC:"+tx+","+ty+','+ts);
                new SendThread(datapath, "acc:"+timestamp+","+tx+","+ty+","+ts+"\n").start();
            }
        });

        // create a listener for gyroscope
        gyroscope.setListener(new Gyroscope.Listener() {
            // on rotation method of gyroscope
            @Override
            public void onRotation(long timestemp,float rx, float ry, float rz) {
                // set the color green if the device rotates on positive z axis
              //  System.out.println(System.currentTimeMillis()+" GYR:" + rx + "," + ry + ',' + rz);
                new SendThread(datapath, "gyr:"+timestemp+","+rx+","+ry+","+rz+"\n").start();
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


      //  mTextView = binding.text;
        startbutton = binding.watchStartButton;
        stopbutton = binding.watchStopButton;
        status = binding.watchStatusLabel;
        status.setBackgroundColor(Color.RED);
        stopbutton.setEnabled(false);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    status.setBackgroundColor(Color.GREEN);
                    stopbutton.setEnabled(true);
                    startbutton.setEnabled(false);
                    accelerometer.register();
                    gyroscope.register();
            }
        });
        stopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startbutton.setEnabled(true);
                stopbutton.setEnabled(false);
                status.setBackgroundColor(Color.RED);
                accelerometer.unregister();
                gyroscope.unregister();
            }
        });
    }




    @Override
    protected void onStart(){
        super.onStart();
        if (!mResolvingError) {
                mGoogleApiClient.connect();
            }
    }

    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
    }




    @Override
    protected void onResume() {
        super.onResume();

        // this will send notification to
        // both the sensors to register
        //accelerometer.register();
       // gyroscope.register();
    }

    // create on pause method
    @Override
    protected void onPause() {
        super.onPause();

        // this will send notification in
        // both the sensors to unregister
      //  accelerometer.unregister();
      //  gyroscope.unregister();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void sendMessage(String Key) {

        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), SERVICE_CALLED_WEAR + "--" + Key, null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }

    }

    class SendThread extends Thread {
        String path;
        String message;

        //constructor
        SendThread(String p, String msg) {
            path = p;
            message = msg;
        }

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        public void run() {
            //first get all the nodes, ie connected wearable devices.
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                List<Node> nodes = Tasks.await(nodeListTask);

                //Now send the message to each device.
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        Integer result = Tasks.await(sendMessageTask);
                      //  Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                    } catch (ExecutionException exception) {
                        Log.e(TAG, "Task failed: " + exception);

                    } catch (InterruptedException exception) {
                        Log.e(TAG, "Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                Log.e(TAG, "Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Interrupt occurred: " + exception);
            }
        }
    }
}
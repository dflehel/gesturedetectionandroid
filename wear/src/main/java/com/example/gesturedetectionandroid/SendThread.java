package com.example.gesturedetectionandroid;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

class SendThread extends Thread {
    private final static String TAG = "Wear MainActivity";
    String path;
    String message;
    Context context;

    //constructor
    SendThread(String p, String msg, Context context) {
        path = p;
        message = msg;
        this.context = context;
    }

    //sends the message via the thread.  this will send to all wearables connected, but
    //since there is (should only?) be one, so no problem.
    public void run() {
        //first get all the nodes, ie connected wearable devices.
        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(this.context).getConnectedNodes();
       // try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
         //   List<Node> nodes = Tasks.await(nodeListTask);

            //Now send the message to each device.
         /*   for (Node node : nodes) {
                Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                try {
                    // Block on a task and get the result synchronously (because this is on a background
                    // thread).
                    Integer result = Tasks.await(sendMessageTask);
                    Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                } catch (ExecutionException exception) {
                    Log.e(TAG, "Task failed: " + exception);

                } catch (InterruptedException exception) {
                    Log.e(TAG, "Interrupt occurred: " + exception);
                }*/

      //      }

     //   } catch (ExecutionException exception) {
       //     Log.e(TAG, "Task failed: " + exception);

       // } catch (InterruptedException exception) {
         //   Log.e(TAG, "Interrupt occurred: " + exception);
       // }
    }
}

package com.example.gesturedetectionandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {


    String datapath = "/message_path";
    Button mybutton;
    TextView logger;
    protected Handler handler;
    String TAG = "Mobile MainActivity";
    int num = 1;
    TextView acc;
    TextView gyro;
    String filane1;
    TextView filename;
    Button savefile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        //System.out.println(dtf.format(now));
        filane1 = dtf.format(now);
        writeFileOnInternalStorage(getApplicationContext(),filane1+"acc.csv","");
        writeFileOnInternalStorage(getApplicationContext(),filane1+"gyr.csv","");


        setContentView(R.layout.activity_main);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                logthis(stuff.getString("logthis"));
                return true;
            }
        });
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        acc = findViewById(R.id.textView2);
        gyro = findViewById(R.id.textView4);
        filename = findViewById(R.id.filename_text);
        savefile = findViewById(R.id.save_button);
        savefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filane1 = filename.getText().toString();
                writeFileOnInternalStorage(getApplicationContext(),filane1+"acc.csv","");
                writeFileOnInternalStorage(getApplicationContext(),filane1+"gyr.csv","");
            }
        });
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
          //  Log.v(TAG, "Main activity received message: " + message);
            // Display message in UI
            logthis(message);
           // writeFileOnInternalStorage(getApplicationContext(),".csv","meszbazdmeg");

        }
    }


    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
       // File dir = new File(mcoContext.getFilesDir(), "mydir");
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File dir = new File(path, "adatok");
        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            File gpxfile = new File(dir, sFileName);
            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(sBody);
            writer.flush();
            writer.close();
          //  System.out.println("meglettttttttttttt");
           // System.out.println(dir.getAbsolutePath());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void logthis(String newinfo) {
        if (newinfo.compareTo("") != 0) {
         //  System.out.println(newinfo);
           String[] splitinfo = newinfo.split(":");
           if (splitinfo[0].equalsIgnoreCase("acc")){
               acc.setText(splitinfo[1]);
               writeFileOnInternalStorage(getApplicationContext(),filane1+"acc.csv",splitinfo[1]);
  //             writeFileOnInternalStorage(getApplicationContext(),filane1+"gyr.csv","");

           }
           else{
               gyro.setText(splitinfo[1]);
    //           writeFileOnInternalStorage(getApplicationContext(),filane1+"acc.csv","");
               writeFileOnInternalStorage(getApplicationContext(),filane1+"gyr.csv",splitinfo[1]);

           }
        }
    }
}
package com.dummyApps.myvoicemail;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity {
    public static boolean EVM;

    String[] permissions = {
            "android.permission.READ_PHONE_STATE",
            "android.permission.RECORD_AUDIO",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.WAKE_LOCK",
            "android.permission.DISABLE_KEYGUARD",
            "android.permission.ANSWER_PHONE_CALLS"
    };
    CallReceiver responseReceiver = null;
    Button recButton;
    Button playButton;
    ProgressBar playProgress;

    protected void onRestart() {

        super.onRestart();
        saveData();
    }

    protected void onResume() {
        super.onResume();
        loadData();
    }

    protected void onStart() {

        super.onStart();
        loadData();
    }

    @Override
    public void onDestroy() {
        //Unregister broadcast receiver
        if (responseReceiver != null) {
            unregisterReceiver(responseReceiver);
            responseReceiver = null;
        }
        super.onDestroy();
        saveData();
    }

    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();
        //GET PERMISSIONS
        recvPermissions();
        Switch s = (Switch) findViewById(R.id.enabler);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EVM = isChecked;
                saveData();
            }
        });
        //RECORD AUDIO
        String folderPath = StorageUtils.createDirectory(this, Environment.DIRECTORY_DCIM, "voicemails");
        String filePath = StorageUtils.GetFilePath(this, folderPath, "audiorecordtest.mp4");
        String logTag = "Eat your food.";

        Button recButton = findViewById(R.id.recButton);
        Button playButton = findViewById(R.id.playButton);

        playProgress = findViewById(R.id.playProgress);
        playProgress.setMax(10);

        RecordUtils.RecordButton RB = new RecordUtils.RecordButton(recButton, filePath, logTag, this);
        RecordUtils.PlayButton PB = new RecordUtils.PlayButton(playButton, playProgress, filePath, logTag);


        // initialize and register broadcast receiver

        IntentFilter filterRefresh = new IntentFilter();
        filterRefresh.addAction("android.intent.action.PHONE_STATE");
        filterRefresh.addAction("android.intent.action.MEDIA_BUTTON");


        responseReceiver = new CallReceiver(this);
        registerReceiver(responseReceiver,filterRefresh);


    }
    private void recvPermissions() {
        if (!hasPermissions(this, permissions))
            requestPermissions(permissions, 200);
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("SPREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Switch s = (Switch) findViewById(R.id.enabler);
        editor.putBoolean("BKEY", s.isChecked());
        editor.apply();
        editor.commit();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("SPREF", Context.MODE_PRIVATE);
        Switch s = (Switch) findViewById(R.id.enabler);
        s.setChecked(sharedPreferences.getBoolean("BKEY", false));
        EVM = sharedPreferences.getBoolean("BKEY", false);
    }

   // @Override
    public void checkPermissions() {
        if (!hasPermissions(this, permissions)) {
            requestPermissions(permissions, 200);
        } else {
            TelecomManager tm = (TelecomManager) this
                    .getSystemService(Context.TELECOM_SERVICE);

            if (tm == null) {
                // whether you want to handle this is up to you really
                throw new NullPointerException("tm == null");
            }

           if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
              return;
            }
            tm.acceptRingingCall();
            Log.i("VoiceMain","call accepted");
        }

    }
}
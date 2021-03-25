package com.dummyApps.myvoicemail;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity {

    String[] permissions = {
            "android.permission.READ_PHONE_STATE",
            "android.permission.RECORD_AUDIO",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.WAKE_LOCK",
            "android.permission.DISABLE_KEYGUARD",
    };

    Button recButton;
    Button playButton;
    ProgressBar playProgress;

    protected void onRestart() {

        super.onRestart();
        setContentView(R.layout.activity_main);
        recvPermissions();


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //GET PERMISSIONS
        recvPermissions();

        //RECORD AUDIO
        String folderPath = StorageUtils.createDirectory(this, Environment.DIRECTORY_DCIM, "voicemails");
        String filePath = StorageUtils.GetFilePath(this, folderPath, "audiorecordtest.mp4");
        String logTag = "Eat your food.";

        Button recButton = findViewById(R.id.recButton);
        Button playButton = findViewById(R.id.playButton);


        playProgress = findViewById(R.id.playProgress);

        playProgress.setMax(10);

        RecordUtils.RecordButton RB = new RecordUtils.RecordButton(recButton, filePath, logTag);
        RecordUtils.PlayButton PB = new RecordUtils.PlayButton(playButton, playProgress, filePath, logTag);

    }

    protected void onStart() {
        super.onStart();

    }
    private void recvPermissions() {
        if (!hasPermissions(this, permissions)) requestPermissions(permissions, 200);
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


}
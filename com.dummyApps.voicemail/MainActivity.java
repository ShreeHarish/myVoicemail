package com.example.myvoicemail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.content.Context;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    String[] permissions = {
            "android.permission.READ_PHONE_STATE",
            "android.permission.RECORD_AUDIO",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };
    Button recButton;
    Button playButton;
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
        String filePath = RecordUtils.CreateFile(this,"audiorecordtest.mp3");
        String logTag = "Eat your food.";

        Button recButton = findViewById(R.id.recButton);
        Button playButton = findViewById(R.id.playButton);

        RecordUtils.RecordButton RB = new RecordUtils.RecordButton(recButton, filePath, logTag);
        RecordUtils.PlayButton PB = new RecordUtils.PlayButton(playButton, filePath, logTag);

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

    private void closeApp(){
        finish();
    }
}
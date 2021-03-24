package com.example.myvoicemail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.content.Context;


public class MainActivity extends AppCompatActivity {

    String[] permissions = {
            "android.permission.READ_PHONE_STATE",
            "android.permission.RECORD_AUDIO",
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
        String filePath = getExternalCacheDir().getAbsolutePath();
        filePath += "/audiorecordtest.3gp";
        String logTag = "Eat your food.";

        MediaRecorder mr = new MediaRecorder();
        MediaPlayer mp = new MediaPlayer();

        Button recButton = findViewById(R.id.recButton);
        Button playButton = findViewById(R.id.playButton);

        RecordUtils.RecordButton RB = new RecordUtils.RecordButton(recButton, mr, filePath, logTag);
        RecordUtils.PlayButton PB = new RecordUtils.PlayButton(playButton, mp, filePath, logTag);


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
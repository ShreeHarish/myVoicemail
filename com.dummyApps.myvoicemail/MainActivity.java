
package com.dummyApps.myvoicemail;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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
    };

    Button recButton;
    Button playButton;
    ProgressBar playProgress;

    protected void onRestart() {

        super.onRestart();
        saveData();
    }

    protected void onResume(){
        super.onResume();
        loadData();
    }

    protected void onStart(){

        super.onStart();
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveData();
    }

    protected void onPause(){
        super.onPause();
        saveData();
    }

    @Override
    public void onStop(){
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
        Switch s = (Switch)findViewById(R.id.enabler);
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
    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("SPREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Switch s = (Switch)findViewById(R.id.enabler);
        editor.putBoolean("BKEY", s.isChecked());
        editor.apply();
        editor.commit();
    }
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("SPREF", Context.MODE_PRIVATE);
        Switch s = (Switch)findViewById(R.id.enabler);
        s.setChecked(sharedPreferences.getBoolean("BKEY", false));
        EVM = sharedPreferences.getBoolean("BKEY", false);
    }
}
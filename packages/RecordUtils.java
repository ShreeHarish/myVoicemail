package com.example.myvoicemail;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class RecordUtils{

    private static void onRecord(boolean start, MediaRecorder recorder, String filePath, String LOG_TAG) {

        if (start) {
            startRecording(recorder, filePath, LOG_TAG);
        } else {
            stopRecording(recorder);
        }
    }

    private static void onPlay(boolean start, MediaPlayer player, String filePath, String LOG_TAG) {

        if (start) {
            startPlaying(player, filePath, LOG_TAG);
        } else {
            stopPlaying(player);
        }
    }

    private static void startPlaying(MediaPlayer player, String filePath, String LOG_TAG) {
        try {
            player.setDataSource(filePath);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private static void stopPlaying(MediaPlayer player) {
        player.release();
    }

    private static void startRecording(MediaRecorder recorder, String filePath, String LOG_TAG) {

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private static void stopRecording(MediaRecorder recorder) {
        recorder.stop();
        recorder.release();
    }

    public static class RecordButton{

        Button b;
        MediaRecorder rec;
        String fp;
        String LT;

        boolean mStartRecording = true;

        View.OnClickListener clicker = v -> {

            if (mStartRecording) {
                rec = new MediaRecorder();
                b.setText("Stop recording");
            } else {
                b.setText("Start recording");
            }

            onRecord(mStartRecording, rec, fp, LT);
            mStartRecording = !mStartRecording;
        };

        public RecordButton(Button button, String filePath, String LOG_TAG) {

            b = button;
            fp = filePath;
            LT = LOG_TAG;

            b.setText("Start recording");

            b.setOnClickListener(clicker);
        }
    }

    public static class PlayButton {

        Button b;
        MediaPlayer p;
        String fp;
        String LT;

        boolean mStartPlaying = true;

        View.OnClickListener clicker = v -> {

            if (mStartPlaying) {
                p = new MediaPlayer();
                b.setText("Stop playing");
            } else {
                b.setText("Start playing");
            }

            onPlay(mStartPlaying, p, fp, LT);
            mStartPlaying = !mStartPlaying;
        };

        public PlayButton(Button button, String filePath, String LOG_TAG) {

            b = button;
            fp = filePath;
            LT = LOG_TAG;

            button.setText("Start playing");

            button.setOnClickListener(clicker);
        }
    }

    public static String CreateFile(Context context, String fileName){

        File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "voicemails");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File test = new File(folder, "dummy.txt");

        try {
            FileOutputStream out = new FileOutputStream(test);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String parents = folder.getAbsolutePath();

        String path = parents + File.separator + fileName;

        return path;
    }
}




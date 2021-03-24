package com.example.myvoicemail;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

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
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

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

            onRecord(mStartRecording, rec, fp, LT);
            if (mStartRecording) {
                b.setText("Stop recording");
            } else {
                b.setText("Start recording");
            }
            mStartRecording = !mStartRecording;
        };

        public RecordButton(Button button, MediaRecorder recorder, String filePath, String LOG_TAG) {

            b = button;
            rec = recorder;
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
            onPlay(mStartPlaying, p, fp, LT);
            if (mStartPlaying) {
                b.setText("Stop playing");
            } else {
                b.setText("Start playing");
            }
            mStartPlaying = !mStartPlaying;
        };

        public PlayButton(Button button, MediaPlayer player, String filePath, String LOG_TAG) {

            b = button;
            p = player;
            fp = filePath;
            LT = LOG_TAG;

            button.setText("Start playing");

            button.setOnClickListener(clicker);
        }
    }
}


package com.example.myvoicemail;

import android.animation.FloatArrayEvaluator;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
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
        ProgressBar PB;
        long duration;

        boolean mStartPlaying = true;

        View.OnClickListener clicker = v -> {

            duration = getDuration(StorageUtils.GetFile(fp));

            if (mStartPlaying) {
                p = new MediaPlayer();
                b.setText("Stop playing");

                CountDownTimer mCountdowntimer = new CountDownTimer(duration, 1000) {

                    public void onTick(long millisUntilFinished) {
                        int progress = (int) (millisUntilFinished/1000);
                        b.setText(Long.toString(millisUntilFinished));
                        PB.setProgress(PB.getMax() - progress);
                    }

                    public void onFinish() {
                        onPlay(mStartPlaying, p, fp, LT);
                        mStartPlaying = !mStartPlaying;
                        b.setText("Start playing");
                        PB.setProgress(0);
                    }

                }.start();

            } else {
                b.setText("Start playing");
                PB.setProgress(0);
            }

            onPlay(mStartPlaying, p, fp, LT);
            mStartPlaying = !mStartPlaying;
        };

        public PlayButton(Button button, ProgressBar playProgress, String filePath, String LOG_TAG) {

            b = button;
            fp = filePath;
            LT = LOG_TAG;
            PB = playProgress;

            button.setText("Start playing");

            button.setOnClickListener(clicker);
        }
    }

    private static long getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Long.parseLong(durationStr);
    }
}




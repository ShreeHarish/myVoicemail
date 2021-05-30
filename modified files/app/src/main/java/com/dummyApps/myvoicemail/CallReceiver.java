package com.dummyApps.myvoicemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import java.io.*;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.DataOutputStream;


public class CallReceiver extends BroadcastReceiver {
    static Boolean AttendController;

    private MainActivity callback;

    public CallReceiver(MainActivity callback){

        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("VoiceMain","phone state received");

        callback.checkPermissions();

        if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
            showText(context, "Call started da");
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){
            showText(context, "Call ended da");
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)){
            showText(context, "Call ringing da");


        }
    }

    void showText(Context context, String msg){
        Toast.makeText(context.getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }


}
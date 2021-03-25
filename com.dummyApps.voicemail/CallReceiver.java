package com.dummyApps.myvoicemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import java.io.*;

import android.view.KeyEvent;
import android.widget.Toast;

import java.io.DataOutputStream;


public class CallReceiver extends BroadcastReceiver {
    static Boolean AttendController;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
            showText(context, "Call started da");
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){
            showText(context, "Call ended da");
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)){
            showText(context, "Call ringing da");
            Intent i = new Intent(context, AcceptCallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        }
    }

    void showText(Context context, String msg){
        Toast.makeText(context.getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }


}

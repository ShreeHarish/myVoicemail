package com.example.myvoicemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {

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

            String incomingNumber =
                    intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (incomingNumber.contentEquals("1234567")) {

                Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
                i.putExtra(Intent.EXTRA_KEY_EVENT,
                        new KeyEvent(KeyEvent.ACTION_UP,
                                KeyEvent.KEYCODE_HEADSETHOOK));
                context.sendOrderedBroadcast(i, null);
            }
        }
    }

    void showText(Context context, String msg){
        Toast.makeText(context.getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
    }
}

package com.example.moham.whereyouare1;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by moham on 7/29/2016.
 */
public class MsgReceiver extends BroadcastReceiver {
    String str = "";
    String phone_sender;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        SmsMessage[] msgs;
        String[] splits = new String[2];
        if (extras != null) {
            Object[] pduses = (Object[]) extras.get("pdus");
            msgs = new SmsMessage[pduses.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pduses[i]);
                if (i == 0) {
                    phone_sender = msgs[i].getOriginatingAddress();
                }
                str += msgs[i].getMessageBody().toString();
            }
            splits = str.split("-");
            if(splits[2].equals("xy"))
                abortBroadcast();
        }
        if (splits[0].equals("m")) {
            NotificationManager notification_manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            Intent Go_to_Mainactivity_intent = new Intent(context, MainActivity.class);
            Go_to_Mainactivity_intent.putExtra("sender_phone", phone_sender);
            PendingIntent pending_intent = PendingIntent.getActivity(context, 0, Go_to_Mainactivity_intent, 0);
            Notification notification = new Notification.Builder(context)
                    .setContentTitle("Title")
                    .setContentText(phone_sender + " wants you to send him your location")
                    .setContentIntent(pending_intent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.dr)
                    .build();
            notification_manager.notify(0, notification);
        } else {
            Intent intent_to_maps = new Intent(context, MapsActivity.class);
            intent_to_maps.putExtra("lati", splits[0].toString());
            intent_to_maps.putExtra("longi", splits[1].toString());
            Log.d("longi = ", splits[1]);
            intent_to_maps.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent_to_maps);
        }

    }
}
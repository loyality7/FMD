package de.nulide.findmydevice.receiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.Objects;

import de.nulide.findmydevice.services.FMDSMSService;
import de.nulide.findmydevice.utils.Logger;

public class SMSReceiver extends SuperReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        if (Objects.equals(intent.getAction(), SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String format = bundle.getString("format");
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                int subscriptionId = intent.getIntExtra("subscription", -1);
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    String sender = msgs[i].getOriginatingAddress();
                    FMDSMSService.scheduleJob(context, sender, subscriptionId, msgs[i].getMessageBody());
                }
            }
        }
        Logger.writeLog();
    }
}

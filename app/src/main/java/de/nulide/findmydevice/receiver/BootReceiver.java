package de.nulide.findmydevice.receiver;

import android.content.Context;
import android.content.Intent;


import org.unifiedpush.android.connector.UnifiedPush;

import de.nulide.findmydevice.data.ConfigSMSRec;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.services.FMDServerCommandService;
import de.nulide.findmydevice.services.FMDServerService;
import de.nulide.findmydevice.utils.Logger;
import de.nulide.findmydevice.utils.Notifications;

public class BootReceiver extends SuperReceiver{

    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        if (intent.getAction().equals(BOOT_COMPLETED)) {
            Logger.logSession("AfterBootTest", "passed");
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT, null);
            config.set(ConfigSMSRec.CONF_TEMP_WHITELISTED_CONTACT_ACTIVE_SINCE, null);
            ch.getSettings().set(Settings.SET_GPS_STATE, 1);
            if(ch.getSettings().checkAccountExists()){
                FMDServerService.scheduleJob(context, 0);
                PushReceiver.Register(context);
                FMDServerService.checkForOldSalt(context);
            }
        }
    }

}

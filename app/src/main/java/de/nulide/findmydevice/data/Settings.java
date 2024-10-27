package de.nulide.findmydevice.data;


import java.util.HashMap;

import de.nulide.findmydevice.BuildConfig;
import de.nulide.findmydevice.utils.RingerUtils;


public class Settings extends HashMap<Integer, Object> {

    public static final int SETTINGS_VERSION = 2;

    public static final int SET_WIPE_ENABLED = 0;
    public static final int SET_ACCESS_VIA_PIN = 1;
    public static final int SET_LOCKSCREEN_MESSAGE = 2;
    public static final int SET_PIN = 3;
    public static final int SET_FMD_COMMAND = 4;
    public static final int SET_OPENCELLID_API_KEY = 5;
    //public static final int SET_INTRODUCTION_VERSION = 6;
    public static final int SET_RINGER_TONE = 7;
    public static final int SET_SET_VERSION = 8;

    //public static final int SET_FMDSERVER_UPLOAD_SERVICE = 101;
    public static final int SET_FMDSERVER_URL = 102;
    public static final int SET_FMDSERVER_UPDATE_TIME = 103;
    public static final int SET_FMDSERVER_ID = 104;
    public static final int SET_FMDSERVER_PASSWORD_SET = 105;
    public static final int SET_FMDSERVER_LOCATION_TYPE = 106; // 0=GPS, 1=CELL, 2=ALL, 3=NONE
    //public static final int SET_FMDSERVER_AUTO_UPLOAD = 107;
    public static final int SET_FMD_CRYPT_PUBKEY = 108;
    public static final int SET_FMD_CRYPT_PRIVKEY = 109;
    public static final int SET_FMD_CRYPT_HPW = 110;
    public static final int SET_FMD_LOW_BAT_SEND = 111;

    //public static final int SET_FMD_CRYPT_NEW_SALT = 112;
    public static final int SET_UPDATEBOARDING_MODERN_CRYPTO_COMPLETED = 113;


    public static final int SET_FIRST_TIME_WHITELIST = 301;
    public static final int SET_FIRST_TIME_CONTACT_ADDED = 302;
    //public static final int SET_FIRST_TIME_FMD_SERVER = 303;

    public static final int SET_APP_CRASHED_LOG_ENTRY = 401; // 0=no crash, 1=crash to show
    public static final int SET_FMDSMS_COUNTER = 402;

    //public static final int SET_GPS_STATE = 501;         // 0=GPS is off 1=GPS is on 2=GPS is turned on by FMD
    public static final int SET_LAST_KNOWN_LOCATION_LAT = 502;
    public static final int SET_LAST_KNOWN_LOCATION_LON = 503;
    public static final int SET_LAST_KNOWN_LOCATION_TIME = 504;
    public static final int SET_LAST_LOW_BAT_UPLOAD = 505;

    public static final int SET_THEME = 601;
    public static final String VAL_THEME_FOLLOW_SYSTEM = "follow_system";
    public static final String VAL_THEME_LIGHT = "light";
    public static final String VAL_THEME_DARK = "dark";

    public static final String DEFAULT_FMD_SERVER_URL = "https://fmd.nulide.de";

    public Settings() {
    }

    public Object get(int key) {
        if (super.containsKey(key)) {
            return super.get(key);
        } else {
            switch (key) {
                case SET_WIPE_ENABLED:
                case SET_ACCESS_VIA_PIN:
                case SET_FIRST_TIME_WHITELIST:
                case SET_FIRST_TIME_CONTACT_ADDED:
                    //case SET_FIRST_TIME_FMD_SERVER:
                case SET_FMDSERVER_PASSWORD_SET:
                    //case SET_FMD_CRYPT_NEW_SALT:
                case SET_UPDATEBOARDING_MODERN_CRYPTO_COMPLETED:
                    return false;
                case SET_FMD_LOW_BAT_SEND:
                    return true;
                case SET_FMD_COMMAND:
                    return BuildConfig.DEFAULT_FMD_COMMAND;
                case SET_FMDSERVER_UPDATE_TIME:
                    return 60;
                //case SET_INTRODUCTION_VERSION:
                case SET_FMDSMS_COUNTER:
                case SET_FMDSERVER_LOCATION_TYPE:
                case SET_SET_VERSION:
                    return 0;
                case SET_RINGER_TONE:
                    return RingerUtils.getDefaultRingtoneAsString();
                case SET_PIN:
                case SET_FMDSERVER_ID:
                case SET_LAST_KNOWN_LOCATION_LAT:
                case SET_LAST_KNOWN_LOCATION_LON:
                case SET_FMD_CRYPT_HPW:
                case SET_FMD_CRYPT_PRIVKEY:
                case SET_FMD_CRYPT_PUBKEY:
                case SET_FMDSERVER_URL:
                    return "";
                //case SET_GPS_STATE:
                //    return 1;
                case SET_APP_CRASHED_LOG_ENTRY:
                    return 0;
                case SET_LAST_KNOWN_LOCATION_TIME:
                case SET_LAST_LOW_BAT_UPLOAD:
                    return -1L;
                case SET_THEME:
                    return VAL_THEME_FOLLOW_SYSTEM;
            }
        }
        return "";
    }

}

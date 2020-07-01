package ru.ppr.cppk.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;

import java.util.TimeZone;

import ru.ppr.logger.Logger;
import ru.ppr.utils.RootShellInterface;

public class Utils {

    private static final String TAG = Logger.makeLogTag(Utils.class);

    // задает время на root устройстве
    public static boolean setTime(long time) {
        if (RootShellInterface.isSuAvailable()) {
            RootShellInterface.runCommand("chmod 666 /dev/alarm");
            SystemClock.setCurrentTimeMillis(time);
            RootShellInterface.runCommand("chmod 664 /dev/alarm");
            return true;
        }
        return false;
    }

    public static String getSoftwareVersion(Context c) {
        String version = null;
        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * Подберет таймзону по смещению и выставит ее в системе
     * @param offset
     * @return
     */
    public static boolean setTimeZoneOffset(Context context, int offset) {
        Logger.info(TAG, "setTimeZoneOffset("+offset+") START");
        boolean res = offset == TimeZone.getDefault().getRawOffset();
        if (!res) {
            String[]TZ = TimeZone.getAvailableIDs();

            TimeZone goodTz = null;
            for(int i = 0; i < TZ.length; i++) {
                TimeZone tz = TimeZone.getTimeZone(TZ[i]);
                if(tz.getRawOffset()==offset) {
                    goodTz=tz;
                    break;
                }
            }

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setTimeZone(goodTz.getID());
            res = offset == TimeZone.getDefault().getRawOffset();
        }
        Logger.info(TAG, "setTimeZoneOffset("+offset+") FINISH res:"+((res)?"OK":"ERROR"));
        return res;
    }



}

/**
 * Класс для обновления, удаления, запуска приложений с использованием root
 */
package ru.ppr.cppkupdater;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.Globals;
import ru.ppr.logger.Logger;

public class RootUtils {

    private static final String TAG = Logger.makeLogTag(RootUtils.class);

    /**
     * Класс хранилище для информации о приложении
     *
     * @author user
     */
    public static class PInfo {
        public String appname = "";
        public String pname = "";
        public String versionName = "";
        public int versionCode = 0;
    }

    /**
     * Удаляет приложение
     *
     * @param packageName
     * @return
     */
    public static boolean uninstallApp(String packageName) {
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "pm uninstall " + packageName});
            return true;
        } catch (IOException e) {
            System.out.println(e.toString());
            System.out.println("no root");
            return false;
        }
    }

    /**
     * Возвращает информацию о приложении
     *
     * @param packagename
     * @param context
     * @return
     */
    public static PInfo getPackageData(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo p = pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);

            PInfo newInfo = new PInfo();
            newInfo.appname = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
            newInfo.pname = p.packageName;
            newInfo.versionName = p.versionName;
            newInfo.versionCode = p.versionCode;

            return newInfo;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Запускает приложение
     *
     * @param packageName
     * @param activityName
     * @return
     */
    public static boolean runApp(String packageName, String activityName) {
        String prefix = "runApp(packageName=\""+packageName+"\", activityName=\""+activityName+"\") ";
        Logger.trace(TAG, prefix+"START");
        boolean res = false;
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "am start -n " + packageName + "/" + packageName + "." + activityName});
            res = true;
        } catch (IOException e) {
            Logger.trace(TAG, prefix+"ERROR no ROOT "+e.getMessage());
        }
        Logger.trace(TAG, prefix+"FINISH return: "+res);
        return res;
    }

    /**
     * Устанавливает/обновляет приложение
     *
     * @param apkFile
     * @return
     */
    public static boolean installNewApk(File apkFile) {
        if (apkFile == null || !apkFile.exists() || apkFile.isDirectory())
            return false;
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "pm install -r " + apkFile.getAbsolutePath()});
            return true;
        } catch (IOException e) {
            System.out.println(e.toString());
            System.out.println("no root");
            return false;
        }
    }

    /**
     * запрещает отображение верхнего системнного статус-бара. Нужны root права, вернет true в случае удачи.
     *
     * @return
     */
    public static boolean disableSystemBar() {
        Logger.trace(TAG, "disableSystemBar() START");
        boolean res = false;
        if (!BuildConfig.USE_REAL_DEVICES_BY_DEFAULT) {
            res = true;
        }
        else {
            // 1 - get app uid
            String appUid = ShellCommand.GetAppUid();
            Logger.trace(TAG, "disableSystemBar() App Uid = " + appUid);
            if(appUid.length()>1) {

                // 2 - verify if we can execute su command
                if (checkRoot()) {
                    Logger.trace(TAG, "disableSystemBar() su available ");
                    // su available so place here the command ypu ant to execute
                    List<String> cmd = new ArrayList<String>();
                    cmd.add("service call activity 42 s16 com.android.systemui");
                    //cmd.add(chmodCmd);
                    Logger.trace(TAG, "disableSystemBar() chown : ");
                    List<String> results = Shell.SU.run(cmd);
                    res = true;
                }else {
                    Logger.error(TAG, "disableSystemBar() su not available ");
                }

            }else {
                Logger.error(TAG, "disableSystemBar() Fail to get application UID");
            }
        }
        Logger.trace(TAG, "disableSystemBar() FINISH return: "+res);
        return res;
    }

    /**
     * разрешает отображение верхнего системнного статус-бара. Нужны root права, вернет true в случае удачи.
     *
     * @return
     */
    public static boolean enableSystemBar() {
        Logger.trace(TAG, "enableSystemBar() START");
        boolean res = false;
        if (!BuildConfig.USE_REAL_DEVICES_BY_DEFAULT) {
            res = true;
        }
        else if (checkRoot()){
            try {
                String command;
                command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
                Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                proc.waitFor();
                res = true;
            } catch (Exception e) {
                Logger.trace(TAG, "enableSystemBar() ERROR "+e.getMessage());
            }
        }
        Logger.trace(TAG, "enableSystemBar() FINISH return: "+res);
        return res;
    }

    @Deprecated
    private static boolean checkRootOld() {
        boolean res = false;
        PackageManager m = Globals.getInstance().getPackageManager();
        String s = Globals.getInstance().getPackageName();

        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            s = p.applicationInfo.dataDir;

            File testFolder = new File(s+"/testRootAccess");

            if (testFolder.exists())
                testFolder.delete();

            String command;
            command = "mkdir "+testFolder.getAbsolutePath();
            Process proc = null;
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            proc.waitFor();

            if (testFolder.exists()) {
                res=true;
                testFolder.delete();
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Logger.info(TAG, "checkRoot() return: "+res);
        return res;
    }

    private static boolean checkRoot() {
        Logger.trace(TAG, "checkRoot() START");
        boolean suAvailable = Shell.SU.available();
        Logger.trace(TAG, "checkRoot() FINISH return: "+suAvailable);
        return suAvailable;
    }
}

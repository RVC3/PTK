/**
 * Данный класс устанавливает apk файл и запускает его
 */

package ru.ppr.cppkupdater;

import android.os.AsyncTask;

import java.io.File;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.helpers.SchedulersCPPK;

public class Updater {

    /**
     * задержка между попытками поиска
     */
    private static final int delay = 1000;

    /**
     * максимальное количество попыток
     */
    private static final int max_repeat_count = 15;

    private Globals g;

    public Updater(Globals g) {
        this.g = g;
    }

    private InstallListener listener = null;
    private String packageName = "";
    private String activityName = "";
    private File apkFile;

    public void installAndRun(InstallListener listener, File apkFile, String packageName, String activityName) {
        this.listener = listener;
        this.packageName = packageName;
        this.activityName = activityName;
        this.apkFile = apkFile;
        boolean res = RootUtils.installNewApk(apkFile);
        if (!res) {
            finish(false, "start install root command error");
        } else {
            (new InstallWaiter()).executeOnExecutor(SchedulersCPPK.backgroundExecutor());
        }
    }

    /**
     * Асинхронный таск ожидающий когда apk реально установится
     *
     * @author user
     */
    private class InstallWaiter extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... noargs) {

            int waitSeconds = 0;

            while (RootUtils.getPackageData(packageName, g.getApplicationContext()) == null && waitSeconds < max_repeat_count) {
                try { // ждем секунду
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitSeconds++;
            }
            if (waitSeconds >= max_repeat_count)
                return false;

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (RootUtils.runApp(packageName, activityName)) {
                    finish(true, "");
                } else {
                    finish(false, "run root command faid");
                }
            } else
                finish(false, "error timeout wait install!");
        }
    }

    private void finish(boolean isOk, String error) {
        if (listener != null) {
            boolean deleteApkFile = listener.ready(isOk, error);
            if (deleteApkFile) {
                if (apkFile.exists()) {
                    apkFile.delete();
                }
            }
        }
    }

}

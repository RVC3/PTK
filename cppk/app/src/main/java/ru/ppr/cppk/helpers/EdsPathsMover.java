package ru.ppr.cppk.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import ru.ppr.core.manager.eds.EdsDirs;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Класс помощник для перемещения папок ЭЦП в нужное место.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsPathsMover {

    private static final String TAG = Logger.makeLogTag(EdsPathsMover.class);

    private Context mContext;
    private EdsDirs mEdsDirs;

    public EdsPathsMover(Context context, EdsDirs edsDirs) {
        mContext = context;
        mEdsDirs = edsDirs;
    }

    public void moveDirs() {
        SharedPreferences preferences = mContext.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        File oldSftPath = new File(preferences.getString("sft_path", mContext.getApplicationInfo().dataDir));

        File oldWorkingPath = new File(oldSftPath, "working");
        if (oldWorkingPath.exists()) {
            if (mEdsDirs.getEdsWorkingDir().exists()) {
                Logger.trace(TAG, "Clearing existing data in " + mEdsDirs.getEdsWorkingDir().getAbsolutePath());
                FileUtils2.clearDir(mEdsDirs.getEdsWorkingDir(), null);
            }
            if (!FileUtils2.renameFile(oldWorkingPath, mEdsDirs.getEdsWorkingDir(), null)) {
                Logger.error(TAG, "Moving dir failed from " + oldWorkingPath.getAbsolutePath() + " to " + mEdsDirs.getEdsWorkingDir().getAbsolutePath());
            }
        }

        File oldInPath = new File(oldSftPath, "in");
        if (oldInPath.exists()) {
            if (mEdsDirs.getEdsTransportInDir().exists()) {
                Logger.trace(TAG, "Clearing existing data in " + mEdsDirs.getEdsTransportInDir().getAbsolutePath());
                FileUtils2.clearDir(mEdsDirs.getEdsTransportInDir(), null);
            }
            if (!FileUtils2.renameFile(oldInPath, mEdsDirs.getEdsTransportInDir(), null)) {
                Logger.error(TAG, "Moving dir failed from " + oldInPath.getAbsolutePath() + " to " + mEdsDirs.getEdsTransportInDir().getAbsolutePath());
            }
        }

        File oldOutPath = new File(oldSftPath, "out");
        if (oldOutPath.exists()) {
            if (mEdsDirs.getEdsTransportOutDir().exists()) {
                Logger.trace(TAG, "Clearing existing data in " + mEdsDirs.getEdsTransportOutDir().getAbsolutePath());
                FileUtils2.clearDir(mEdsDirs.getEdsTransportOutDir(), null);
            }
            if (!FileUtils2.renameFile(oldOutPath, mEdsDirs.getEdsTransportOutDir(), null)) {
                Logger.error(TAG, "Moving dir failed from " + oldOutPath.getAbsolutePath() + " to " + mEdsDirs.getEdsTransportOutDir().getAbsolutePath());
            }
        }

        File oldSftUtlPath = mContext.getFilesDir();

        File utilFile = new File(oldSftUtlPath, "lic_support.android");
        if (utilFile.exists()) {
            if (!utilFile.delete()) {
                Logger.error(TAG, "Could not delete file: " + utilFile.getAbsolutePath());
            }
        }
        File utilLibFile = new File(oldSftUtlPath, "libcryptoc.so");
        if (utilLibFile.exists()) {
            if (!utilLibFile.delete()) {
                Logger.error(TAG, "Could not delete file: " + utilLibFile.getAbsolutePath());
            }
        }
    }
}
package ru.ppr.cppk.helpers;

import android.content.Context;

import java.io.File;

import ru.ppr.cppk.PathsConstants;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Класс помощник для удаления старой папки со скриншотами.
 *
 * @author Aleksandr Brazhkin
 */
public class FeedbackPathsCleaner {

    private static final String TAG = Logger.makeLogTag(FeedbackPathsCleaner.class);

    private Context mContext;

    public FeedbackPathsCleaner(Context context) {
        mContext = context;
    }

    public void deleteOldDir() {
        File oldPath = new File(PathsConstants.LOG, "REPORT");
        if (oldPath.exists()) {
            Logger.trace(TAG, "Deleting existing old feedback dir: " + oldPath.getAbsolutePath());
            if (!FileUtils2.clearDir(oldPath, mContext)) {
                Logger.error(TAG, "Could not delete dir: " + oldPath.getAbsolutePath());
            }

        }
    }
}
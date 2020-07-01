package ru.ppr.cppk.helpers;

import java.io.File;

import ru.ppr.cppk.PathsConstants;
import ru.ppr.logger.Logger;

/**
 * Хелпер, обеспечивает логику записи отчета в файл
 *
 * @see ru.ppr.cppk.systembar.FeedbackActivity
 * @see ru.ppr.cppk.systembar.FeedbackActivityDelegate
 * Created by Григорий on 16.03.2017.
 */
public class FeedbackHelper {

    private static final String TAG = Logger.makeLogTag(FeedbackHelper.class);

    /**
     * Максимальное количество скриншотов в папке FEEDBACK
     */
    public static final int REPORT_MAX_SCREENS_COUNT = 10;

    public FeedbackHelper() {
    }

    public static FeedbackHelper getInstance() {
        return new FeedbackHelper();
    }

    public void saveFeedback(File pngFile, String text) {
        SchedulersCPPK.backgroundExecutor().execute(() -> {
            String prefix = "saveFeedback(\"" + ((pngFile == null) ? "null" : pngFile.getName()) + "\", \"" + text + "\") ";
            Logger.trace(TAG, prefix + "START");

            long time = System.currentTimeMillis();

            File reportDir = new File(PathsConstants.FEEDBACK);
            reportDir.mkdirs();

            //почистим старые файлы в папке, чтобы не засорять sd-карту
            int startCount = reportDir.listFiles().length;
            while (startCount > REPORT_MAX_SCREENS_COUNT) {
                File oldestFile = FeedbackHelper.this.getOldestFileFromDir(reportDir);
                if (oldestFile != null)
                    Logger.trace(TAG, prefix + "Удаляем старый скриншот: " + oldestFile.getName() + " - " + oldestFile.delete());
                else
                    Logger.trace(TAG, prefix + "Error удаляемый файл оказался - null");
                //на всякий случай, чтобы не попасть в бесконечный цикл
                if (startCount == reportDir.listFiles().length)
                    break;
                startCount = reportDir.listFiles().length;
            }


            Logger.trace(TAG, prefix + "FINISH " + (System.currentTimeMillis() - time) + "ms");
        });
    }

    /**
     * Найдет в папке самый старый файл
     *
     * @param dir
     * @return
     */
    private File getOldestFileFromDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() > files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }
}

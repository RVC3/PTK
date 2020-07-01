package ru.ppr.cppk.systembar;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.helpers.FeedbackHelper;
import ru.ppr.cppk.ui.dialog.SimpleFeedbackDialog;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ShellCommand;

/**
 * Класс для реализации логики создания отчетов обратной связи
 *
 * @see ru.ppr.cppk.systembar.FeedbackActivity
 * @see ru.ppr.cppk.helpers.FeedbackHelper
 * Created by Григорий on 15.03.2017.
 */
public class FeedbackActivityDelegate {

    private static final String TAG = Logger.makeLogTag(FeedbackActivityDelegate.class);

    private Context mcontext = null;

    public FeedbackActivityDelegate(Context context) {
        mcontext = context;
    }

    public static FeedbackActivityDelegate getInstance(Context context) {
        return new FeedbackActivityDelegate(context);
    }

    /**
     * Показать диалог, ввода сообщение об ошибке
     */
    public void showDialog() {
        Logger.info(TAG, "showTripOpeningClosureDialog() START");

        File file = makeScreenshotFile();

        SimpleFeedbackDialog dialog = SimpleFeedbackDialog.newInstance(mcontext);
        dialog.setDialogPositiveBtnClickListener((dialog1, inputText) -> {
            FeedbackHelper.getInstance().saveFeedback(file, inputText);
        });
        dialog.setDialogNegativeBtnClickListener((dialog12, inputText) -> {
            if (file != null)
                file.delete();
            Logger.info(TAG, "showTripOpeningClosureDialog() Отказались от сохранения отчета");
        });
        dialog.show();
        Logger.info(TAG, "showTripOpeningClosureDialog() FINISH");
    }

    private File makeScreenshotFile() {
        Logger.trace(TAG, "makeScreenshotFile() START");

        long time = System.currentTimeMillis();
        String filePath = getImageFilePath(time);
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        //применим костылек, поскольку железка почему-то считает внутреннюю память sd картой
        String internalStoragePath = Environment.getExternalStorageDirectory().getPath();
        String workedStoragePath = "/sdcard";
        String commandFilePath = filePath.replaceAll(internalStoragePath, workedStoragePath);

        String command = "/system/bin/screencap -p " + commandFilePath;
        ShellCommand shellCommand = new ShellCommand.Builder(command).setRunAsSu(true).build();
        try {
            shellCommand.run();
            Logger.trace(TAG, "runMakeScreenshotCommand output:\n" + shellCommand.getOutput());
        } catch (IOException | InterruptedException e) {
            Logger.error(TAG, e);
        }
        Logger.trace(TAG, "makeScreenshotFile() FINISH " + (System.currentTimeMillis() - time) + "ms");
        return file;
    }

    private String getImageFilePath(long time) {
        String folderPath = PathsConstants.FEEDBACK;
        StringBuilder sb = new StringBuilder(folderPath);
        sb.append("/");
        sb.append((new SimpleDateFormat("yyyy-MM-dd_HH-m-ss-SSS").format(time)));
        sb.append(".png");
        return sb.toString();
    }

}

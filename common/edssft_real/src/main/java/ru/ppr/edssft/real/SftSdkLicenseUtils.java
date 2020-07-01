package ru.ppr.edssft.real;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import ru.ppr.edssft.LicType;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ShellCommand;

/**
 * Вспомогательные функции для SFT.
 *
 * @author Aleksandr Brazhkin
 */
class SftSdkLicenseUtils {

    private static final String TAG = Logger.makeLogTag(SftSdkLicenseUtils.class);

    /**
     * Проверяет существуют ли файлы лицензий такого типа в папке working.
     */
    static boolean checkLicFileInWorkingFolder(File outDir, EnumSet<LicType> licTypes) {
        boolean isCheckExist = false;
        boolean isSellExist = false;
        File[] files = outDir.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f.getName().toLowerCase().startsWith("local_check_4-")) {
                    isCheckExist = true;
                    Logger.trace(TAG, "найдена лицензия: " + f.getAbsolutePath());
                } else if (f.getName().toLowerCase().startsWith("local_sell_4-")) {
                    isSellExist = true;
                    Logger.trace(TAG, "найдена лицензия: " + f.getAbsolutePath());
                }
            }
        }

        //в зависимости от того файл какой лицензии нас интересует формируем ответ
        isCheckExist = isCheckExist || !licTypes.contains(LicType.CHECK);
        isSellExist = isSellExist || !licTypes.contains(LicType.SELL);

        return isCheckExist && isSellExist;
    }

    /**
     * Создает запрос на получение лицензий.
     *
     * @param utilFile    Утилита SFT
     * @param utilLibFile Библиотека для утилиты SFT
     * @param licTypes    Типы запрашиваемых лицензий
     * @param utilDstDir  Папка, куда положить запросы на получение лицензий
     * @param sdkLicDir   Папка, где расположены рабочие лицензии (working)
     * @return {@code true} при успешном выполнение операции, {@code false} иначе
     */
    static boolean runLicRequestCommand(File utilFile, File utilLibFile, EnumSet<LicType> licTypes, File utilDstDir, File sdkLicDir) {

        String licType;
        if (licTypes.contains(LicType.CHECK) && licTypes.contains(LicType.SELL)) {
            licType = "all";
        } else if (licTypes.contains(LicType.CHECK)) {
            licType = "check";
        } else if (licTypes.contains(LicType.SELL)) {
            licType = "sell";
        } else {
            throw new IllegalArgumentException("EnumSet licTypes is invalid");
        }

        String command = createLicRequestCommand(licType, utilLibFile, utilFile, sdkLicDir, utilDstDir);
        ShellCommand shellCommand = new ShellCommand.Builder(command).build();
        try {
            shellCommand.run();
            Logger.trace(TAG, "runLicRequestCommand output:\n" + shellCommand.getOutput());
            return true;
        } catch (IOException | InterruptedException e) {
            Logger.error(TAG, e);
            return false;
        }
    }

    /**
     * Создает команду для формирования файла с запросом лицензий.
     *
     * @return оманда для запуска в shell
     */
    @NonNull
    private static String createLicRequestCommand(String licType, File utilLibFile, File utilFile, File sdkLicDir, File utilDstDir) {

        StringBuilder sb = new StringBuilder();

        sb.append("LD_LIBRARY_PATH").append("=").append(utilLibFile.getParentFile().getAbsolutePath()).append(":$LD_LIBRARY_PATH");
        sb.append(" ").append(utilFile.getAbsolutePath());
        sb.append(" ").append("--action request");
        sb.append(" ").append("--sdk_lic_folder").append(" ").append(sdkLicDir.getAbsolutePath());
        sb.append(" ").append("--lic_type").append(" ").append(licType);
        sb.append(" ").append("--dst_folder").append(" ").append(utilDstDir.getAbsolutePath());

        return sb.toString();
    }

    /**
     * Выполняет команду загрузки лицензий.
     *
     * @param utilFile    Утилита SFT
     * @param utilLibFile Библиотека для утилиты SFT
     * @param utilSrcDir  Папка, откуда брать лицензии
     * @param sdkLicDir   Папка, где расположены рабочие лицензии (working)
     * @return {@code true} при успешном выполнение операции, {@code false} иначе
     */
    static boolean runTakeLicCommand(File utilFile, File utilLibFile, File utilSrcDir, File sdkLicDir) {

        String command = createTakeLicCommand(utilSrcDir, utilLibFile, utilFile, sdkLicDir);
        ShellCommand shellCommand = new ShellCommand.Builder(command).build();
        try {
            shellCommand.run();
            Logger.trace(TAG, "runTakeLicCommand output:\n" + shellCommand.getOutput());
            return true;
        } catch (IOException | InterruptedException e) {
            Logger.error(TAG, e);
            return false;
        }
    }

    /**
     * Создает команду для заливки лицензий.
     *
     * @return Команда для запуска в shell
     */
    @NonNull
    private static String createTakeLicCommand(File utilSrcDir, File utilLibFile, File utilFile, File sdkLicDir) {

        StringBuilder sb = new StringBuilder();

        sb.append("LD_LIBRARY_PATH").append("=").append(utilLibFile.getParentFile().getAbsolutePath()).append(":$LD_LIBRARY_PATH");
        sb.append(" ").append(utilFile.getAbsolutePath());
        sb.append(" ").append("--action take");
        sb.append(" ").append("--sdk_lic_folder").append(" ").append(sdkLicDir.getAbsolutePath());
        sb.append(" ").append("--src_folder").append(" ").append(utilSrcDir.getAbsolutePath());

        return sb.toString();
    }

}

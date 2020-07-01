package ru.ppr.core.manager.eds;

import android.support.annotation.NonNull;

import java.io.File;

import ru.ppr.logger.Logger;

/**
 * Информация о директориях, с которыми работает sft.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsDirs {

    private static final String TAG = Logger.makeLogTag(EdsDirs.class);

    private static final String DIR_WORKING = "working";
    private static final String DIR_TRANSPORT = "transport";
    private static final String DIR_TRANSPORT_IN = "in";
    private static final String DIR_TRANSPORT_OUT = "out";
    private static final String DIR_UTIL_SRC = "util_src";
    private static final String DIR_UTIL_DST = "util_dst";

    private final File edsDir;

    private final File edsUtilDir;
    private final File edsUtilSrcDir;
    private final File edsUtilDstDir;

    private final File edsWorkingDir;

    private final File edsTransportDir;
    private final File edsTransportInDir;
    private final File edsTransportOutDir;

    public EdsDirs(@NonNull final File edsDir, @NonNull final File edsUtilDir) {

        this.edsDir = edsDir;
        this.edsUtilSrcDir = new File(edsDir, DIR_UTIL_SRC);
        this.edsUtilDstDir = new File(edsDir, DIR_UTIL_DST);

        this.edsWorkingDir = new File(edsDir, DIR_WORKING);

        this.edsTransportDir = new File(edsDir, DIR_TRANSPORT);
        this.edsTransportInDir = new File(edsTransportDir, DIR_TRANSPORT_IN);
        this.edsTransportOutDir = new File(edsTransportDir, DIR_TRANSPORT_OUT);

        this.edsUtilDir = edsUtilDir;
    }

    /**
     * Корневая директория необходимя для работы ЭЦП.
     *
     * @return {@link File} представляющий директорию.
     */
    @NonNull
    public File getEdsDir() {
        return edsDir;
    }

    /**
     * Директория где расположена утилита SFT.
     *
     * @return {@link File} представляющий директорию.
     */
    @NonNull
    public File getEdsUtilDir() {
        return edsUtilDir;
    }

    /**
     * Директория, из которой принимает лицензии утилита SFT.
     *
     * @return {@link File} представляющий директорию.
     */
    @NonNull
    public File getEdsUtilSrcDir() {
        return edsUtilSrcDir;
    }

    /**
     * Директория в которой создает запросы лицензий утилита SFT.
     *
     * @return {@link File} представляющий директорию.
     */
    @NonNull
    public File getEdsUtilDstDir() {
        return edsUtilDstDir;
    }

    /**
     * Директория где расположены рабочие лицензии.
     *
     * @return {@link File} представляющий директорию.
     */
    @NonNull
    public File getEdsWorkingDir() {
        return edsWorkingDir;
    }

    /**
     * Директория где расположены директории необходимые для обмена.
     *
     * @return {@link File} представляющий директорию.
     */
    @NonNull
    public File getEdsTransportDir() {
        return edsTransportDir;
    }

    /**
     * Директория где расположены все ключи ЭЦП.
     *
     * @return {@link File} представляющий директорию.
     */
    @NonNull
    public File getEdsTransportInDir() {
        return edsTransportInDir;
    }

    /**
     * Директория где располагается новый запрос на ключи, если истекает срок действия ключей.
     *
     * @return {@link File} представляющий директорию.
     */
    @NonNull
    public File getEdsTransportOutDir() {
        return edsTransportOutDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EdsDirs edsDirs = (EdsDirs) o;

        if (!edsDir.equals(edsDirs.edsDir)) return false;
        return edsUtilDir.equals(edsDirs.edsUtilDir);
    }

    @Override
    public int hashCode() {
        int result = edsDir.hashCode();
        result = 31 * result + edsUtilDir.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EdsDirs{" +
                "edsDir=" + edsDir +
                ", edsUtilDir=" + edsUtilDir +
                '}';
    }
}

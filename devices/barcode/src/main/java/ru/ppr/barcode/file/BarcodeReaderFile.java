package ru.ppr.barcode.file;

import android.support.annotation.Nullable;

import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.logger.Logger;
import ru.ppr.logger.LoggerAspect;
import ru.ppr.utils.CommonUtils;

/**
 * Реализация считывателя ШК, читающая ШК из файла.
 *
 * @author Artem Ushakov
 */
@LoggerAspect.IncludeClass
public class BarcodeReaderFile implements IBarcodeReader {

    private static final String TAG = Logger.makeLogTag(BarcodeReaderFile.class);

    private final Config config;

    public BarcodeReaderFile(Config config) {
        this.config = config;
    }

    @Override
    @Nullable
    public byte[] scan() {
        File file = getFile();

        byte[] image = null;

        if (file != null) {
            try {
                image = Files.toByteArray(file);
                Logger.info(TAG, CommonUtils.bytesToHexWithSpaces(image));
            } catch (IOException e) {
                Logger.error(TAG, "Error read image for barcode", e);
            }
        }
        return image;
    }

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public void close() {
        /* NOP */
    }

    @Override
    public boolean getFirmwareVersion(StringBuilder stringBuilder) {
        stringBuilder.append("FileBarcode");
        return true;
    }

    @Override
    public boolean getModel(String[] model) {
        model[0] = "FileBarcodeReader";
        return true;
    }

    @Nullable
    private File getFile() {
        final FluentIterable<File> files = Files.fileTreeTraverser().preOrderTraversal(config.getImageDir());
        File outFile = null;
        if (!files.isEmpty()) {
            for (File file : files) {
                if (file.isFile()) {
                    outFile = file;
                }
            }
        } else {
            Logger.info(TAG, "getFile() - Empty image dir");
        }
        return outFile;
    }

    /**
     * Настройки считывателя ШК.
     */
    public static class Config {

        /**
         * Путь до папки с образами.
         */
        private final File imageDir;

        public Config(File imageDir) {
            this.imageDir = imageDir;
        }

        public File getImageDir() {
            return imageDir;
        }

        @Override
        public String toString() {
            return "imageDir=" + imageDir;
        }
    }
}

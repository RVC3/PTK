package ru.ppr.chit.bs.synchronizer.operation;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Single;
import ru.ppr.chit.bs.synchronizer.base.Notifier;
import ru.ppr.chit.bs.synchronizer.base.SynchronizeException;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ZipUtils;

/**
 * Операция извлечения пакета из архива
 *
 * @author Dmitry Nevolin
 */
public class ExtractPacketOperation {

    private static final String TAG = Logger.makeLogTag(ExtractPacketOperation.class);

    private final Params params;
    private final Notifier<String> notifier;

    public ExtractPacketOperation(Params params, Notifier<String> notifier) {
        this.params = params;
        this.notifier = notifier;
    }

    /**
     * @return извлеченный файл пакета
     */
    public Single<Result> rxStart() {
        return Single
                .fromCallable(() -> {
                    try {
                        File extractedFile = new File(params.extractedFileDir, params.extractedFileName);
                        if (params.extractToDir) {
                            if (extractedFile.exists() && !extractedFile.delete()) {
                                throw new Exception("can't delete dir extractedFile: " + extractedFile);
                            }
                            if (!extractedFile.mkdirs()) {
                                throw new Exception("can't create dir extractedFile: " + extractedFile);
                            }
                        }
                        Logger.info(TAG, "start unzip:" +
                                " archiveFile: " + params.archiveFile +
                                " extractedFile: " + extractedFile);
                        if (params.extractToDir) {
                            // При распаковке в папку должен быть tar.gz
                            unpackTarGz(params.archiveFile, extractedFile);
                        } else {
                            // Одиночные файлы просто в gz, смысла собирать их в tar нет
                            unpackGz(params.archiveFile, extractedFile);
                        }
                        return extractedFile;
                    } catch (Exception e) {
                        throw new SynchronizeException(params.operationTitle + ": ошибка извлечения файла из пакета", e);
                    }
                })
                .map(Result::new);
    }

    /**
     * Распаковывает gz архив в указанную папку
     *
     * @param src    архив
     * @param dstDir папка для распаковки
     */
    private void unpackGz(File src, File dstDir) throws Exception {
        String gzipAbsPath = src.getAbsolutePath();
        String unzipAbsPath = dstDir.getAbsolutePath();
        if (!ZipUtils.unpackGZip(gzipAbsPath, unzipAbsPath)) {
            throw new Exception("unpackGZip return false," +
                    " gzipAbsPath: " + gzipAbsPath +
                    " unzipAbsPath: " + unzipAbsPath);
        }
    }

    /**
     * Распаковывает tar.gz архив в указанную папку
     *
     * @param src    архив
     * @param dstDir папка для распаковки
     */
    private void unpackTarGz(File src, File dstDir) throws IOException {
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(src)));
        notifier.notify(params.operationTitle + ": распаковка архива");
        Logger.trace(TAG, "unpack: " + src.getAbsolutePath() + " to: " + dstDir.getAbsolutePath());
        try {
            TarArchiveEntry srcEntry;
            while ((srcEntry = tarArchiveInputStream.getNextTarEntry()) != null) {
                if (srcEntry.isDirectory()) {
                    continue;
                }
                File dst = new File(dstDir, srcEntry.getName());
                if (!dst.getParentFile().exists() && !dst.getParentFile().mkdirs()) {
                    throw new IllegalStateException("failed to create parent dir for dst: " + dst.getParentFile().getAbsolutePath());
                }
                BufferedOutputStream dstOutputStream = new BufferedOutputStream(new FileOutputStream(dst));
                Logger.trace(TAG, "unpack entry: " + srcEntry.getName() + " -> " + dst.getAbsolutePath());
                notifier.notify(String.format(params.operationTitle + ": распаковка файла %s", srcEntry.getName()));
                try {
                    IOUtils.copy(tarArchiveInputStream, dstOutputStream);
                } catch (Exception e) {
                    Logger.error(TAG, e);
                    throw e;
                } finally {
                    dstOutputStream.close();
                }
            }
        } finally {
            tarArchiveInputStream.close();
        }
    }

    public static class Params {

        private final File archiveFile;
        private final File extractedFileDir;
        private final String extractedFileName;
        private final String operationTitle;
        private boolean extractToDir;

        public Params(String operationTitle,
                      File archiveFile,
                      File extractedFileDir,
                      String extractedFileName) {
            this.operationTitle = operationTitle;
            this.archiveFile = archiveFile;
            this.extractedFileDir = extractedFileDir;
            this.extractedFileName = extractedFileName;
        }

        public Params setExtractToDir(boolean extractToDir) {
            this.extractToDir = extractToDir;

            return this;
        }

    }

    public static class Result {

        private final File extractedFile;

        private Result(File extractedFile) {
            this.extractedFile = extractedFile;
        }

        public File getExtractedFile() {
            return extractedFile;
        }

    }

}

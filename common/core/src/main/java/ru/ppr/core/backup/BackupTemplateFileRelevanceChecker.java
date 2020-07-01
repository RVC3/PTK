package ru.ppr.core.backup;

import android.support.annotation.NonNull;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import javax.inject.Inject;

import ru.ppr.logger.Logger;
import ru.ppr.utils.MD5Utils;

/**
 * Проверяет соответствие всех файлов в архиве файла-шаблона к списку файлов по хешу,
 * хеш для архивированных файлов берётся из мета-информации архива.
 * Подробнее про шаблоны см. {@link BackupArchiveCreator.Config#archiveTemplateMap}
 *
 * @author Dmitry Nevolin
 */
public class BackupTemplateFileRelevanceChecker {

    private static final String TAG = Logger.makeLogTag(BackupTemplateFileRelevanceChecker.class);

    @Inject
    BackupTemplateFileRelevanceChecker() {

    }

    /**
     * Выполняет проверку.
     *
     * @param archiveFile    файл архива
     * @param archiveFileMap список файлов для проверки соответствия
     * @return true если соответствует, false в противном случае
     */
    public boolean check(@NonNull File archiveFile, @NonNull Map<String, File> archiveFileMap) throws IOException, NoSuchAlgorithmException {
        ZipFile templateFile;
        try {
            templateFile = new ZipFile(archiveFile);
        } catch (IOException e) {
            //если вывалился Exception значит темплайт битый - скажем наружу что он нерелевантный.
            Logger.error(TAG, e);
            return false;
        }
        Enumeration<ZipArchiveEntry> templateEntries = templateFile.getEntries();
        boolean templateRelevant = true;
        try {
            while (templateEntries.hasMoreElements()) {
                ZipArchiveEntry templateEntry = templateEntries.nextElement();
                File file = archiveFileMap.get(templateEntry.getName());
                if (file != null) {
                    if (!file.exists()) {
                        // http://agile.srvdev.ru/browse/CPPKPP-40009
                        // http://agile.srvdev.ru/browse/CPPKPP-41297
                        // Нормальная ситуация, когда шаблон существует,
                        // а файлов, которые пишутся в данный шаблон - нет.
                        // Так, например, будет, когда ПО установили заново с нуля,
                        // а шаблоны остались от предыдущей версии
                        // Считаем шаблон невалидным.
                        Logger.trace(TAG, "archiveFileMap contains non-existent file: " + file.getAbsolutePath());
                        templateRelevant = false;
                        break;
                    }
                    // Проверка на соответствие папки не папке
                    if (templateEntry.isDirectory() || file.isDirectory()) {
                        if (!templateEntry.isDirectory() || !file.isDirectory()) {
                            throw new IllegalArgumentException("archiveFileMap contains map of dir to non-dir: " +
                                    templateEntry.getName() + " -> " + file.getAbsolutePath());
                        }
                        // Нет смысла считать хеш для папок
                        continue;
                    }
                    byte[] storedHash = templateEntry.getExtra();
                    if (storedHash != null) {
                        byte[] computedHash = MD5Utils.from(file);
                        // При совпадениях просто продолжаем работу, при первом же несовпадении завершаем
                        if (Arrays.equals(storedHash, computedHash)) {
                            Logger.trace(TAG, "hash match for file: " + file.getAbsolutePath());
                        } else {
                            Logger.trace(TAG, "hash NOT match for file: " + file.getAbsolutePath());
                            templateRelevant = false;
                            break;
                        }
                    }
                }
            }
        } finally {
            templateFile.close();
        }

        return templateRelevant;
    }

}

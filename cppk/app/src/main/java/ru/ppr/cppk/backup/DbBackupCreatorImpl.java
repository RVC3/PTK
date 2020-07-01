package ru.ppr.cppk.backup;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ru.ppr.core.backup.BackupArchiveCreator;
import ru.ppr.core.backup.BackupNameGenerator;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.logger.Logger;
import ru.ppr.utils.MtpUtils;

/**
 * Базовый клас, создающий бекап баз данных
 *
 * @author Grigoriy Kashka
 */
abstract class DbBackupCreatorImpl extends PtkBackupCreator implements DbBackupCreator {

    private static final String TAG = Logger.makeLogTag(DbBackupCreatorImpl.class);

    private static final String DBS_ARCHIVE_FILE_LIST_DIR_NAME = "databases";

    private final Context context;

    DbBackupCreatorImpl(BackupArchiveCreator backupArchiveCreator,
                        BackupNameGenerator backupNameGenerator,
                        FilePathProvider pathProvider,
                        Context context,
                        PrivateSettingsHolder privateSettingsHolder) {
        super(backupArchiveCreator, pathProvider, backupNameGenerator, privateSettingsHolder);
        this.context = context;
    }

    /**
     * Начинает создание полной резервной копии
     *
     * @return true если успешно, false в противном случае
     */
    public Pair<Boolean, String> start() {
        File archiveFile = provideArchiveFile();
        try {
            Config config = new Config.Builder()
                    .setArchiveFile(archiveFile)
                    .setArchiveFileListMap(provideArchiveFileListMap())
                    .setArchiveTemplateListMap(provideArchiveTemplateMap())
                    .build();
            create(config);
            // Иначе windows не увидит файл
            MtpUtils.notifyFileCreated(context, archiveFile);
            return Pair.create(true, archiveFile.getAbsolutePath());
        } catch (Exception exception) {
            Logger.error(TAG, exception);
            return Pair.create(false, archiveFile.getAbsolutePath());
        }
    }


    private Map<String, List<File>> provideArchiveFileListMap() {
        return Collections.emptyMap();
    }

    private Map<String, List<File>> provideArchiveTemplateMap() throws InterruptedException, ExecutionException, NoSuchAlgorithmException, IOException {
        File dbTemplate = provideTemplate();
        if (dbTemplate != null) {
            return Collections.singletonMap(DBS_ARCHIVE_FILE_LIST_DIR_NAME,
                    Collections.singletonList(dbTemplate));
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Предоставляет актуальный файл шаблона резервной копии локальной базы
     */
    @Nullable
    abstract File provideTemplate() throws InterruptedException, ExecutionException, NoSuchAlgorithmException, IOException;
}
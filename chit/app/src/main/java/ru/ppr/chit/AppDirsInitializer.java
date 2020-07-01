package ru.ppr.chit;

import javax.inject.Inject;

import ru.ppr.chit.helpers.FilePathProvider;

/**
 * Занимается инициализацией папок.
 * При старте приложения, как правило не все папки приложения используются,
 * но некоторые из них нужны, данный класс создаёт все недостающие папки.
 *
 * @author Dmitry Nevolin
 */
public class AppDirsInitializer {

    private final FilePathProvider filePathProvider;

    @Inject
    AppDirsInitializer(FilePathProvider filePathProvider) {
        this.filePathProvider = filePathProvider;
    }

    /**
     * Запускает инициализацию папок, добавлять новые по мере неоходимости.
     */
    public void init() {
        filePathProvider.getRestoreBackupDir();
    }

}

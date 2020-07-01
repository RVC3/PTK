package ru.ppr.chit.bs.synchronizer.operation;

import java.io.File;
import java.io.IOException;

import ru.ppr.utils.FileUtils2;

/**
 * Операция очистки папки от всех файлов
 *
 * @author Dmitry Nevolin
 */
public class ClearDirOperation {

    private final Params params;

    public ClearDirOperation(Params params) {
        this.params = params;
    }

    public void start() throws IOException {
        FileUtils2.clearDir(params.targetDir, null);
    }

    public static class Params {

        private final File targetDir;

        public Params(File targetDir) {
            this.targetDir = targetDir;
        }

    }

    // Очищает транспортный каталог пакета
    public static void clearDir(File directory) throws IOException {
        new ClearDirOperation(new ClearDirOperation.Params(directory)).start();
    }


}

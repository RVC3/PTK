package ru.ppr.chit.bs.synchronizer.operation;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import io.reactivex.Single;

/**
 * Операция перемещения файла в папку
 *
 * @author Dmitry Nevolin
 */
public class MoveFileOperation {

    private final Params params;

    public MoveFileOperation(Params params) {
        this.params = params;
    }

    /**
     * @return перемещенный файл
     */
    public Result start() throws Exception {
        File targetFile = new File(params.targetDir, params.targetFile.getName());
        if (params.overrideExistentFile && targetFile.exists() && !targetFile.delete()) {
            throw new Exception("Can't delete targetFile: " + targetFile);
        }
        FileUtils.moveFileToDirectory(params.targetFile, params.targetDir, false);
        return new Result(targetFile);
    }

    /**
     * @return перемещенный файл
     */
    public Single<Result> rxStart() {
        return Single.fromCallable(this::start);
    }

    public static class Params {

        private final File targetFile;
        private final File targetDir;
        /**
         * Признак перезаписи файла назначения (если существует)
         */
        private boolean overrideExistentFile = false;

        public Params(File targetFile, File targetDir) {
            this.targetFile = targetFile;
            this.targetDir = targetDir;
        }

        public Params setOverrideExistentFile(boolean overrideExistentFile) {
            this.overrideExistentFile = overrideExistentFile;

            return this;
        }

    }

    public static class Result {

        private final File movedFile;

        private Result(File movedFile) {
            this.movedFile = movedFile;
        }

        public File getMovedFile() {
            return movedFile;
        }

    }

}

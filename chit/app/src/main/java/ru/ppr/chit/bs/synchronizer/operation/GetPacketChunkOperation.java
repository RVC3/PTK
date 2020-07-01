package ru.ppr.chit.bs.synchronizer.operation;

import android.util.Base64;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.UUID;

import io.reactivex.Single;
import ru.ppr.chit.api.Api;
import ru.ppr.chit.api.request.GetPacketChunkRequest;
import ru.ppr.logger.Logger;

/**
 * Операция закачки пакета
 *
 * @author Dmitry Nevolin
 */
public class GetPacketChunkOperation {

    private static final String TAG = Logger.makeLogTag(GetPacketChunkOperation.class);

    private final Api api;
    private final Params params;

    public GetPacketChunkOperation(Api api, Params params) {
        this.api = api;
        this.params = params;
    }

    /**
     * @return закаченный файл пакета
     */
    public Single<Result> rxStart() {
        // Идём на сервер за файлом
        return Single
                .fromCallable(() -> {
                    GetPacketChunkRequest request = new GetPacketChunkRequest();
                    request.setRequestId(params.requestId);
                    request.setPacketOffset(params.packetOffset);
                    request.setChunkLength(params.chunkLength);
                    return request;
                })
                .flatMap(api::getPacketChunk)
                .map(inputStream -> {
                    File packetFile = new File(params.packetFileDir, params.packetFileName + "." + params.packetFileExt);
                    FileUtils.copyInputStreamToFile(inputStream, packetFile);
                    return packetFile;
                })
                .flatMap(packetFile -> {
                    String hash = Base64.encodeToString(Files.hash(packetFile, Hashing.md5()).asBytes(), Base64.NO_WRAP);
                    // Сверяем хеш
                    if (hash.equals(params.packetFileHash)) {
                        return Single.just(new Result(packetFile));
                    } else {
                        return Single.error(new Exception("Hash not match, expected hash: " + params.packetFileHash + "; computed hash: " + hash));
                    }
                });
    }

    public static class Params {

        private final UUID requestId;
        private final long packetOffset;
        private final long chunkLength;
        private final File packetFileDir;
        private final String packetFileName;
        private final String packetFileExt;
        private final String packetFileHash;

        public Params(UUID requestId,
                      long packetOffset,
                      long chunkLength,
                      File packetFileDir,
                      String packetFileName,
                      String packetFileExt,
                      String packetFileHash) {
            this.requestId = requestId;
            this.packetOffset = packetOffset;
            this.chunkLength = chunkLength;
            this.packetFileDir = packetFileDir;
            this.packetFileName = packetFileName;
            this.packetFileExt = packetFileExt;
            this.packetFileHash = packetFileHash;
        }

    }

    public static class Result {

        private final File packetFile;

        private Result(File packetFile) {
            this.packetFile = packetFile;
        }

        public File getPacketFile() {
            return packetFile;
        }

    }

}

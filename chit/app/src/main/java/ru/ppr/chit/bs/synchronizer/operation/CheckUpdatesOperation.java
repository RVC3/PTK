package ru.ppr.chit.bs.synchronizer.operation;

import java.util.UUID;

import io.reactivex.Single;
import ru.ppr.chit.api.Api;
import ru.ppr.chit.api.entity.ErrorEntity;
import ru.ppr.chit.api.entity.PacketStatusEntity;
import ru.ppr.chit.api.request.GetPacketStatusRequest;

/**
 * Операция проверки обновления
 *
 * @author Dmitry Nevolin
 */
public class CheckUpdatesOperation {

    /**
     * Такой UUID приходит, если не нужны обновления
     */
    private static final UUID NO_UPDATES_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final Api api;
    private final Params params;

    public CheckUpdatesOperation(Api api, Params params) {
        this.api = api;
        this.params = params;
    }

    /**
     * @return результат проверки
     */
    public Single<Result> rxStart() {
        // Если обновления не нужны - сразу отдаём результат
        if (NO_UPDATES_UUID.equals(params.requestId)) {
            return Single.just(new Result(params.requestId));
        } else {
            // Иначе лезем на сервер
            return Single
                    .fromCallable(() -> {
                        GetPacketStatusRequest request = new GetPacketStatusRequest();
                        request.setRequestId(params.requestId);
                        return request;
                    })
                    .flatMap(api::getPacketStatus)
                    .map(response -> new Result(
                            params.requestId,
                            response.getPacketStatus(),
                            response.getPacketHash(),
                            response.getPacketLength(),
                            response.getError())
                    );
        }
    }

    public static class Params {

        private final UUID requestId;

        public Params(UUID requestId) {
            this.requestId = requestId;
        }

    }

    public static class Result {

        private final boolean hasUpdates;
        private final UUID requestId;
        private final PacketStatusEntity packetStatus;
        private final String packetHash;
        private final Long packetLength;
        private final ErrorEntity error;

        private Result(UUID requestId) {
            this.hasUpdates = false;
            this.requestId = requestId;
            this.packetStatus = PacketStatusEntity.READY;
            this.packetHash = null;
            this.packetLength = null;
            this.error = null;
        }

        private Result(UUID requestId, PacketStatusEntity packetStatus, String packetHash, long packetLength, ErrorEntity error) {
            this.hasUpdates = packetStatus != PacketStatusEntity.PENDING_DATA ? true : false;
            this.requestId = requestId;
            this.packetStatus = packetStatus;
            this.packetHash = packetHash;
            this.packetLength = packetLength;
            if (error != null) {
                this.error = new ErrorEntity(error.getCode(), error.getDescription());
            } else {
                this.error = null;
            }
        }

        /**
         * @return признак что по запросу есть новые данные
         */
        public boolean hasUpdates() {
            return hasUpdates;
        }

        /**
         * @return requestId переданный в params
         */
        public UUID getRequestId() {
            return requestId;
        }

        /**
         * @return READY если hasUpdates() = false, значение в противном случае
         */
        public PacketStatusEntity getPacketStatus() {
            return packetStatus;
        }

        /**
         * @return null если hasUpdates() = false, значение в противном случае
         */
        public String getPacketHash() {
            return packetHash;
        }

        /**
         * @return null если hasUpdates() = false, значение в противном случае
         */
        public Long getPacketLength() {
            return packetLength;
        }

        public final ErrorEntity getError() {
            return error;
        }

    }

}

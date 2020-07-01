package ru.ppr.chit.api.response;

import java.util.UUID;

/**
 * Базовый класс для ответов по запросам синхронизации
 * @author m.sidorov
 */

public class BasePrepareSyncResponse extends BaseResponse {

    /**
     * Идентификатор пакета, если UUID = 00000000-0000-0000-0000-000000000000, то обновление не требуется
     */
    private UUID requestId;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

}

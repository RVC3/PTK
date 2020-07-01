package ru.ppr.chit.api.response;

import ru.ppr.chit.api.entity.ErrorEntity;

/**
 * @author Dmitry Nevolin
 */
public class BaseResponse {

    /**
     * Если нет ошибок, то null
     */
    private ErrorEntity error;

    public final ErrorEntity getError() {
        return error;
    }

    public final void setError(ErrorEntity error) {
        this.error = error;
    }

}

package ru.ppr.core.exceptions;

/**
 * Специальный тип ошибки для отображения критических ошибок
 *
 * @author m.sidorov
 */
public class UserCriticalException extends RuntimeException implements UserThrowable {

    public UserCriticalException(String message) {
        super(message);
    }

    public UserCriticalException(String message, Throwable cause){
        super(message, cause);
    }

}

package ru.ppr.chit.bs.synchronizer.base;

import ru.ppr.core.exceptions.UserException;

/**
 * Класс ошибки синхронизации
 *
 * @author m.sidorov
 */
public class SynchronizeException extends UserException {

    public SynchronizeException(String message) {
        super(message);
    }

    public SynchronizeException(String message, Throwable e) {
        super(message, e);
    }

}

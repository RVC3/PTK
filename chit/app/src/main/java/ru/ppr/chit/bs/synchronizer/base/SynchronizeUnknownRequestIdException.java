package ru.ppr.chit.bs.synchronizer.base;

/**
 * Специальный класс ошибки, говорящий, что на сервере нет новых данных
 *
 * @author m.sidorov
 */
public class SynchronizeUnknownRequestIdException extends SynchronizeException {

    public SynchronizeUnknownRequestIdException(String message) {
        super(message);
    }

}

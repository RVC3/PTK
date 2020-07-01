package ru.ppr.chit.bs.synchronizer.base;

/**
 * Специальный класс ошибки, говорящий, что на сервере нет новых данных
 *
 * @author m.sidorov
 */
public class SynchronizeNoDataException extends SynchronizeException {

    public SynchronizeNoDataException(String message) {
        super(message);
    }

}

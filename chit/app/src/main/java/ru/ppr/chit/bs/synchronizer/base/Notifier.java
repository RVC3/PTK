package ru.ppr.chit.bs.synchronizer.base;

/**
 * Интерфейс уведомления чего либо
 *
 * @author m.sidorov
 */
public interface Notifier<T> {

    void notify(T event);

}


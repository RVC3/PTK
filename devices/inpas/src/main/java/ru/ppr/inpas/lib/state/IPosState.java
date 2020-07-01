package ru.ppr.inpas.lib.state;

/**
 * Поддерживаемые состояния на терминале.
 */
public interface IPosState {

    enum PosState {
        UNKNOWN,
        DONE,
        SEND,
        RECEIVE,
        PARSE,
        EXECUTE,
        WAIT,
        ERROR
    }

    void doAction();

}
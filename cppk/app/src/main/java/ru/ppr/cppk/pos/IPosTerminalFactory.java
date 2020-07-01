package ru.ppr.cppk.pos;

import android.support.annotation.NonNull;

import ru.ppr.ipos.IPos;

/**
 * Интерфейс для фабрики POS терминала.
 */
public interface IPosTerminalFactory {

    /**
     * С помощью данного метода можно получить конкретный экземпляр POS терминала.
     *
     * @param posType тип POS терминала.
     * @param mac     MAC адрес POS терминала.
     * @return конкретный экземпляр POS терминала.
     */
    IPos getPosTerminal(@NonNull final PosType posType, @NonNull final String mac);

}
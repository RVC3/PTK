package ru.ppr.chit.domain.boarding;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.logger.Logger;

/**
 * Класс для проверки {@link BoardingEvent.Status}.
 *
 * @author Aleksandr Brazhkin
 */
public class BoardingStatusChecker {

    private static final String TAG = Logger.makeLogTag(BoardingStatusChecker.class);

    @Inject
    BoardingStatusChecker() {

    }

    /**
     * Проверяет на основании события, производится ли посадка.
     *
     * @param boardingEvent Событие обслуживания поезки
     * @return {@code true} если обслуживание начато, {@code false} - иначе
     */
    public boolean isStarted(@Nullable BoardingEvent boardingEvent) {
        boolean isStarted = boardingEvent != null && boardingEvent.getStatus() == BoardingEvent.Status.STARTED;
        Logger.trace(TAG, "isStarted: " + isStarted);
        return isStarted;
    }
}

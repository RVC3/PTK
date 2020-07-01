package ru.ppr.chit.domain.tripservice;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.logger.Logger;

/**
 * Класс для проверки {@link TripServiceEvent.Status}.
 *
 * @author Aleksandr Brazhkin
 */
public class TripServiceStatusChecker {

    private static final String TAG = Logger.makeLogTag(TripServiceStatusChecker.class);

    @Inject
    TripServiceStatusChecker() {

    }

    /**
     * Проверяет на основании события, производится ли обслуживание поезкди.
     *
     * @param tripServiceEvent Событие обслуживания поезки
     * @return {@code true} если обслуживание начато, {@code false} - иначе
     */
    public boolean isStarted(@Nullable TripServiceEvent tripServiceEvent) {
        boolean isStarted = tripServiceEvent != null
                && (tripServiceEvent.getStatus() == TripServiceEvent.Status.STARTED
                || tripServiceEvent.getStatus() == TripServiceEvent.Status.TRANSFERRED);
        Logger.trace(TAG, "isStarted: " + isStarted);
        return isStarted;
    }
}

package ru.ppr.chit.domain.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.EnumSet;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.model.nsi.Version;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.domain.repository.nsi.VersionRepository;

/**
 * Предоставляет версию базы НСИ
 *
 * @author Dmitry Nevolin
 */
public class NsiVersionProvider {

    /**
     * Начальная версия базы НСИ, используется когда мы не можем
     * получить текущую версию базы НСИ из-за отсутствия самой базы/записей в таблице
     */
    private static final int INITIAL_NSI_VERSION = 0;

    private final EnumSet<Version.Status> testStatusSet = EnumSet.of(Version.Status.READY_TO_TEST, Version.Status.READY_TO_CHECK);
    private final EnumSet<Version.Status> releaseStatusSet = EnumSet.of(Version.Status.READY_TO_DEPLOY);
    private final VersionRepository versionRepository;
    private final TripServiceEventRepository tripServiceEventRepository;

    @Inject
    NsiVersionProvider(VersionRepository versionRepository, TripServiceEventRepository tripServiceEventRepository) {
        this.versionRepository = versionRepository;
        this.tripServiceEventRepository = tripServiceEventRepository;
    }

    public int getCurrentNsiVersion() {
        Version version = getNsiVersionModelForDate(getDateByTripUuid(null));
        return version != null ? version.getVersionId() : INITIAL_NSI_VERSION;
    }

    public Version getCurrentNsiVersionModel() {
        return getNsiVersionModelForDate(getDateByTripUuid(null));
    }

    public int getNsiVersionForDate(@NonNull Date date) {
        Version version = getNsiVersionModelForDate(date);
        return version != null ? version.getVersionId() : INITIAL_NSI_VERSION;
    }

    public Version getNsiVersionModelForDate(@NonNull Date date) {
        // В будущем добавить возможность изменения сета
        return versionRepository.loadForDate(date, releaseStatusSet);
    }

    @NonNull
    private Date getDateByTripUuid(@Nullable String tripUuid) {
        TripServiceEvent tripServiceEvent = null;
        if (tripUuid == null) {
            TripServiceEvent currentTripServiceEvent = tripServiceEventRepository.loadLast();
            if (currentTripServiceEvent != null) {
                switch (currentTripServiceEvent.getStatus()) {
                    case STARTED:
                        tripServiceEvent = currentTripServiceEvent;
                        break;
                    case TRANSFERRED:
                        tripServiceEvent = tripServiceEventRepository.loadFirstByTripUuid(currentTripServiceEvent.getTripUuid());
                        break;
                }
            }
        } else {
            tripServiceEvent = tripServiceEventRepository.loadFirstByTripUuid(tripUuid);
        }
        return tripServiceEvent != null ? tripServiceEvent.getStartTime() : new Date();
    }

}

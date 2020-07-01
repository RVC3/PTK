package ru.ppr.cppk.managers;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.nsi.entity.Version;
import ru.ppr.nsi.repository.VersionRepository;

/**
 * @author Dmitry Nevolin
 */
public class NsiVersionManager {

    private final VersionRepository versionRepository;
    private final Holder<LocalDaoSession> localDaoSession;
    private final CommonSettingsStorage commonSettingsStorage;

    public NsiVersionManager(@NonNull VersionRepository versionRepository,
                             @NonNull Holder<LocalDaoSession> localDaoSession,
                             @NonNull CommonSettingsStorage commonSettingsStorage) {
        this.versionRepository = versionRepository;
        this.localDaoSession = localDaoSession;
        this.commonSettingsStorage = commonSettingsStorage;
    }

    @Nullable
    public Version getNsiVersionForShift(@NonNull String shiftId) {
        return versionRepository.getVersionForDate(getNviVersionDate(shiftId), getNsiStatuses());
    }

    public int getNsiVersionIdForShift(@NonNull String shiftId) {
        return versionRepository.getVersionIdForDate(getNviVersionDate(shiftId), getNsiStatuses());
    }

    @Nullable
    public Version getCurrentNsiVersion() {
        return versionRepository.getVersionForDate(getNviVersionDate(null), getNsiStatuses());
    }

    public int getCurrentNsiVersionId() {
        return versionRepository.getVersionIdForDate(getNviVersionDate(null), getNsiStatuses());
    }

    public int getMaxNsiVersionId() {
        return versionRepository.getMaxVersionId();
    }

    @SuppressLint("WrongConstant")
    @Nullable
    public Version getNsiVersionForDate(@NonNull Date date) {
        //https://aj.srvdev.ru/browse/CPPKPP-28149
        int[] nsiStatuses = commonSettingsStorage.get().isSelectDraftNsi() ? Version.testStatuses : Version.releaseStatuses;
        return versionRepository.getVersionForDate(date, nsiStatuses);
    }

    public int getNsiVersionIdForDate(@NonNull Date date) {
        return versionRepository.getVersionIdForDate(date, getNsiStatuses());
    }

    @Version.Status
    private int[] getNsiStatuses() {
        //https://aj.srvdev.ru/browse/CPPKPP-28149
        return commonSettingsStorage.get().isSelectDraftNsi() ? Version.testStatuses : Version.releaseStatuses;
    }

    /**
     * Проверяет корректна ли текущая версия НСИ на ПТК
     */
    public boolean checkCurrentVersionIdValid() {
        return getCurrentNsiVersionId() != -1;
    }

    public Date getCriticalNsiChangeDate() {
        Date criticalNsiChangeDate = null;
        Version nextNsiVersion = getNextNsiVersion();

        if (nextNsiVersion != null && nextNsiVersion.getIsCriticalChange()) {
            criticalNsiChangeDate = nextNsiVersion.getStartedDateTime();
        }

        return criticalNsiChangeDate;
    }

    @Nullable
    private Version getNextNsiVersion() {
        Version nextNsiVersion = null;

        if (checkCurrentVersionIdValid()) {
            nextNsiVersion = versionRepository.getVersionById(getCurrentNsiVersionId() + 1);
        }

        return nextNsiVersion;
    }

    @Nullable
    private Date getNviVersionDate(@Nullable String shiftId) {
        ShiftEvent startShiftEvent = null;

        if (shiftId == null) {
            ShiftEvent currentShiftEvent = localDaoSession.get().getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            boolean isShiftOpened = currentShiftEvent != null && currentShiftEvent.getStatus() != ShiftEvent.Status.ENDED;

            if (isShiftOpened) {
                switch (currentShiftEvent.getStatus()) {
                    case STARTED: {
                        startShiftEvent = currentShiftEvent;
                        break;
                    }
                    case TRANSFERRED: {
                        startShiftEvent = localDaoSession.get().getShiftEventDao().getFirstShiftEventByShiftId(currentShiftEvent.getShiftId(), ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
                        break;
                    }
                }
            }
        } else {
            startShiftEvent = localDaoSession.get().getShiftEventDao().getFirstShiftEventByShiftId(shiftId, ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        }

        return startShiftEvent != null ? startShiftEvent.getStartTime() : new Date();
    }

}

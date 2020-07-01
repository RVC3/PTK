package ru.ppr.cppk.pd.utils;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.CppkTicketControlsDao;
import ru.ppr.cppk.entity.event.base34.CPPKTicketControl;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.creator.EventCreator;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.CountTripsPdControlData;

/**
 * Класс, выполняющий сборку {@link CPPKTicketControl} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketControlEventCreator {

    private final EventCreator eventCreator;
    private final PtkModeChecker ptkModeChecker;
    private final PrivateSettings privateSettings;
    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;

    private PD pd;
    private PassageResult passageResult;
    private TicketEventBase ticketEventBase;
    private int smartCardStopListReasonCode;
    private CountTripsPdControlData countTripsPdControlData;
    private Globals app;

    @Inject
    TicketControlEventCreator(EventCreator eventCreator,
                              PtkModeChecker ptkModeChecker,
                              PrivateSettings privateSettings,
                              LocalDaoSession localDaoSession,
                              LocalDbTransaction localDbTransaction,
                              Globals app) {
        this.eventCreator = eventCreator;
        this.ptkModeChecker = ptkModeChecker;
        this.privateSettings = privateSettings;
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
        this.app = app;
    }

    public TicketControlEventCreator setPd(@NonNull PD pd) {
        this.pd = pd;
        return this;
    }

    public TicketControlEventCreator setPassageResult(@NonNull PassageResult passageResult) {
        this.passageResult = passageResult;
        return this;
    }

    public TicketControlEventCreator setTicketEventBase(@NonNull TicketEventBase ticketEventBase) {
        this.ticketEventBase = ticketEventBase;
        return this;
    }

    public TicketControlEventCreator setSmartCardStopListReasonCode(int smartCardStopListReasonCode) {
        this.smartCardStopListReasonCode = smartCardStopListReasonCode;
        return this;
    }

    public TicketControlEventCreator setCountTripsPdControlData(CountTripsPdControlData countTripsPdControlData) {
        this.countTripsPdControlData = countTripsPdControlData;
        return this;
    }

    /**
     * Выполнят сборку {@link CPPKTicketControl} и запись его в БД.
     *
     * @return Сформированный {@link CPPKTicketControl}
     */
    @NonNull
    public CPPKTicketControl create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    public CPPKTicketControl createInternal() {
        Preconditions.checkNotNull(pd);
        Preconditions.checkNotNull(passageResult);
        Preconditions.checkNotNull(ticketEventBase);

        // Пишем в БД Event
        Event event = eventCreator.create();

        // Пишем в БД ticketEventBase
        localDaoSession.getTicketEventBaseDao().insertOrThrow(ticketEventBase);

        ParentTicketInfo parentTicketInfo = pd.parentTicketInfo;
        if (parentTicketInfo != null) {
            // Пишем в БД ParentTicketInfo
            localDaoSession.getParentTicketInfoDao().insertOrThrow(parentTicketInfo);
        }

        CPPKTicketControl ticketControlEvent = new CPPKTicketControl();
        ticketControlEvent.setEventId(event.getId());
        ticketControlEvent.setParentTicketInfoId(parentTicketInfo == null ? -1 : parentTicketInfo.getId());
        ticketControlEvent.setTicketEventBaseId(ticketEventBase.getId());
        ticketControlEvent.setControlTimestamp(event.getCreationTimestamp());
        ticketControlEvent.setPassageResult(passageResult);
        ticketControlEvent.setTicketNumber(pd.numberPD);
        ticketControlEvent.setEdsKeyNumber(pd.ecpNumberPD);
        ticketControlEvent.setRevokedEds(pd.isRevokedEcp());
        ticketControlEvent.setExemptionCode(pd.exemptionCode);
        ticketControlEvent.setStopListId(smartCardStopListReasonCode);
        ticketControlEvent.setSellTicketDeviceId(pd.deviceId);
        ticketControlEvent.setRestoredTicket(pd.isRestoredTicket());

        if (countTripsPdControlData != null) {
            ticketControlEvent.setTripsSpend(countTripsPdControlData.isTripsCountDecremented() ? 1 : 0);
            ticketControlEvent.setTrips7000Spend(countTripsPdControlData.isTrips7000CountDecremented() ? 1 : 0);
            ticketControlEvent.setTripsCount(countTripsPdControlData.getAvailableTripsCount());
            ticketControlEvent.setTrips7000Count(countTripsPdControlData.getAvailableTrips7000Count());
        }

        if (ptkModeChecker.isTransferControlMode()) {
            ticketControlEvent.setTransferDeparturePoint(getWorkingDepartureStation(privateSettings));
            ticketControlEvent.setTransferDestinationPoint(getWorkingDestinationStation(privateSettings));

            Date transferDepartureDateTime = SharedPreferencesUtils.getTransferDepartureDateTime(app);
            ticketControlEvent.setTransferDepartureDateTime(transferDepartureDateTime);
        }

        // Пишем в БД TicketControlEvent
        CppkTicketControlsDao controlsDao = localDaoSession.getCppkTicketControlsDao();
        controlsDao.insertOrThrow(ticketControlEvent);

        return ticketControlEvent;
    }

    @NonNull
    private static Long getWorkingDepartureStation(@NonNull PrivateSettings privateSettings) {
        return privateSettings.getTransferRouteStationsCodes()[0];
    }

    @NonNull
    private static Long getWorkingDestinationStation(@NonNull PrivateSettings privateSettings) {
        return privateSettings.getTransferRouteStationsCodes()[1];
    }

}

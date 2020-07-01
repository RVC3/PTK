package ru.ppr.cppk.pd.check.control;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.dataCarrier.pd.check.control.StrategyCheck;
import ru.ppr.cppk.logic.SmartCardStopListChecker;
import ru.ppr.cppk.logic.pd.checker.BeginDateChecker;
import ru.ppr.cppk.logic.pd.checker.ServiceFeeEndDateChecker;
import ru.ppr.cppk.logic.pd.checker.TicketStopListItemChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.ServiceFee;
import ru.ppr.nsi.repository.ServiceFeeRepository;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.security.entity.StopCriteriaType;

/**
 * Валидатор ПД-услуг.
 *
 * @author Dmitry Nevolin
 */
public class ServiceFeePdStrategyCheck implements StrategyCheck {

    private static final String TAG = Logger.makeLogTag(ServiceFeePdStrategyCheck.class);

    private final NsiVersionManager nsiVersionManager;
    private final ServiceFeeRepository serviceFeeRepository;
    private final BeginDateChecker beginDateChecker;
    private final ServiceFeeEndDateChecker endDateChecker;
    private final TicketStopListItemChecker ticketStopListItemChecker;
    private final SmartCardStopListChecker smartCardStopListChecker;
    private final PdVersionChecker pdVersionChecker;

    @Inject
    ServiceFeePdStrategyCheck(NsiVersionManager nsiVersionManager,
                              ServiceFeeRepository serviceFeeRepository,
                              BeginDateChecker beginDateChecker,
                              ServiceFeeEndDateChecker endDateChecker,
                              TicketStopListItemChecker ticketStopListItemChecker,
                              SmartCardStopListChecker smartCardStopListChecker,
                              PdVersionChecker pdVersionChecker) {
        this.nsiVersionManager = nsiVersionManager;
        this.serviceFeeRepository = serviceFeeRepository;
        this.beginDateChecker = beginDateChecker;
        this.endDateChecker = endDateChecker;
        this.ticketStopListItemChecker = ticketStopListItemChecker;
        this.smartCardStopListChecker = smartCardStopListChecker;
        this.pdVersionChecker = pdVersionChecker;
    }

    @Override
    public List<PassageResult> execCheck(PD pd) {
        // Проверяем версию ПД
        // Если это НЕ билет-услуга, значит нам передали заведомо кривой ПД
        PdVersion pdVersion = PdVersion.getByCode(pd.versionPD);
        Preconditions.checkNotNull(pdVersion);
        if (!pdVersionChecker.isServiceFeeTicket(pdVersion)) {
            throw new IllegalArgumentException("Pd version != PdVersion.V21");
        }

        /*
         * errors это контейнер для хранения ошибок, выявленных при проверке ПД
         * в случае если ошибок не выявлено, то данный контейнер остается пустым,
         * что в дальнейшем будет свидетельствовать об отсутствие ошибок
         */
        List<PassageResult> errors = new ArrayList<>();
        int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

        // Получаем услугу из НСИ
        ServiceFee serviceFee = serviceFeeRepository.load(pd.serviceFeeCode, nsiVersion);
        if (serviceFee == null) {
            Logger.trace(TAG, "serviceFee is null");
            // Для услуг специальных ошибок не предусмотрено, ворвзащаем максимально близкую по смыслу
            errors.add(PassageResult.TariffNotFound);
            return errors;
        }

        // Проверяем дату начала действия услуги
        Date startPdTime = new Date(pd.getSaleDate().getTime() + TimeUnit.DAYS.toMillis(pd.term));
        if (!checkStartDate(startPdTime)) {
            Logger.trace(TAG, "checkBeginDate() is false");
            errors.add(PassageResult.TooEarly);
        }

        // Проверяем дату окончания действия услуги
        if (!checkEndDate(startPdTime, serviceFee)) {
            Logger.trace(TAG, "checkEndDate() is false");
            errors.add(PassageResult.TooLate);
        }

        // Проверяем услуги по стоплисту билетов
        if (ticketStopListItemChecker.check(pd.getTicketId())) {
            Logger.trace(TAG, "ticketStopListItemChecker.check() is true");
            errors.add(PassageResult.BannedByStopListTickets);
        }

        // Проверяем карту по стоп-листу
        if (!checkCardInStopList(pd, nsiVersion)) {
            Logger.trace(TAG, "checkCardInStopList() is false");
            errors.add(PassageResult.BannedByStopListCards);
        }

        return errors;
    }

    /**
     * Проверяет дату начала действия ПД.
     *
     * @param pdStartDate Дата начала действия ПД
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    private boolean checkStartDate(@NonNull Date pdStartDate) {
        return beginDateChecker.check(pdStartDate);
    }

    /**
     * Проверяет дату окончания действия для услуги
     *
     * @param startPdTime время начала действия
     * @param serviceFee  услуга
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    private boolean checkEndDate(@NonNull Date startPdTime, @NonNull ServiceFee serviceFee) {
        return endDateChecker.check(startPdTime, serviceFee);
    }

    /**
     * Проверяет карту по стоп-листу.
     *
     * @param pd         ПД
     * @param nsiVersion Версия НСИ
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    private boolean checkCardInStopList(PD pd, int nsiVersion) {
        BscInformation bscInformation = pd.getBscInformation();
        if (bscInformation != null) {
            // Если у билета есть информация о карте
            Pair<SmartCardStopListItem, String> stopItemResult = smartCardStopListChecker.findSmartCardStopListItem(
                    bscInformation.getSmartCardTypeBsc(),
                    bscInformation.getOuterNumberString(),
                    bscInformation.getCrustalSerialNumberString(),
                    EnumSet.of(StopCriteriaType.READ_AND_WRITE),
                    nsiVersion);

            return stopItemResult == null;
        } else {
            // Услуга может быть записана только на карту
            throw new IllegalArgumentException("Service fee must have bsc information");
        }
    }

}

package ru.ppr.cppk.pd.check.control;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.cppk.dataCarrier.PdFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.entity.PassageMark;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.dataCarrier.pd.check.control.StrategyCheck;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.SmartCardStopListChecker;
import ru.ppr.cppk.logic.TransferPdChecker;
import ru.ppr.cppk.logic.pd.checker.BeginDateChecker;
import ru.ppr.cppk.logic.pd.checker.EndDateChecker;
import ru.ppr.cppk.logic.pd.checker.PdLastPassageChecker;
import ru.ppr.cppk.logic.pd.checker.RevokedPdChecker;
import ru.ppr.cppk.logic.pd.checker.SeasonForDaysTicketForControlChecker;
import ru.ppr.cppk.logic.pd.checker.SeasonTicketCountTripChecker;
import ru.ppr.cppk.logic.pd.checker.SeasonTicketWeekendDaysChecker;
import ru.ppr.cppk.logic.pd.checker.SeasonTicketWorkingDaysChecker;
import ru.ppr.cppk.logic.pd.checker.TicketStopListItemChecker;
import ru.ppr.cppk.logic.pd.checker.TrainCategoryChecker;
import ru.ppr.cppk.logic.pd.checker.TransferRouteChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.security.entity.StopCriteriaType;

/**
 * Общая реализация валидатора ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class StrategyCheckImpl implements StrategyCheck {

    private static final String TAG = Logger.makeLogTag(StrategyCheckImpl.class);

    private final PtkModeChecker ptkModeChecker;
    private final NsiDaoSession nsiDaoSession;
    private final NsiVersionManager nsiVersionManager;
    private final BeginDateChecker beginDateChecker;
    private final EndDateChecker endDateChecker;
    private final RevokedPdChecker revokedPdChecker;
    private final TrainCategoryChecker trainCategoryChecker;
    private final TicketStopListItemChecker ticketStopListItemChecker;
    private final SmartCardStopListChecker smartCardStopListChecker;
    private final SeasonTicketWeekendDaysChecker seasonTicketWeekendDaysChecker;
    private final SeasonTicketWorkingDaysChecker seasonTicketWorkingDaysChecker;
    private final SeasonTicketCountTripChecker seasonTicketCountTripChecker;
    private final SeasonForDaysTicketForControlChecker seasonForDaysTicketForControlChecker;
    private final TransferRouteChecker transferRouteChecker;
    private final TransferPdChecker transferPdChecker;
    private final PdLastPassageChecker pdLastPassageChecker;
    private final TicketCategoryChecker ticketCategoryChecker;

    @Inject
    StrategyCheckImpl(PtkModeChecker ptkModeChecker,
                      NsiDaoSession nsiDaoSession,
                      NsiVersionManager nsiVersionManager,
                      BeginDateChecker beginDateChecker,
                      EndDateChecker endDateChecker,
                      RevokedPdChecker revokedPdChecker,
                      TrainCategoryChecker trainCategoryChecker,
                      TicketStopListItemChecker ticketStopListItemChecker,
                      SmartCardStopListChecker smartCardStopListChecker,
                      SeasonTicketWeekendDaysChecker seasonTicketWeekendDaysChecker,
                      SeasonTicketWorkingDaysChecker seasonTicketWorkingDaysChecker,
                      SeasonTicketCountTripChecker seasonTicketCountTripChecker,
                      SeasonForDaysTicketForControlChecker seasonForDaysTicketForControlChecker,
                      TransferRouteChecker transferRouteChecker,
                      TransferPdChecker transferPdChecker,
                      PdLastPassageChecker pdLastPassageChecker,
                      TicketCategoryChecker ticketCategoryChecker) {
        this.ptkModeChecker = ptkModeChecker;
        this.nsiDaoSession = nsiDaoSession;
        this.nsiVersionManager = nsiVersionManager;
        this.beginDateChecker = beginDateChecker;
        this.endDateChecker = endDateChecker;
        this.revokedPdChecker = revokedPdChecker;
        this.trainCategoryChecker = trainCategoryChecker;
        this.ticketStopListItemChecker = ticketStopListItemChecker;
        this.smartCardStopListChecker = smartCardStopListChecker;
        this.seasonTicketWeekendDaysChecker = seasonTicketWeekendDaysChecker;
        this.seasonTicketWorkingDaysChecker = seasonTicketWorkingDaysChecker;
        this.seasonTicketCountTripChecker = seasonTicketCountTripChecker;
        this.seasonForDaysTicketForControlChecker = seasonForDaysTicketForControlChecker;
        this.transferRouteChecker = transferRouteChecker;
        this.transferPdChecker = transferPdChecker;
        this.pdLastPassageChecker = pdLastPassageChecker;
        this.ticketCategoryChecker = ticketCategoryChecker;
    }

    @Override
    public List<PassageResult> execCheck(PD pd) {
        /*
         * errorTypes это контейнер для хранения ошибок, выявленных при проверке ПД
         * в случае если ошибок не выявлено, то данный контейнер остается пустым,
         * что в дальнейшем будет свидетельствовать об отсутствие ошибок
         */
        List<PassageResult> errorTypes = new ArrayList<>();
        int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

        // Если это билет заглушка, то никаких проверок производить не надо
        if (pd.versionPD == PdVersion.V64.getCode()) {
            return errorTypes;
        }

        // Проверям наличие тарифа
        Tariff tariff = pd.getTariff();
        if (tariff == null) {
            errorTypes.add(PassageResult.TariffNotFound);
            return errorTypes;
        }

        Integer ticketCategoryCode = tariff.getTicketType(nsiDaoSession).getTicketCategoryCode();

        // Проверяем дату начала действия ПД
        Date pdStartDate = new Date(pd.getSaleDate().getTime() + TimeUnit.DAYS.toMillis(pd.term));
        if (!checkStartDate(pdStartDate)) {
            Logger.trace(TAG, "checkStartDate() is false");
            errorTypes.add(PassageResult.TooEarly);
        }


        if (ticketCategoryChecker.isSeasonForDaysTicket(ticketCategoryCode)) {
            // Проверяем даты действия ПД
            if (!seasonForDaysTicketForControlChecker.check(pd.getStartPdDate(), (int) pd.actionDays, new Date())) {
                // В будущем заменить на нормальный код ошибки, когда Александр Корчак расскажет какой он должен быть
                errorTypes.add(PassageResult.Unknown);
            }
        } else {
            // Проверяем дату окончания действия ПД
            if (!checkEndDate(pd, tariff, pdStartDate, nsiVersion)) {
                Logger.trace(TAG, "checkEndDate() is false");
                errorTypes.add(PassageResult.TooLate);
            }
        }


        // Проверяем, не аннулирован ли ПД.
        if (!checkIsNotAnnulled(pd)) {
            Logger.trace(TAG, "checkIsNotAnnulled() is false");
            errorTypes.add(PassageResult.BannedByCanceled);
        }

        // Проверяем категорию поезда
        if (!checkTrainCategory(tariff, nsiVersion)) {
            Logger.trace(TAG, "checkTrainCategory() is false");
            errorTypes.add(PassageResult.BannedTrainType);
        }

        // Проверяем ПД по стоплисту билетов
        if (ticketStopListItemChecker.check(pd.getTicketId())) {
            Logger.trace(TAG, "ticketStopListItemChecker.check() is true");
            errorTypes.add(PassageResult.BannedByStopListTickets);
        }

        // Проверяем карту по стоп-листу
        if (!checkCardInStopList(pd, nsiVersion)) {
            Logger.trace(TAG, "checkCardInStopList() is false");
            errorTypes.add(PassageResult.BannedByStopListCards);
        }

        // Проверяем направление для ПД-трансфера
        if (!checkRouteFoTransferPd(pd, tariff, nsiVersion)) {
            Logger.trace(TAG, "checkRouteFoTransferPd() is false");
            errorTypes.add(PassageResult.InvalidStation);
        }

        if (ticketCategoryChecker.isWeekendSeasonTicket(ticketCategoryCode)) {
            if (!(seasonTicketWeekendDaysChecker.check(nsiVersion))) {
                errorTypes.add(PassageResult.WeekendOnly);
            }
        } else if (ticketCategoryChecker.isWorkDaysSeasonTicket(ticketCategoryCode)) {
            if (!(seasonTicketWorkingDaysChecker.check(nsiVersion))) {
                errorTypes.add(PassageResult.WorkingDayOnly);
            }
        } else if (ticketCategoryChecker.isCountTripsSeasonTicket(ticketCategoryCode)) {
            PassageMark passageMark = pd.getPassageMark();
            if (passageMark != null) {
                if (!seasonTicketCountTripChecker.check(pd, passageMark)) {
                    errorTypes.add(PassageResult.NoTrips);
                }
                pdLastPassageChecker.checkLastPassage(pd, passageMark);
            }
        }

        return errorTypes;
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
     * Проверяет дату окончания действия ПД.
     *
     * @param pd          ПД
     * @param tariff      Тариф
     * @param pdStartDate Дата начала действия ПД
     * @param nsiVersion  Версия НСИ
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    private boolean checkEndDate(@NonNull PD pd, @NonNull Tariff tariff, @NonNull Date pdStartDate, int nsiVersion) {
        return endDateChecker.check(
                nsiVersion,
                pdStartDate,
                pd.wayType,
                tariff.getTicketTypeCode()
        );
    }

    /**
     * Проверяет, не аннулирован ли ПД.
     *
     * @param pd ПД
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    private boolean checkIsNotAnnulled(PD pd) {
        return revokedPdChecker.check(pd.numberPD, pd.ecpNumberPD, pd.getSaleDate());
    }

    /**
     * Проверяет категорию поезда.
     *
     * @param tariff     Тариф
     * @param nsiVersion Версия НСИ
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    private boolean checkTrainCategory(Tariff tariff, int nsiVersion) {
        if (ptkModeChecker.isTransferControlMode()) {
            // При работе в режиме трансфера не проверяем категорию ПД
            // http://agile.srvdev.ru/browse/CPPKPP-36776
            return true;
        }
        return trainCategoryChecker.check(tariff, nsiVersion);
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
            return true;
        }
    }

    /**
     * Проверяет маршрут для ПД-трансфера.
     *
     * @param legacyPd   ПД
     * @param tariff     Тариф
     * @param nsiVersion Версия НСИ
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    private boolean checkRouteFoTransferPd(PD legacyPd, Tariff tariff, int nsiVersion) {
        Pd pd = new PdFromLegacyMapper().fromLegacyPd(legacyPd);

        // Если это не трансфер, пропускаем проверку
        if (!transferPdChecker.check(pd)) {
            return true;
        }
        // Если мы работаем в автобусе, и у нас включен либо режим
        // "Контроль ПД на трансфер в автобусе" либо ???
        // (если для продажи будет отдельный режим работы),
        // то в этом случае мы можем взять из настроек станцию
        // отправления/назначения работы и сравнить со считанными станциями,
        // в противном случае у нас нет автобусной станции работы,
        // соответственно валидацию по этому признаку мы не проводим
        if (ptkModeChecker.isTransferControlMode()) {
            return transferRouteChecker.checkRouteFoTransferPd(legacyPd, tariff, nsiVersion);
        } else {
            return true;
        }
    }
}

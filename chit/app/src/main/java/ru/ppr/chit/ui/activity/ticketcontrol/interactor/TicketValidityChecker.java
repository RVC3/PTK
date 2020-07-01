package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.chit.domain.ticketcontrol.DataCarrierType;
import ru.ppr.chit.domain.ticketcontrol.TicketControlData;
import ru.ppr.chit.domain.tripservice.TripServiceMode;
import ru.ppr.chit.domain.tripservice.TripServiceModeManager;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.exceptions.UserCriticalException;
import ru.ppr.core.manager.eds.CheckSignResult;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.logger.Logger;

/**
 * Валидатор ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketValidityChecker {

    private static final String TAG = Logger.makeLogTag(TicketValidityChecker.class);

    private final DepartureDateChecker departureDateChecker;
    private final TrainNumberChecker trainNumberChecker;
    private final DepStationChecker depStationChecker;
    private final DestinationStationChecker destinationStationChecker;
    private final WhiteListChecker whiteListChecker;
    private final StationRepository stationRepository;
    private final NsiVersionProvider nsiVersionProvider;
    private final EdsChecker edsChecker;
    private final TicketStatusChecker ticketStatusChecker;
    private final TripServiceModeManager tripServiceModeManager;

    @Inject
    TicketValidityChecker(DepartureDateChecker departureDateChecker,
                          TrainNumberChecker trainNumberChecker,
                          DepStationChecker depStationChecker,
                          DestinationStationChecker destinationStationChecker,
                          WhiteListChecker whiteListChecker,
                          StationRepository stationRepository,
                          NsiVersionProvider nsiVersionProvider,
                          EdsChecker edsChecker,
                          TicketStatusChecker ticketStatusChecker,
                          TripServiceModeManager tripServiceModeManager) {
        this.departureDateChecker = departureDateChecker;
        this.trainNumberChecker = trainNumberChecker;
        this.depStationChecker = depStationChecker;
        this.destinationStationChecker = destinationStationChecker;
        this.whiteListChecker = whiteListChecker;
        this.stationRepository = stationRepository;
        this.nsiVersionProvider = nsiVersionProvider;
        this.edsChecker = edsChecker;
        this.ticketStatusChecker = ticketStatusChecker;
        this.tripServiceModeManager = tripServiceModeManager;
    }

    @NonNull
    public Result check(@NonNull TicketControlData ticketControlData, @Nullable Pd pd, @Nullable byte[] eds, @Nullable CardInformation cardInformation) {
        //region Проверка ЭЦП
        CheckSignResultState checkSignResultState;
        long deviceId;
        Logger.trace(TAG, "dataCarrierType = " + ticketControlData.getDataCarrierType());
        if (ticketControlData.getDataCarrierType() == DataCarrierType.TICKET_LIST) {
            // Если билет контролируется из списка
            // Считаем что подпись валидна и ключ не отозван
            checkSignResultState = CheckSignResultState.VALID;
            // Используем deviceId из TicketId
            deviceId = ticketControlData.getDeviceId();
        } else {
            // Если билет считан с ШК или карты
            // Проверям подпись
            if (pd == null) {
                throw new UserCriticalException("Ошибка получения информации о ПД");
            }
            CheckSignResult checkSignResult = edsChecker.check(pd, eds, cardInformation);
            checkSignResultState = checkSignResult.getState();
            deviceId = checkSignResult.getDeviceId();
            Logger.trace(TAG, "checkSignResult: " + checkSignResult);
        }
        //endregion

        // Устанавливаем значения по умолчанию
        boolean ticketCancelled = false;
        boolean ticketReturned = false;
        boolean departureDateFullyValid = true;
        boolean departureDateProbablyValid = false;
        boolean trainNumberValid = true;
        boolean depStationValid = true;
        boolean destinationStationValid = true;
        boolean inWhiteList = false;

        // Задаем начальное значение переменной общей валидности ПД
        boolean valid = true;

        if (isTrainAndControlStationInfoAvailable()) {
            // Если известна информация о поезде и станции контроля

            // Создаем идентификатор ПД
            TicketId ticketId = new TicketId();
            ticketId.setSaleDate(ticketControlData.getSaleDateTime());
            ticketId.setTicketNumber(ticketControlData.getTicketNumber());
            ticketId.setDeviceId(String.valueOf(deviceId));

            // Проверяем статус билета
            TicketStatusChecker.Result ticketStatusCheckerResult = ticketStatusChecker.check(ticketId);
            ticketCancelled = ticketStatusCheckerResult == TicketStatusChecker.Result.CANCELLED;
            ticketReturned = ticketStatusCheckerResult == TicketStatusChecker.Result.RETURNED;
            valid &= !ticketCancelled && !ticketReturned;

            // Проверяем станцию отправления
            depStationValid = depStationChecker.checkStation(stationRepository.load(ticketControlData.getDepartureStationCode(), nsiVersionProvider.getCurrentNsiVersion()));
            valid &= depStationValid;

            // Проверяем станцию назначения
            destinationStationValid = destinationStationChecker.checkStation(stationRepository.load(ticketControlData.getDestinationStationCode(), nsiVersionProvider.getCurrentNsiVersion()));
            valid &= destinationStationValid;

            // Проверяем билет по белым спискам
            inWhiteList = whiteListChecker.isInWhiteList(ticketId);

            if (ticketStatusCheckerResult == TicketStatusChecker.Result.NOT_FOUND) {
                // Если билет ненайден в списке с БС
                // Проверяем дату отправления
                DepartureDateChecker.Result departureDateCheckerResult = departureDateChecker.checkDate(ticketControlData.getDepartureDates().get(0), ticketControlData.getDepartureStationCode());
                departureDateFullyValid = departureDateCheckerResult == DepartureDateChecker.Result.FULLY_VALID;
                departureDateProbablyValid = departureDateCheckerResult == DepartureDateChecker.Result.PROBABLY_VALID;
                valid &= departureDateFullyValid || departureDateProbablyValid;
                // Проверяем номер поезда
                trainNumberValid = trainNumberChecker.checkTrainNumber(ticketControlData.getTrainNumber());
                valid &= trainNumberValid;
            }
        }

        // Формируем итоговый результат проверки подписи
        // Ошибки проверки подписи НЕТ, если:
        // - Подпись валидна
        valid &= checkSignResultState == CheckSignResultState.VALID ||
                // - Ключ ЭЦП отозван, но билет в белом списке
                checkSignResultState == CheckSignResultState.KEY_REVOKED && inWhiteList;

        return new Result(valid,
                departureDateFullyValid,
                departureDateProbablyValid,
                trainNumberValid,
                depStationValid,
                destinationStationValid,
                ticketCancelled,
                ticketReturned,
                checkSignResultState,
                deviceId,
                inWhiteList);
    }

    /**
     * Проверяет, известна ли информация о поезде и станции контроля.
     *
     * @return {@code true} если информация известна, {@code false} - иначе
     */
    private boolean isTrainAndControlStationInfoAvailable() {
        // Такая реализация, пока не будет пересмотрено понятие оффлайн/онлайн
        return tripServiceModeManager.detectTripServiceMode() == TripServiceMode.ONLINE;
    }

    public class Result {

        private final boolean valid;
        private final boolean departureDateFullyValid;
        private final boolean departureDateProbablyValid;
        private final boolean trainNumberValid;
        private final boolean depStationValid;
        private final boolean destinationStationValid;
        private final boolean ticketCancelled;
        private final boolean ticketReturned;
        private final CheckSignResultState checkSignResultState;
        private final long deviceId;
        private final boolean inWhiteList;

        Result(boolean valid,
               boolean departureDateFullyValid,
               boolean departureDateProbablyValid,
               boolean trainNumberValid,
               boolean depStationValid,
               boolean destinationStationValid,
               boolean ticketCancelled,
               boolean ticketReturned,
               CheckSignResultState checkSignResultState,
               long deviceId,
               boolean inWhiteList) {
            this.valid = valid;
            this.departureDateFullyValid = departureDateFullyValid;
            this.departureDateProbablyValid = departureDateProbablyValid;
            this.trainNumberValid = trainNumberValid;
            this.depStationValid = depStationValid;
            this.destinationStationValid = destinationStationValid;
            this.ticketCancelled = ticketCancelled;
            this.ticketReturned = ticketReturned;
            this.checkSignResultState = checkSignResultState;
            this.deviceId = deviceId;
            this.inWhiteList = inWhiteList;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isDepartureDateFullyValid() {
            return departureDateFullyValid;
        }

        public boolean isDepartureDateProbablyValid() {
            return departureDateProbablyValid;
        }

        public boolean isTrainNumberValid() {
            return trainNumberValid;
        }

        public boolean isDestinationStationValid() {
            return destinationStationValid;
        }

        public boolean isDepStationValid() {
            return depStationValid;
        }

        public boolean isTicketCancelled() {
            return ticketCancelled;
        }

        public boolean isTicketReturned() {
            return ticketReturned;
        }

        public CheckSignResultState getCheckSignResultState() {
            return checkSignResultState;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public boolean isInWhiteList() {
            return inWhiteList;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "valid=" + valid +
                    ", departureDateFullyValid=" + departureDateFullyValid +
                    ", departureDateProbablyValid=" + departureDateProbablyValid +
                    ", trainNumberValid=" + trainNumberValid +
                    ", depStationValid=" + depStationValid +
                    ", destinationStationValid=" + destinationStationValid +
                    ", ticketCancelled=" + ticketCancelled +
                    ", ticketReturned=" + ticketReturned +
                    ", checkSignResultState=" + checkSignResultState +
                    ", deviceId=" + deviceId +
                    ", inWhiteList=" + inWhiteList +
                    '}';
        }
    }
}

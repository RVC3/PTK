package ru.ppr.cppk.ui.fragment.pd.countrips.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageTime;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.PdV23V24HwCounter;
import ru.ppr.logger.Logger;

/**
 * Валидатор последнего прохода по ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class LastPassageChecker {

    private static final String TAG = Logger.makeLogTag(LastPassageChecker.class);

    private final CommonSettings commonSettings;
    private final PdVersionChecker pdVersionChecker;

    @Inject
    LastPassageChecker(CommonSettings commonSettings, PdVersionChecker pdVersionChecker) {
        this.commonSettings = commonSettings;
        this.pdVersionChecker = pdVersionChecker;
    }

    /**
     * Выполняет проверку метки прохода для ПД.
     *
     * @param pd             ПД
     * @param passageMark    Метка прохода
     * @param pdIndex        Порядковый номер ПД на карте
     * @param hwCounterValue Значение хардварного счетчика
     * @return Результат проверки метки прохода
     */
    @NonNull
    public Result check(@NonNull Pd pd, @NonNull PassageMark passageMark, int pdIndex, int hwCounterValue) {
        // Определяем наличие входа через турникет 7000
        boolean entrance7000 = false;
        if (pdVersionChecker.isCombinedCountTripsSeasonTicket(pd.getVersion())) {
            PdV23V24HwCounter pdV23V24HwCounter = new PdV23V24HwCounter(hwCounterValue);
            Logger.warning(TAG, "PdV23V24HwCounter: " + pdV23V24HwCounter);
            if (pdV23V24HwCounter.getTrips7000Counter() % 2 == 0) {
                // Значение счетчика для поезда 7000 - четное.
                // Это значит, что был вход через турникет 7000
                entrance7000 = true;
            }
        }
        Logger.trace(TAG, "entrance7000: " + entrance7000);

        // Определяем наличие последнего прохода
        if (!(passageMark instanceof PassageMarkWithFlags)) {
            Logger.trace(TAG, "Incorrect passage mark: " + passageMark);
            return new Result(false, null, entrance7000);
        }
        PassageMarkWithFlags passageMarkWithFlags = (PassageMarkWithFlags) passageMark;
        boolean passageExists = passageMarkWithFlags.getPassageStatusForPd(pdIndex) == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS;
        Logger.trace(TAG, "passageExists: " + passageExists);

        if (!passageExists) {
            Logger.trace(TAG, "No passage on mark: " + passageMark);
            if (entrance7000) {
                // Значение счетчика для поезда 7000 - четное.
                // Это значит, что последним проходом был вход на станцию
                // http://agile.srvdev.ru/browse/CPPKPP-42108
                // Ориентируемся на счетчик, считаем, что проход был
                Logger.warning(TAG, "Data conflict, passageExists: false, entrance7000: true");
            } else {
                return new Result(false, null, false);
            }
        }

        // Определяем время последнего прохода
        if (!(passageMark instanceof PassageMarkWithPassageTime)) {
            Logger.trace(TAG, "Incorrect passage mark: " + passageMark);
            return new Result(false, null, entrance7000);
        }
        PassageMarkWithPassageTime passageMarkWithPassageTime = (PassageMarkWithPassageTime) passageMark;
        Date lastPassageTime = calcPassageTime(pd, passageMarkWithPassageTime.getPdPassageTime(pdIndex));
        Logger.trace(TAG, "lastPassageTime: " + lastPassageTime);
        if (lastPassageTime == null) {
            return new Result(false, null, entrance7000);
        }

        // Определяем сколько времени назад была списана поездка
        long timeFormPrevPassage = System.currentTimeMillis() - lastPassageTime.getTime();
        Logger.trace(TAG, "timeFormPrevPassage: " + timeFormPrevPassage);

        // Определяем валидность времени прохода
        // Время валидно, если не выходит за допустимые границы
        int maxTimeFormPrevPassage = commonSettings.getMaxTimeAgoMark();
        boolean passageTimeValid = timeFormPrevPassage <= TimeUnit.HOURS.toMillis(maxTimeFormPrevPassage);
        Logger.trace(TAG, "passageTimeValid: " + passageTimeValid);
        if (!passageTimeValid) {
            return new Result(false, lastPassageTime, entrance7000);
        }

        // Определяем направление последнего прохода
        // http://agile.srvdev.ru/browse/CPPKPP-31705
        int passageType = passageMarkWithFlags.getPassageTypeForPd(pdIndex);
        Logger.trace(TAG, "passageType: " + passageType);

        if (entrance7000) {
            // Значение счетчика для поезда 7000 - четное.
            // Это значит, что последним проходом был вход на станцию
            // http://agile.srvdev.ru/browse/CPPKPP-42108
            // Лебедев Сергей:
            // Ориентируемся на счетчик при расхождении показаний с меткой
            if (passageType != PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION) {
                Logger.warning(TAG, "Data conflict, passageType: " + passageType + ", entrance7000: true");
                passageType = PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION;
            }
        }

        // Определяем валидность направления прохода
        // Направление валидно, если это не выход со станции
        boolean directionValid = passageType != PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION;
        Logger.trace(TAG, "directionValid: " + directionValid);

        if (!directionValid) {
            return new Result(false, lastPassageTime, false);
        }

        return new Result(true, lastPassageTime, entrance7000);
    }

    /**
     * Возвращает время прохода по билету.
     *
     * @param pd          ПД
     * @param passageTime Время прохода из метки, в секундах
     */
    @Nullable
    private Date calcPassageTime(Pd pd, long passageTime) {
        if (passageTime == 0) {
            return null;
        } else {
            long saleDateTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(pd.getSaleDateTime().getTime());
            long passageTimeInSeconds = saleDateTimeInSeconds + passageTime;
            return new Date(TimeUnit.SECONDS.toMillis(passageTimeInSeconds));
        }
    }

    public static class Result {
        /**
         * Валидность прохода
         */
        private final boolean valid;
        /**
         * Время прохода
         */
        private final Date passageTime;
        /**
         * Факт входа через турникет 7000.
         * Заполняется только для {@link PdVersion#V23} и {@link PdVersion#V24}
         * {@code true} - если это был вход через 7000, {@code false} - иначе
         */
        private final boolean entrance7000;

        public Result(boolean valid, Date passageTime, boolean entrance7000) {
            this.valid = valid;
            this.passageTime = passageTime;
            this.entrance7000 = entrance7000;
        }

        public boolean isValid() {
            return valid;
        }

        @Nullable
        public Date getPassageTime() {
            return passageTime;
        }

        public boolean isEntrance7000() {
            return entrance7000;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "valid=" + valid +
                    ", passageTime=" + passageTime +
                    ", entrance7000=" + entrance7000 +
                    '}';
        }
    }
}

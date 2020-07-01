package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.cppk.dataCarrier.PassageMarkFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.PdFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.ui.fragment.pd.countrips.interactor.LastPassageChecker;
import ru.ppr.cppk.ui.fragment.pd.countrips.interactor.TripsCountCalculator;
import ru.ppr.logger.Logger;

/**
 * Чекер абонемента на количество поездок
 *
 * @author Grigoriy Kashka
 */
public class SeasonTicketCountTripChecker {

    private static final String TAG = Logger.makeLogTag(SeasonTicketCountTripChecker.class);

    private final TripsCountCalculator tripsCountCalculator;
    private final LastPassageChecker lastPassageChecker;

    @Inject
    public SeasonTicketCountTripChecker(TripsCountCalculator tripsCountCalculator,
                                        LastPassageChecker lastPassageChecker) {
        this.tripsCountCalculator = tripsCountCalculator;
        this.lastPassageChecker = lastPassageChecker;
    }

    /**
     * Проверяет наличие поездок.
     *
     * @param legacyPd          ПД
     * @param legacyPassageMark Метка прохода
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    public boolean check(@NonNull PD legacyPd, @NonNull ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark) {
        PassageMark passageMark = new PassageMarkFromLegacyMapper().fromLegacyPassageMark(legacyPassageMark);
        Pd pd = new PdFromLegacyMapper().fromLegacyPd(legacyPd);
        int hwCounterValue = legacyPd.getHwCounterValue() == null ? 0 : legacyPd.getHwCounterValue();
        if (tripsCountCalculator.calcAvailableTripsCount(pd, hwCounterValue) == 0) {
            // Нет поездок для списания
            LastPassageChecker.Result lastPassageCheckResult = lastPassageChecker.check(pd, passageMark, legacyPd.orderNumberPdOnCard, hwCounterValue);
            Logger.trace(TAG, "lastPassageCheckResult: " + lastPassageCheckResult);
            // Если последний проход валиден, считаем ПД валидным
            return lastPassageCheckResult.isValid();
        } else {
            // Ещё есть поездки для списания
            return true;
        }
    }
}

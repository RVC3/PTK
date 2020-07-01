package ru.ppr.cppk.ui.activity.controlreadbsc.helper;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardData;
import ru.ppr.cppk.logic.pd.checker.ValidAndControlNeededChecker;

/**
 * Определитель необходимости увеличения счетчика использования карты.
 *
 * @author Aleksandr Brazhkin
 */
public class IncrementPmHwUsageCounterChecker {

    private final PdVersionChecker pdVersionChecker;
    private final ValidAndControlNeededChecker validAndControlNeededChecker;

    @Inject
    IncrementPmHwUsageCounterChecker(PdVersionChecker pdVersionChecker,
                                     ValidAndControlNeededChecker validAndControlNeededChecker) {
        this.pdVersionChecker = pdVersionChecker;
        this.validAndControlNeededChecker = validAndControlNeededChecker;
    }

    /**
     * Проверяет, требуется ли увеличивать счетчик использования карты в метке прохода
     *
     * @return {@code true} если требуется, {@code false} - иначе
     */
    public boolean isIncrementRequired(@NonNull List<PD> legacyPdList) {
        for (PD legacyPd : legacyPdList) {
            PdVersion pdVersion = PdVersion.getByCode(legacyPd.versionPD);
            Preconditions.checkNotNull(pdVersion);
            if (validAndControlNeededChecker.isValidAndControlNeeded(legacyPd)) {
                // Если ПД валиден, и требуется создание события контроля
                if (pdVersionChecker.isCountTripsSeasonTicket(pdVersion)) {
                    // Если это абонемент на количество поездок
                    // Нужно выполнить доп. проверку валидности последнего прохода
                    if (legacyPd.isLastPassageValid()) {
                        // Если последний проход валиден
                        // Считаем билет "зеленым", нужно увеличивать счетчик использования карты
                        return true;
                    }
                } else {
                    // Если это НЕ абонемент на количество поездок
                    // Считаем билет "зеленым", нужно увеличивать счетчик использования карты
                    return true;
                }
            }
        }
        // Нет "зеленых" ПД, увеличивать счетчик использования карты НЕ нужно
        return false;
    }

    /**
     * Проверяет, требуется ли увеличивать счетчик использования карты в метке прохода
     *
     * @return {@code true} если требуется, {@code false} - иначе
     */
    public boolean isIncrementRequired(@NonNull ServiceTicketControlCardData serviceTicketControlCardData) {
        return serviceTicketControlCardData.getCheckResult().isValid();
    }

}

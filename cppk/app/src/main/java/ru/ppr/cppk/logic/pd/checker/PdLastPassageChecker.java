package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.cppk.dataCarrier.PassageMarkFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.PdFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.ui.fragment.pd.countrips.interactor.LastPassageChecker;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TrainCategoryPrefix;

/**
 * Валидатор последнего прохода по ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdLastPassageChecker {

    private static final String TAG = Logger.makeLogTag(PdLastPassageChecker.class);

    private final LastPassageChecker lastPassageChecker;
    private final PrivateSettings privateSettings;
    private final PdVersionChecker pdVersionChecker;

    @Inject
    PdLastPassageChecker(LastPassageChecker lastPassageChecker,
                         PrivateSettings privateSettings,
                         PdVersionChecker pdVersionChecker) {
        this.lastPassageChecker = lastPassageChecker;
        this.privateSettings = privateSettings;
        this.pdVersionChecker = pdVersionChecker;
    }

    /**
     * Проверяет последний проход.
     *
     * @param legacyPd          ПД
     * @param legacyPassageMark Метка прохода
     */
    public void checkLastPassage(@NonNull PD legacyPd, @NonNull ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark) {
        PassageMark passageMark = new PassageMarkFromLegacyMapper().fromLegacyPassageMark(legacyPassageMark);
        Pd pd = new PdFromLegacyMapper().fromLegacyPd(legacyPd);
        int hwCounterValue = legacyPd.getHwCounterValue() == null ? 0 : legacyPd.getHwCounterValue();

        LastPassageChecker.Result lastPassageCheckResult = lastPassageChecker.check(pd, passageMark, legacyPd.orderNumberPdOnCard, hwCounterValue);
        Logger.info(TAG, "lastPassageCheckResult: " + lastPassageCheckResult);

        if (pdVersionChecker.isCombinedCountTripsSeasonTicket(pd.getVersion())) {
            // Это комбинированный абонемент
            if (lastPassageCheckResult.isEntrance7000()) {
                // Если был вход через турникет 7000
                // Контроль 6000 и 7000
                legacyPd.setLastPassageTurnstile7000(true);
                legacyPd.setLastPassage6000Valid(false);
                legacyPd.setLastPassageTime(lastPassageCheckResult.getPassageTime());
                legacyPd.setLastPassageValid(lastPassageCheckResult.isValid());
            } else {
                // Если не было входа через турникет 7000
                if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.EXPRESS) {
                    // Контроль 7000
                    legacyPd.setLastPassageTurnstile7000(false);
                    legacyPd.setLastPassage6000Valid(lastPassageCheckResult.isValid());
                    legacyPd.setLastPassageTime(lastPassageCheckResult.getPassageTime());
                    legacyPd.setLastPassageValid(false);
                } else {
                    // Контроль 6000
                    legacyPd.setLastPassageTurnstile7000(false);
                    legacyPd.setLastPassage6000Valid(lastPassageCheckResult.isValid());
                    legacyPd.setLastPassageTime(lastPassageCheckResult.getPassageTime());
                    legacyPd.setLastPassageValid(lastPassageCheckResult.isValid());
                }
            }
        } else {
            // Классический сценарий
            legacyPd.setLastPassageTurnstile7000(false);
            legacyPd.setLastPassage6000Valid(false);
            legacyPd.setLastPassageTime(lastPassageCheckResult.getPassageTime());
            legacyPd.setLastPassageValid(lastPassageCheckResult.isValid());
        }
    }
}

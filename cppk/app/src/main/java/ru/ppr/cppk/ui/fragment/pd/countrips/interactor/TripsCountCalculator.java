package ru.ppr.cppk.ui.fragment.pd.countrips.interactor;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TrainCategoryPrefix;

/**
 * Калькулятор количества доступных поездок по ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TripsCountCalculator {

    private static final String TAG = Logger.makeLogTag(TripsCountCalculator.class);

    private final PdV7V18TripsCountCalculator pdV7V18TripsCountCalculator;
    private final PdV19V20TripsCountCalculator pdV19V20TripsCountCalculator;
    private final PdV23V24TripsCountCalculator pdV23V24TripsCountCalculator;
    private final PrivateSettings privateSettings;

    @Inject
    TripsCountCalculator(PdV7V18TripsCountCalculator pdV7V18TripsCountCalculator,
                         PdV19V20TripsCountCalculator pdV19V20TripsCountCalculator,
                         PdV23V24TripsCountCalculator pdV23V24TripsCountCalculator,
                         PrivateSettings privateSettings) {
        this.pdV7V18TripsCountCalculator = pdV7V18TripsCountCalculator;
        this.pdV19V20TripsCountCalculator = pdV19V20TripsCountCalculator;
        this.pdV23V24TripsCountCalculator = pdV23V24TripsCountCalculator;
        this.privateSettings = privateSettings;
    }

    /**
     * Рассчитывает количество оставшихся поездок по ПД.
     *
     * @param pd             ПД
     * @param hwCounterValue Значение хардварного счетчика
     * @return Количество оставшихся поездок
     */
    public int calcAvailableTripsCount(@NonNull Pd pd, int hwCounterValue) {
        PdVersion pdVersion = pd.getVersion();
        if (pdVersion == PdVersion.V7 || pdVersion == PdVersion.V18) {
            return calcTripsCountForPdV718(pd, hwCounterValue);
        } else if (pdVersion == PdVersion.V19 || pdVersion == PdVersion.V20) {
            return calcTripsCountForPdV19V20(hwCounterValue);
        } else if (pdVersion == PdVersion.V23 || pdVersion == PdVersion.V24) {
            return calcTripsCountForPdV23V24(hwCounterValue);
        } else {
            Logger.trace(TAG, "Unsupported pdVersion = " + pdVersion);
            return 0;
        }
    }

    private int calcTripsCountForPdV718(@NonNull Pd pd, int hwCounterValue) {
        return pdV7V18TripsCountCalculator.calcTripsCount(pd, hwCounterValue);
    }

    private int calcTripsCountForPdV19V20(int hwCounterValue) {
        return pdV19V20TripsCountCalculator.calcTripsCount(hwCounterValue);
    }

    private int calcTripsCountForPdV23V24(int hwCounterValue) {
        PdV23V24TripsCountCalculator.Result result = pdV23V24TripsCountCalculator.calcTripsCount(hwCounterValue);
        if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.EXPRESS) {
            return result.getAvailableTrips7000Count();
        } else {
            return result.getAvailableTripsTotalCount();
        }
    }
}

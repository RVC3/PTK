package ru.ppr.cppk.pd.check.control;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.StrategyCheck;
import ru.ppr.cppk.logic.ServiceFeePdChecker;

/**
 * Фабрика для {@link StrategyCheck}.
 *
 * @author Dmitry Nevolin
 */
public class StrategyCheckFactory {

    private final StrategyCheckImpl strategyCheckImpl;
    private final ServiceFeePdStrategyCheck serviceFeePdStrategyCheck;
    private final ServiceFeePdChecker serviceFeePdChecker;

    @Inject
    public StrategyCheckFactory(StrategyCheckImpl strategyCheckImpl,
                                ServiceFeePdStrategyCheck serviceFeePdStrategyCheck,
                                ServiceFeePdChecker serviceFeePdChecker) {
        this.strategyCheckImpl = strategyCheckImpl;
        this.serviceFeePdStrategyCheck = serviceFeePdStrategyCheck;
        this.serviceFeePdChecker = serviceFeePdChecker;
    }

    @NonNull
    public StrategyCheck createStrategyCheck(@NonNull PD pd) {
        if (serviceFeePdChecker.check(pd)) {
            return serviceFeePdStrategyCheck;
        } else {
            return strategyCheckImpl;
        }
    }
}

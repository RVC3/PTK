package ru.ppr.core.dataCarrier.findcardtask.authstrategy;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.NsiDataProvider;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.NsiSamAuthorizationStrategy;
import ru.ppr.rfid.SamAuthorizationStrategy;

/**
 * Фабрика для {@link SamAuthorizationStrategy}.
 *
 * @author Aleksandr Brazhkin
 */
public class SamAuthorizationStrategyFactoryImpl implements SamAuthorizationStrategyFactory {

    private final NsiDataProvider nsiDataProvider;

    @Inject
    SamAuthorizationStrategyFactoryImpl(NsiDataProvider nsiDataProvider) {
        this.nsiDataProvider = nsiDataProvider;
    }

    @Override
    public SamAuthorizationStrategy create() {
        return new NsiSamAuthorizationStrategy(nsiDataProvider, null);
    }
}

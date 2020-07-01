package ru.ppr.core.dataCarrier.findcardtask.authstrategy;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.findcardtask.authstrategy.statickey.DefaultStaticKeyAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Фабрика для {@link StaticKeyAuthorizationStrategy}.
 *
 * @author Aleksandr Brazhkin
 */
public class StaticKeyAuthorizationStrategyFactoryImpl implements StaticKeyAuthorizationStrategyFactory {

    @Inject
    StaticKeyAuthorizationStrategyFactoryImpl(){

    }

    @Override
    public StaticKeyAuthorizationStrategy create() {
        return new DefaultStaticKeyAuthorizationStrategy();
    }
}

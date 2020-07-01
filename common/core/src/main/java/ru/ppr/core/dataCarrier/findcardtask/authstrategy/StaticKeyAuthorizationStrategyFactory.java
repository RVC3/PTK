package ru.ppr.core.dataCarrier.findcardtask.authstrategy;

import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Фабрика для {@link StaticKeyAuthorizationStrategy}.
 *
 * @author Aleksandr Brazhkin
 */
public interface StaticKeyAuthorizationStrategyFactory {
    /**
     * Возвращает новый инстанс {@link StaticKeyAuthorizationStrategy}.
     */
    StaticKeyAuthorizationStrategy create();
}

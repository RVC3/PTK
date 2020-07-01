package ru.ppr.core.dataCarrier.findcardtask.authstrategy;

import ru.ppr.rfid.SamAuthorizationStrategy;

/**
 * Фабрика для {@link SamAuthorizationStrategy}.
 *
 * @author Aleksandr Brazhkin
 */
public interface SamAuthorizationStrategyFactory {
    /**
     * Возвращает новый инстанс {@link SamAuthorizationStrategy}.
     */
    SamAuthorizationStrategy create();
}

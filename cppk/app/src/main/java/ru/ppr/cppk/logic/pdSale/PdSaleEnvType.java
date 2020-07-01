package ru.ppr.cppk.logic.pdSale;

/**
 * Тип {@link PdSaleEnv}.
 *
 * @author Aleksandr Brazhkin
 */
public enum PdSaleEnvType {
    /**
     * Оформление разового ПД
     */
    SINGLE_PD,
    /**
     * Оформление багажа
     */
    BAGGAGE,
    /**
     * Информация о тарифах
     */
    TARIFFS_INFO,
    /**
     * Оформление трансфера
     */
    TRANSFER,
    /**
     * Оформление доплаты
     */
    EXTRA_PAYMENT
}

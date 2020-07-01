package ru.ppr.core.dataCarrier.smartCard.serviceData.base;

import java.util.Date;

import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataVersion;

/**
 * Служебные данные.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceData {
    /**
     * Возвращает версию служебных данных.
     *
     * @return Версия служебных данных.
     */
    ServiceDataVersion getVersion();

    /**
     * Возвращает размер служебных данных в байтах.
     *
     * @return Размер служебных данных в байтах.
     */
    int getSize();

    /**
     * Возвращает дату инициализации.
     *
     * @return Дата инициализации
     */
    Date getInitDateTime();

    /**
     * Возвращает номер ключа ЭЦП.
     *
     * @return Номер ключа ЭЦП
     */
    long getEdsKeyNumber();
}

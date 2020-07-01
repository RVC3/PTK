package ru.ppr.core.dataCarrier.smartCard.checker;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataVersion;

/**
 * Вспомогательный класс, различающий карты Тройка с ПД и карты Тройка со служебными данными.
 *
 * @author Aleksandr Brazhkin
 */
public class TroykaRecognizer {

    private static final int VERSION_INDEX = 0;

    /**
     * Проверяет, является ли карта картой Тройка с ПД.
     *
     * @param payloadFirstBlockData Данные первого блока, в котором расположен ПД или служебные данные
     * @return {@code true}, если это карта Тройка с ПД, {@code false} иначе.
     */
    public boolean isTroykaWithPd(byte[] payloadFirstBlockData) {
        return PdVersion.getByCode(payloadFirstBlockData[VERSION_INDEX]) != null;
    }

    /**
     * Проверяет, является ли карта картой Тройка со служебными данными.
     *
     * @param payloadFirstBlockData Данные первого блока, в котором расположен ПД или служебные данные
     * @return {@code true}, если это карта Тройка со служебными данными, {@code false} иначе.
     */
    public boolean isTroykaWithServiceData(byte[] payloadFirstBlockData) {
        return ServiceDataVersion.getByCode(payloadFirstBlockData[VERSION_INDEX]) != null;
    }
}

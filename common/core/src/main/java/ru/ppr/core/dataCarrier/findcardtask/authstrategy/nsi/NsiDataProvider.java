package ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.List;

/**
 * Провайдер данных НСИ для авторизации с помощью SAM-модуля.
 *
 * @author Aleksandr Brazhkin
 */
public interface NsiDataProvider {

    /**
     * Возвращает список возможных правил доступа.
     *
     * @param sectorNumber           Номер сектора
     * @param forRead                {@code true} если нужны правила на чтение, {@code false} - иначе
     * @param accessSchemeCodesIn    Ограничивающий список кодов схем доступа
     * @param accessSchemeCodesNotIn Список кодов исключаемых схем доступа
     * @return Список возможных правил доступа
     */
    List<Pair<AccessScheme, AccessRule>> provideSchemeRules(int sectorNumber,
                                                            boolean forRead,
                                                            @Nullable List<Integer> accessSchemeCodesIn,
                                                            @Nullable List<Integer> accessSchemeCodesNotIn);

    /**
     * Возвращает список кодов схем доступа для код типа носителя ПД.
     *
     * @param ticketStorageTypeCode Код типа носителя ПД
     * @return Список кодов схем доступа
     */
    List<Integer> getAccessSchemeCodes(Integer ticketStorageTypeCode);
}

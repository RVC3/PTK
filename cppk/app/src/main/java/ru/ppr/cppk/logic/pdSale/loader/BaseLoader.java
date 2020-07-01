package ru.ppr.cppk.logic.pdSale.loader;

import ru.ppr.cppk.logic.pdSale.PdSaleRestrictions;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Базовый класс для всех лоадеров данных при оформлении ПД.
 *
 * @author Aleksandr Brazhkin
 */
abstract class BaseLoader {

    /**
     * Ограничения на оформление ПД
     */
    private final PdSaleRestrictions pdSaleRestrictions;

    BaseLoader(PdSaleRestrictions pdSaleRestrictions) {
        // Устанавливаем зависимости
        this.pdSaleRestrictions = pdSaleRestrictions;
    }

    PdSaleRestrictions getPdSaleRestrictions() {
        return pdSaleRestrictions;
    }

    int getVersionId() {
        return pdSaleRestrictions.getNsiVersion();
    }
}

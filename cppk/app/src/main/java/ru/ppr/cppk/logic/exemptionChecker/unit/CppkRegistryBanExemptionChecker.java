package ru.ppr.cppk.logic.exemptionChecker.unit;

import ru.ppr.nsi.entity.Exemption;

/**
 * Проверка "Запрет на оформление ПД по коду льготу в ЦППК"
 *
 * @author Aleksandr Brazhkin
 */
public class CppkRegistryBanExemptionChecker {

    /**
     * Выполняет проверку льготы.
     *
     * @param exemption Льгота
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(Exemption exemption) {
        return !exemption.isCppkRegistryBan();
    }
}

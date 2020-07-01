package ru.ppr.cppk.logic.exemptionChecker.unit;

import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.nsi.entity.Exemption;

/**
 * Проверка "Для оформления билета требуется предъявление соц. карты"
 *
 * @author Aleksandr Brazhkin
 */
public class SocialCardExemptionChecker {

    /**
     * Выполняет проверку льготы.
     *
     * @param exemptionForEvent Льгота
     * @param exemption         Льгота
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(ExemptionForEvent exemptionForEvent, Exemption exemption) {
        return !exemptionForEvent.isManualInput() || !exemption.isRequireSocialCard() || exemption.isRequireSnilsNumber();
    }
}

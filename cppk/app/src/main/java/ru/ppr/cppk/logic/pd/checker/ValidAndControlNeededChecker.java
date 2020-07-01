package ru.ppr.cppk.logic.pd.checker;

import javax.inject.Inject;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.logic.NeedCreateControlEventChecker;

/**
 * Валидатор ПД и необходимости создания события контроля.
 *
 * @author Aleksandr Brazhkin
 */
public class ValidAndControlNeededChecker {

    private final NeedCreateControlEventChecker needCreateControlEventChecker;

    @Inject
    public ValidAndControlNeededChecker(NeedCreateControlEventChecker needCreateControlEventChecker) {
        this.needCreateControlEventChecker = needCreateControlEventChecker;
    }

    /**
     * Проверяет валидность ПД и необходимости создания события контроля.
     *
     * @param pd ПД
     * @return {@code true} если ПД валиден и требует создания события контроля, {@code false} - иначе
     */
    public boolean isValidAndControlNeeded(PD pd) {
        return pd.isValid() && needCreateControlEventChecker.check(pd);
    }
}

package ru.ppr.cppk.dataCarrier.pd.check.control;

import java.util.List;

import ru.ppr.cppk.dataCarrier.entity.PD;

/**
 * Валидатор ПД.
 *
 * @author Aleksandr Brazhkin
 */
public interface StrategyCheck {
    /**
     * Производит проверку действительности билета
     *
     * @return Список ошибок
     */
    List<PassageResult> execCheck(PD pd);
}

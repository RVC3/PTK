package ru.ppr.cppk.logic.exemptionChecker.unit;

/**
 * Проверка "Запрет на использование другой льготы".
 *
 * @author Aleksandr Brazhkin
 */
public class ExpressCodeExemptionChecker {

    /**
     * Выполняет проверку льготы.
     *
     * @param expressCode         Код льготы
     * @param exceptedExpressCode Ожидаемый код льготы
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(int expressCode, int exceptedExpressCode) {
        return exceptedExpressCode == 0 || expressCode == exceptedExpressCode;
    }
}

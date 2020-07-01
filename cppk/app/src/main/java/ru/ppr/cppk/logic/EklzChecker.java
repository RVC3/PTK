package ru.ppr.cppk.logic;

/**
 * Логика проверки номера ЭКЛЗ.
 *
 * @author Aleksandr Brazhkin
 */
public interface EklzChecker {
    /**
     * Выполняет проверку ЭКЛЗ.
     *
     * @param printerEKLZNumber   Номер ЭКЛЗ принетра
     * @param printerSerialNumber Серийный номер принтера
     * @return {@code true} если проверка прошла успешно, {@code false} - иначе
     */
    boolean check(String printerEKLZNumber, String printerSerialNumber);
}

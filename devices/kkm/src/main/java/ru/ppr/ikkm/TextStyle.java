package ru.ppr.ikkm;

/**
 * Стиль текста, отправляемого на печать.
 *
 * @author Aleksandr Brazhkin
 */
public enum TextStyle {
    /**
     * Нормальный шрифт для печати простого текста
     */
    TEXT_NORMAL,
    /**
     * Увеличенный шрифт для печати простого текста
     */
    TEXT_LARGE,
    /**
     * Нормальный шрифт для печати текста в фискальном чеке
     */
    FISCAL_NORMAL,
    /**
     * Увеличенный шрифт для печати текста в фискальном чеке
     */
    FISCAL_LARGE
}

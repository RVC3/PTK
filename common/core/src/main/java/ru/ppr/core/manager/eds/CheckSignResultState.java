package ru.ppr.core.manager.eds;

/**
 * Результат (состояние) проверки подписи
 *
 * @author Grigoriy Kashka
 */
public enum CheckSignResultState {
    /**
     * Подпись валидна
     */
    VALID,
    /**
     * Подпись валидна, но ключ отозван
     */
    KEY_REVOKED,
    /**
     * Подпись невалидна, либо возникла ошибка при выполнении операции
     */
    INVALID
}

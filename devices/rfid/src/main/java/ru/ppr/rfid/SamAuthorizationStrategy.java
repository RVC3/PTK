package ru.ppr.rfid;

/**
 * Алгоритм авторизации в секторах карт Mifare Classic c использованием SAM-модуля.
 *
 * @author Aleksandr Brazhkin
 */
public interface SamAuthorizationStrategy {
    /**
     * Возвращает возможный вариант правила доступа к сектору.
     *
     * @param sectorNum Номер сектора, для которого требуется авторизация.
     * @param forRead   {@code true}, если цель авторизации - чтение, {@code false}, если запись.
     * @return вариант правила доступа к сектору, {@code null}, если нет вариантов доступа.
     */
    SamAccessRule getKey(int sectorNum, boolean forRead);

    /**
     * Сообщает алгоритму статус результат авторизации предложенным вариантом правила.
     *
     * @param success {@code true}, если авторизация прошла успешно, {@code false} иначе.
     */
    void setLastSamAccessRuleStatus(boolean success);

    /**
     * Сохраняет текущее состояние процесса авторизации
     */
    void saveState();

    /**
     * Восстанавливает сохраненное состояние процесса авторизации
     */
    void restoreState();
}

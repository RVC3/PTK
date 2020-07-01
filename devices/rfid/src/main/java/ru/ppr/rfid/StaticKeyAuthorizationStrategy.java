package ru.ppr.rfid;

import android.support.annotation.Nullable;

/**
 * Алгоритм авторизации в секторах карт Mifare Classic c использованием конкретных ключей.
 *
 * @author Aleksandr Brazhkin
 */
public interface StaticKeyAuthorizationStrategy {
    /**
     * Возвращает возможный ключ для доступа к сектору.
     *
     * @param sectorNum Номер сектора, для которого требуется авторизация.
     * @param forRead   {@code true}, если цель авторизации - чтение, {@code false}, если запись.
     * @return возможный ключ доступа к сектору, {@code null}, если нет ключей.
     */
    @Nullable
    StaticKeyAccessRule getKey(int sectorNum, boolean forRead);

    /**
     * Сообщает алгоритму статус результат авторизации предложенным ключом.
     *
     * @param success {@code true}, если авторизация прошла успешно, {@code false} иначе.
     */
    void setLastStaticKeyStatus(boolean success);
}

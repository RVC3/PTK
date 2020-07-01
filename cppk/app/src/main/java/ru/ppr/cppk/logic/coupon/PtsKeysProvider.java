package ru.ppr.cppk.logic.coupon;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.security.entity.PtsKey;

/**
 * Отдаёт список ключей
 *
 * @author Dmitry Nevolin
 */
public interface PtsKeysProvider {

    /**
     * Возвращает список ключей с заданным deviceKey
     *
     * @param deviceKey он же terminalId
     * @return список ключей
     */
    @NonNull
    List<PtsKey> provide(int deviceKey);

}

package ru.ppr.edssft.stub.hash;

import android.support.annotation.NonNull;

/**
 * Created by Артем on 29.01.2016.
 */
public interface Hash {

    /**
     * Производит вычисление хэш суммы
     * @param data данные для который надо посчитать хэш суммы
     * @return хэш сумма
     */
    @NonNull
    byte[] computeHash(byte[] data);
}

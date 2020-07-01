package ru.ppr.cppk.utils.ecp;

import android.support.annotation.Nullable;

/**
 * Created by Артем on 18.01.2016.
 */
public interface EcpDataCreator {
    /**
     * Создает массив байтов для подписи
     * @return
     */
    @Nullable byte[] create();
}

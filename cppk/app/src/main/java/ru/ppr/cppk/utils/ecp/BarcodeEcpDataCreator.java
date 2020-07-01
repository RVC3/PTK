package ru.ppr.cppk.utils.ecp;

import android.support.annotation.NonNull;

/**
 * Класс для создания данный для подписи, которые будут записаны на штрих-код
 *
 * Created by Артем on 18.01.2016.
 */
public class BarcodeEcpDataCreator implements EcpDataCreator {

    private final byte[] newPd;

    private BarcodeEcpDataCreator(Builder builder) {
        newPd = builder.newPd;
    }

    @Override
    public byte[] create() {
        //просто вернем данные билета, т.к. дополнительных заморочек в этом случае нет
        return newPd;
    }

    public static class Builder {

        private final byte[] newPd;

        public Builder(@NonNull byte[] newPd) {
            this.newPd = newPd;
        }

        public EcpDataCreator build(){
            return new BarcodeEcpDataCreator(this);
        }
    }
}

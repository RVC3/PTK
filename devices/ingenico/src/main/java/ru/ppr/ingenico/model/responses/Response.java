package ru.ppr.ingenico.model.responses;

import ru.ppr.ingenico.utils.Arcus2Utils;

/**
 * Created by Dmitry Nevolin on 13.11.2015.
 */
public abstract class Response {

    private String value;

    Response(String value) {
        this.value = value;
    }

    public byte[] packSelf() {
        return Arcus2Utils.packDefault(Arcus2Utils.convertStringToBytes(value));
    }

    @Override
    public String toString() {
        return value;
    }

}

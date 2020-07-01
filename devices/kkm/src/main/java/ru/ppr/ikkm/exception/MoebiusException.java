package ru.ppr.ikkm.exception;

/**
 * Created by Александр on 21.01.2016.
 */
public class MoebiusException extends Exception {

    public MoebiusException(byte err) {
        super("err = " + Byte.toString(err));
    }
}

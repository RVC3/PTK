package ru.ppr.ikkm.exception;

/**
 * Возникает когда при попытке проведения фискальной операции принтер отвечает что
 * время смены закончилось
 *
 * Created by Артем on 10.06.2016.
 */
public class ShiftTimeOutException extends PrinterException {

    public ShiftTimeOutException() {
        super("Shift close required");
    }
}

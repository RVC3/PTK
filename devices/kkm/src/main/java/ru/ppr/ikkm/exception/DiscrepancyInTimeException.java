package ru.ppr.ikkm.exception;

/**
 * Возникает при расхождении времени на принтере и ПТК.
 *
 * @author Aleksandr Brazhkin
 */
public class DiscrepancyInTimeException extends PrinterException {

    public DiscrepancyInTimeException() {
        super("Time is not synchronized");
    }
}

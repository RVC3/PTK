package ru.ppr.cppk.printer.rx.operation.base;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.exception.ShiftNotClosedException;
import ru.ppr.ikkm.IPrinter;

/**
 * Операция выполняемая ТОЛЬКО в закрытой смене
 *
 * @author Grigoriy Kashka
 */
public abstract class InClosedShiftOperation extends PrinterBaseOperation {

    public InClosedShiftOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    /**
     * Дополнительная операция для всех методов, которые должны работать только в открытой смене
     */
    @Override
    protected void performAdditionalChecks() throws Exception {
        if (printer.isShiftOpened())
            throw new ShiftNotClosedException("Недопустимая операция при открытой смене");
    }

}
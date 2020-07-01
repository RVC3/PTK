package ru.ppr.cppk.printer.rx.operation.base;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.exception.ShiftNotOpenedException;
import ru.ppr.ikkm.IPrinter;

/**
 * Операция выполняемая ТОЛЬКО в открытой смене
 *
 * @author Grigoriy Kashka
 */
public abstract class InOpenedShiftOperation extends PrinterBaseOperation {

    public InOpenedShiftOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    /**
     * Дополнительная операция для всех методов, которые должны работать только в открытой смене
     */
    @Override
    protected void performAdditionalChecks() throws Exception {
        if (!printer.isShiftOpened())
            throw new ShiftNotOpenedException("Недопустимая операция при закрытой смене");
    }

}

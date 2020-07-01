package ru.ppr.cppk.printer.tpl;

import ru.ppr.ikkm.IPrinter;

/**
 * Базовый класс шаблона отчета
 *
 * @author А.Бражкин
 */
public abstract class PrinterTpl {

    public PrinterTpl() {

    }

    public abstract void printToDriver(IPrinter printer) throws Exception;

}

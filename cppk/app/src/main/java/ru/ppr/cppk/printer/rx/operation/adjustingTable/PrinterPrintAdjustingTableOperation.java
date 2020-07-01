package ru.ppr.cppk.printer.rx.operation.adjustingTable;

import rx.Observable;

/**
 * Печать настроечнй таблицы.
 *
 * Created by Dmitry Nevolin on 08.04.2016.
 */
public interface PrinterPrintAdjustingTableOperation {

    Observable<? extends Void> call();

}

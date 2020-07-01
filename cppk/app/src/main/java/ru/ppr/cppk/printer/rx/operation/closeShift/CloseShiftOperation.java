package ru.ppr.cppk.printer.rx.operation.closeShift;

import java.util.Date;

import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import rx.Observable;

/**
 * Операция печати Z-отчета.
 *
 * @author Aleksandr Brazhkin
 */
public interface CloseShiftOperation {

    Observable<Result> call();

    class Params {

        final FiscalHeaderBuilder.Params headerParams;
        final boolean checkEklz;

        public Params(FiscalHeaderBuilder.Params headerParams, boolean checkEklz) {
            this.headerParams = headerParams;
            this.checkEklz = checkEklz;
        }
    }

    class Result {

        private final Date operationTime;

        private final int spndNumber;

        public Result(Date date, int spndNumber) {
            this.operationTime = date;
            this.spndNumber = spndNumber;
        }

        public Date getOperationTime() {
            return operationTime;
        }

        public int getSpndNumber() {
            return spndNumber;
        }
    }

}

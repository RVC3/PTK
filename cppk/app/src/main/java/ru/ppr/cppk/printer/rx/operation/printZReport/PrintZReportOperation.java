package ru.ppr.cppk.printer.rx.operation.printZReport;

import java.util.Date;

import rx.Observable;

/**
 * @author Dmitry Nevolin
 */
public interface PrintZReportOperation {

    Observable<PrintZReportOperation.Result> call();

    class Params {

        final boolean checkEklz;

        public Params(boolean checkEklz) {
            this.checkEklz = checkEklz;
        }

    }

    class Result {

        private final Date operationTime;

        public Result(Date date) {
            operationTime = date;
        }

        public Date getOperationTime() {
            return operationTime;
        }

    }

}

package ru.ppr.cppk.printer.rx.operation.btMonthReport;

import java.util.Date;

import rx.Observable;

/**
 * @author Aleksandr Brazhkin
 */
public interface PrintBtMonthReportOperation {

    Observable<Result> call();

    class Result {

        private Date operationTime;

        public Result(Date date) {
            this.operationTime = date;
        }

        public Date getOperationTime() {
            return operationTime;
        }

    }

}

package ru.ppr.cppk.printer.rx.operation.auditTrail;

import java.util.Date;

import rx.Observable;

public interface PrintAuditTrailOperation {

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

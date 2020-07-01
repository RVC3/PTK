package ru.ppr.cppk.printer.rx.operation;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

public class PrinterGetState extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetState.class);

    public PrinterGetState(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<Result> call() {
        return wrap(() -> {
            Date date = printer.getDate();
            int SPND = printer.getLastSPND();
            int shiftNum = printer.getShiftNum();
            String INN = printer.getINN();
            String regNumber = printer.getRegNumber();
            String EKLZNumber = printer.getEKLZNumber();
            String FNSerial = printer.getFNSerial();
            String model = printer.getModel();
            long availableSpaceForShifts = printer.getAvailableSpaceForShifts();
            boolean isShiftOpened = printer.isShiftOpened();
            BigDecimal cashInFR = printer.getCashInFR();
            Result res = new Result(date, shiftNum, isShiftOpened, SPND, INN, regNumber, EKLZNumber, FNSerial, model, availableSpaceForShifts, cashInFR);
            Logger.trace(TAG, res.toString());
            return res;
        });
    }

    public static class Result {

        private int shiftNum;
        private boolean isShiftOpened;
        private int SPND;
        private String INN;
        private String regNumber;
        private String EKLZNumber;
        private String FNSerial;
        private String model;
        private long availableSpaceForShifts;
        private Date operationTime;
        private BigDecimal cashInFR;

        public Result(Date date, int shiftNum, boolean isShiftOpened, int SPND, String INN, String regNumber, String EKLZNumber, String FNSerial, String model, long availableSpaceForShifts, BigDecimal cashInFR) {
            this.operationTime = date;
            this.shiftNum = shiftNum;
            this.isShiftOpened = isShiftOpened;
            this.SPND = SPND;
            this.INN = INN;
            this.regNumber = regNumber;
            this.EKLZNumber = EKLZNumber;
            this.FNSerial = FNSerial;
            this.model = model;
            this.availableSpaceForShifts = availableSpaceForShifts;
            this.cashInFR = cashInFR;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "shiftNum=" + shiftNum +
                    ", isShiftOpened=" + isShiftOpened +
                    ", SPND=" + SPND +
                    ", INN='" + INN + '\'' +
                    ", regNumber='" + regNumber + '\'' +
                    ", EKLZNumber='" + EKLZNumber + '\'' +
                    ", FNSerial='" + FNSerial + '\'' +
                    ", model='" + model + '\'' +
                    ", availableSpaceForShifts=" + availableSpaceForShifts +
                    ", operationTime=" + operationTime +
                    ", cashInFR=" + cashInFR +
                    '}';
        }

        public int getShiftNum() {
            return shiftNum;
        }

        public boolean isShiftOpened() {
            return isShiftOpened;
        }

        public String getINN() {
            return INN;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public String getEKLZNumber() {
            return EKLZNumber;
        }

        public String getFNSerial() {
            return FNSerial;
        }

        public String getModel() {
            return model;
        }

        public long getAvailableSpaceForShifts() {
            return availableSpaceForShifts;
        }

        public int getSPND() {
            return SPND;
        }

        public Date getOperationTime() {
            return operationTime;
        }

        public BigDecimal getCashInFR() {
            return cashInFR;
        }

    }
}

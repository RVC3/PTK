package ru.ppr.ikkm.file.state;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.Printer;
import ru.ppr.ikkm.exception.SaveStateException;
import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Operator;
import ru.ppr.ikkm.file.state.model.PrinterSettings;
import ru.ppr.ikkm.file.state.model.Shift;
import ru.ppr.ikkm.file.state.model.ShiftInfo;
import ru.ppr.ikkm.file.state.model.ShiftState;
import ru.ppr.ikkm.file.state.storage.PrinterStateStorage;
import ru.ppr.logger.Logger;

/**
 * Управляет виртуальным состоянием принтера
 */
public class StateVirtual implements State {

    private static final String TAG = Logger.makeLogTag(StateVirtual.class);

    private final PrinterStateStorage stateStorage;

    public StateVirtual(PrinterStateStorage stateStorage) {
        this.stateStorage = stateStorage;
    }

    @Override
    public void saveCheck(@NonNull Check check) throws SaveStateException {
        checkLastFiscalOperationTime();

        if (isOpened()) {
            //допишем смену, в рамках который сохраняем чек
            Shift shift = stateStorage.getShift();
            check.setShift(shift);
            stateStorage.addCheck(check);
            //Увеличим СПДН номер(берем его из чека, т.к. там он уже увеличен на 1)
            PrinterSettings printerSettings = stateStorage.getPrinterSetting();
            printerSettings.setCheckNumber(check.getSpdnNumber());
            stateStorage.savePrinterSetting(printerSettings);
        } else {
            throw new SaveStateException("Shift is closed");
        }
    }

    @Override
    public void open(Operator operator) throws SaveStateException {
        checkLastFiscalOperationTime();
        Preconditions.checkNotNull(operator);

        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        int newShiftNumber = printerSettings.getShiftNumber() + 1;
        int spdnNumber = printerSettings.getCheckNumber() + 1;
        Shift shift = new Shift();
        shift.setCashier(operator);
        shift.setOpenShiftTime(new Date());
        shift.setShiftNumber(newShiftNumber);
        shift.setShiftState(ShiftState.OPEN);

        printerSettings.setShiftNumber(newShiftNumber);
        printerSettings.setCheckNumber(spdnNumber);
        // Т.к. эти две операции не в транзакции, то существует верояность
        // что одна выполнится с ошибкой, другая нет, и произойдет
        // небольшая рассинхронизация в номерах смен
        stateStorage.savePrinterSetting(printerSettings);
        stateStorage.saveShift(shift);
    }

    @Override
    public void close() throws SaveStateException {
        checkLastFiscalOperationTime();
        //получаем текущую последнюю смену и допишем соответствующие поля
        Shift openShift = stateStorage.getShift();
        if (openShift == null) {
            throw new IllegalStateException("Shift not open");
        }
        openShift.setShiftState(ShiftState.CLOSE);
        openShift.setCloseShiftTime(new Date());
        stateStorage.saveShift(openShift);
        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        printerSettings.setCheckNumber(printerSettings.getCheckNumber() + 1);
        stateStorage.savePrinterSetting(printerSettings);
    }

    @Override
    public boolean isOpened() {

        Shift shift = stateStorage.getShift();
        return shift != null && shift.getShiftState() == ShiftState.OPEN;
    }

    @Override
    public Operator getOperator() {
        Shift shift = stateStorage.getShift();
        return shift != null ? shift.getCashier() : null;
    }

    @Override
    public void setOperator(Operator operator) throws SaveStateException {
        Preconditions.checkNotNull(operator);
        Shift shift = stateStorage.getShift();
        if (shift != null) {
            //обновляем данные о операторе
            Operator currentOperator = shift.getCashier();
            currentOperator.setOperatorCode(operator.getOperatorCode());
            currentOperator.setOperatorName(operator.getOperatorName());
            stateStorage.setOperator(currentOperator);
        }
    }

    @Override
    public int getLastSPDN() {
        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        return printerSettings.getCheckNumber();
    }

    @Override
    public int getLastShiftNum() {
        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        return printerSettings.getShiftNumber();
    }

    @Override
    public void setHeadersLine(List<String> lines) throws SaveStateException {
        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        printerSettings.setHeaderLines(lines);
        stateStorage.savePrinterSetting(printerSettings);
    }

    @Override
    public List<String> getHeaderLines() {
        return stateStorage.getPrinterSetting().getHeaderLines();
    }

    @Override
    public String getKkmNumber() {
        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        return printerSettings.getSerialNumber();
    }

    @Override
    public void setVatValue(int index, int value) throws SaveStateException {

        if (index > 0 && index < 16) {
            PrinterSettings printerSettings = stateStorage.getPrinterSetting();
            SparseArray<Integer> vatTable = printerSettings.getVatTable();
            vatTable.append(index, value);
            printerSettings.setVatTable(vatTable);
            stateStorage.savePrinterSetting(printerSettings);
        }
    }

    @Override
    public int getVatValue(int index) {

        int value = -1;
        if (index >= 0 && index < 16) {
            PrinterSettings printerSettings = stateStorage.getPrinterSetting();
            value = printerSettings.getVatTable().get(index);
        }
        return value;
    }

    @Override
    public Date getPrinterDate() {
        //берем время устройства
        return new Date();
    }

    @Override
    public Date getLastCheckTime() {

        // если сильно будет тормозить постоянная загрузка из бд, то можно реализовать отдельный запрос,
        // который будет возвращать чек или время чека
        final Shift shift = stateStorage.getShift();
        Preconditions.checkNotNull(shift, "No shifts!");
        final List<Check> checks = shift.getChecks();
        Date printTime;
        if (checks.isEmpty()) {
            printTime = shift.getOpenShiftTime();
        } else {
            Check check = checks.get(checks.size() - 1);
            printTime = check.getPrintTime();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(printTime);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    @Override
    public String getInn() {
        return stateStorage.getPrinterSetting().getInn();
    }

    @Override
    public String getRegisterNumber() {
        return stateStorage.getPrinterSetting().getRegisterNumber();
    }

    @Override
    public Date getOpenDate() {

        Date openDate = null;
        Shift shift = stateStorage.getShift();
        if (shift != null) {
            openDate = shift.getOpenShiftTime();
        }
        return openDate;
    }

    @Override
    public String getEklz() {
        return stateStorage.getPrinterSetting().getEklzNumber();
    }

    @Override
    public BigDecimal getTotalForShift() {

        Totals total;
        Shift shift = stateStorage.getShift();
        if (shift != null) {
            total = calculateTotalForShift(shift);
        } else {
            total = new Totals(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return total.sell;
    }

    @Override
    public BigDecimal getTotal() {

        List<Shift> shifts = stateStorage.getAllShift();
        BigDecimal totalSell = BigDecimal.ZERO;
        for (Shift shift : shifts) {
            Totals totalForShift = calculateTotalForShift(shift);
            totalSell = totalSell.add(totalForShift.sell);
        }
        return totalSell;
    }

    @Override
    public long getOdometerValue() {
        return stateStorage.getPrinterSetting().getOdometerValue();
    }

    @Override
    public long getAvailableSpaceForDocs() {
        return stateStorage.getPrinterSetting().getAvailableDocs();
    }

    @Override
    public long getAvailableSpaceForShifts() {
        return stateStorage.getPrinterSetting().getAvailableShifts();
    }

    @Override
    public void appendOdometrValue(long millimeter) {
        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        printerSettings.setOdometerValue(printerSettings.getOdometerValue() + millimeter);
        try {
            stateStorage.savePrinterSetting(printerSettings);
        } catch (SaveStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decrementAvailableSpaceForDocs() throws SaveStateException {
        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        printerSettings.setAvailableDocs(printerSettings.getAvailableDocs() - 1);
        stateStorage.savePrinterSetting(printerSettings);
    }

    @Override
    public void decrementAvailableSpaceForShifts() throws SaveStateException {
        PrinterSettings printerSettings = stateStorage.getPrinterSetting();
        printerSettings.setAvailableShifts(printerSettings.getAvailableShifts() - 1);
        stateStorage.savePrinterSetting(printerSettings);
    }

    @Override
    public List<ShiftInfo> getShiftInfo(@Nullable Date startDate, @Nullable Date endDate) {
        Logger.trace(TAG, "Get shift info for startDate - " + String.valueOf(startDate)
                + ", end date - " + String.valueOf(endDate));


        Collection<Shift> filteredShifts = Collections2.filter(stateStorage.getAllShift(), shift ->
                (startDate == null || shift.getOpenShiftTime().after(startDate))
                        && (endDate == null || shift.getOpenShiftTime().before(endDate)));

        Collection<ShiftInfo> modifiedShifts = Collections2.transform(filteredShifts, shift -> {
            final List<Check> checks = shift.getChecks();

            BigDecimal sumForShift = new BigDecimal(0);

            for (Check check : checks) {
                if (IPrinter.DocType.SALE.equals(check.getType())) {
                    sumForShift = sumForShift.add(check.getTotal());
                }
            }

            final ShiftInfo.Builder builder = new ShiftInfo.Builder();
            return builder.shiftNumber(shift.getShiftNumber())
                    .closeTime(shift.getCloseShiftTime())
                    .summForShift(sumForShift)
                    .build();
        });

        return ImmutableList.copyOf(modifiedShifts);
    }

    @Override
    public void closeState() {
        stateStorage.close();
    }

    private Totals calculateTotalForShift(@NonNull Shift shift) {
        BigDecimal totalSell = BigDecimal.ZERO;
        BigDecimal totalRepeal = BigDecimal.ZERO;
        List<Check> checks = shift.getChecks();
        for (Check check : checks) {
            IPrinter.DocType currentType = check.getType();
            // Считаем только продажи
            if (Printer.DocType.SALE.equals(currentType)) {
                totalSell = totalSell.add(check.getTotal());
            } else if (Printer.DocType.RETURN.equals(currentType)) {
                totalRepeal = totalRepeal.add(check.getTotal());
            }
        }
        return new Totals(totalSell, totalRepeal);
    }

    private void checkLastFiscalOperationTime() throws SaveStateException {
        try {
            final Shift shift = stateStorage.getShift();
            final Date lastCheckTime = getLastCheckTime();

            final long currentTime = System.currentTimeMillis();
            final long checkTime = lastCheckTime.getTime();
            final long shiftTime = shift == null ? 0 : (shift.getCloseShiftTime() == null ? shift.getOpenShiftTime().getTime() : shift.getCloseShiftTime().getTime());

            if (shiftTime > currentTime || checkTime > currentTime) {
                throw new SaveStateException("You tryin' to do a fiscal operation in the past!");
            }
        } catch (NullPointerException | IllegalArgumentException exception) {
            exception.printStackTrace();
        }
    }

    static class Totals {
        public final BigDecimal sell;
        public final BigDecimal repeal;

        Totals(BigDecimal sell, BigDecimal repeal) {
            this.sell = sell;
            this.repeal = repeal;
        }
    }
}

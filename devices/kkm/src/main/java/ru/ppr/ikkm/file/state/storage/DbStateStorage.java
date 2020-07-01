package ru.ppr.ikkm.file.state.storage;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.ikkm.file.db.PrinterDaoSession;
import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Operator;
import ru.ppr.ikkm.file.state.model.PrinterSettings;
import ru.ppr.ikkm.file.state.model.Shift;

/**
 * Класс для управления состоянием в БД
 * Created by Артем on 21.01.2016.
 */
public class DbStateStorage implements PrinterStateStorage {

    private final PrinterDaoSession printerDaoSession;
    private final long stateId;
    private final String model;

    public DbStateStorage(PrinterDaoSession printerDaoSession, long stateId, String model) {
        this.printerDaoSession = printerDaoSession;
        this.stateId = stateId;
        this.model = model;
    }

    @Override
    public void addCheck(@NonNull Check check) {
        printerDaoSession.getCheckDao().save(check);
    }

    @Override
    public void setOperator(@NonNull Operator operator) {
        printerDaoSession.getCashierDao().saveOrReplace(operator, stateId);
    }

    @Override
    public Shift getShift() {
        return printerDaoSession.getShiftDao().loadLast(stateId);
    }

    @Override
    public void saveShift(Shift shift) {
        printerDaoSession.getShiftDao().save(shift, stateId);
    }

    @Override
    public List<Shift> getAllShift() {
        return printerDaoSession.getShiftDao().loadAll(stateId);
    }

    @NonNull
    @Override
    public PrinterSettings getPrinterSetting() {
        return printerDaoSession.getPrinterSettingDao().loadFirst(stateId, model);
    }

    @Override
    public void savePrinterSetting(@NonNull PrinterSettings printerSettings) {
        printerDaoSession.getPrinterSettingDao().saveOrUpdate(printerSettings);
    }

    @Override
    public void close() {
        printerDaoSession.getHelper().close();
    }
}

package ru.ppr.cppk.debug;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View.OnClickListener;

import com.androidquery.AQuery;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import ru.ppr.core.helper.Toaster;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.settings.LocalUser;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.ui.helper.ShiftEventStatusStringifier;

/**
 * Дебажное окно ручного управления состоянием смены
 */
public class ShiftManagerActivity extends LoggedActivity {

    private AQuery aQuery = null;

    private Holder<PrivateSettings> privateSettingsHolder;
    private LocalDaoSession localDaoSession;
    private ShiftManager shiftManager;
    private PaperUsageCounter paperUsageCounter;
    private Toaster toaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Dagger.appComponent().privateSettingsHolder();
        localDaoSession = Dagger.appComponent().localDaoSession();
        shiftManager = Dagger.appComponent().shiftManager();
        paperUsageCounter = Dagger.appComponent().paperUsageCounter();
        toaster = Dagger.appComponent().toaster();

        setContentView(R.layout.debug_manage_shift_status);

        aQuery = new AQuery(this);
        aQuery.id(R.id.debug_manage_shift_close_shift).clicked(closeClickListener);
        aQuery.id(R.id.debug_manage_shift_open_shift).clicked(openClickListener);
        aQuery.id(R.id.debug_manage_shift_transfer_shift).clicked(transferClickListener);
        aQuery.id(R.id.debug_manage_shift_time_to_warning).clicked(v -> timeToWarning(true));
        aQuery.id(R.id.debug_manage_shift_time_to_close).clicked(v -> timeToWarning(false));


        updateView(aQuery);
    }

    /**
     * Производит изменение времени для птк, на время близкое к закрытию смены либо к показу
     * предупреждения о закрытии смены
     *
     * @param forWarning true - время переведется на близкое к показу предупреждения о закрытии смены,
     *                   false - время переведется на близкое к закрытию смены
     */
    private void timeToWarning(boolean forWarning) {
        final ShiftEvent shiftEvent = ShiftManager.getInstance().getCurrentShiftEvent();
        if (shiftEvent != null) {
            final ShiftEvent.Status shiftStatus = shiftEvent.getStatus();
            if (ShiftEvent.Status.ENDED.equals(shiftStatus)) {
                toaster.showToast("Смена закрыта!");
                return;
            }

            final Date shiftStartTime = shiftEvent.getStartTime();
            int minuteToCloseShift;
            if (forWarning) {
                final PrivateSettings privateSettings = privateSettingsHolder.get();
                minuteToCloseShift = privateSettings.getTimeForShiftCloseMessage() + 1;
            } else {
                minuteToCloseShift = 1;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(shiftStartTime);
            calendar.add(Calendar.DAY_OF_MONTH, 1); //определяем время, в которое смена должна быть закрыта
            // вычитаем количество минут, за которое необходимо показать уведомление,
            // так же вычитаем еще 1 минуту чтобы уведомление не показывалось моментально
            calendar.add(Calendar.MINUTE, (minuteToCloseShift) * -1);
            changeTimeForShift(calendar.getTime());
        } else {
            toaster.showToast("Смен не было");
        }
    }

    private void changeTimeForShift(@NonNull Date date) {

        if (date.before(Calendar.getInstance().getTime())) {
            throw new IllegalArgumentException("New time is incorrect - " + date);
        }

        final int printerMode = Di.INSTANCE.printerManager().getPrinterMode();
        switch (printerMode) {
            case PrinterManager.PRINTER_MODE_MOEBIUS_REAL:
                toaster.showToast("Время можно изменять только для виртуальных принтеров");
                return;
            default:
        }

        SystemClock.setCurrentTimeMillis(date.getTime());
    }

    private void updateView(AQuery aQuery) {

        // последнее событие для смены
        ShiftEvent shiftEvent = ShiftManager.getInstance().getCurrentShiftEvent();
        if (shiftEvent != null) {
            ShiftEvent.Status shiftStatus = shiftEvent.getStatus();
            aQuery.id(R.id.debug_manage_shift_current_state_shift).text(new ShiftEventStatusStringifier().stringify(shiftStatus));
            aQuery.id(R.id.debug_manage_shift_current_number_statet).text(String.valueOf(shiftEvent.getShiftNumber()));
            aQuery.id(R.id.debug_manage_shift_open_time).text(DateFormatOperations.getDateForOut(shiftEvent.getStartTime()));
            aQuery.id(R.id.debug_manage_shift_last_status_time_change).text(DateFormatOperations.getDateForOut(shiftEvent.getOperationTime()));
            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(shiftEvent.getCashRegisterEventId());
            Cashier cashier = localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId());
            aQuery.id(R.id.debug_manage_shift_current_user).text(cashier.getFio());
            aQuery.id(R.id.debug_manage_shift_current_user_login).text(cashier.getLogin());
            aQuery.id(R.id.debug_manage_shift_official_code).text(String.valueOf(cashier.getOfficialCode()));
        }
    }

    /**
     * Моделирует событие открытия смены
     */
    private OnClickListener openClickListener = v -> {

        if (shiftManager.isShiftOpened()) {
            toaster.showToast("Смена не была закрыта - открытие не возможно");
            return;
        }

        //т.к. мы залогинились заново - обновляем данные для cashier
        LocalUser currentUser = Di.INSTANCE.getUserSessionInfo().getCurrentUser();
        Cashier currentCashier = new Cashier();
        currentCashier.setFio(currentUser.getName());
        currentCashier.setLogin(currentUser.getLogin());
        Di.INSTANCE.getCashierSessionInfo().setCurrentCashier(currentCashier);

        CashRegisterEvent lastCashRegisterEvent = localDaoSession.getCashRegisterEventDao().getLastCashRegisterEvent();
        Cashier cashier = null;
        if(lastCashRegisterEvent != null) {
            cashier = localDaoSession.cashierDao().load(lastCashRegisterEvent.getCashierId());
        }
        if (lastCashRegisterEvent != null && !TextUtils.equals(cashier.getOfficialCode(), Cashier.DEFAULT_OFFICIAL_CODE)) {
            // Текущий кассир  с officialCode > 1, сбросим до 1 при открытии смены
            CashRegister cashRegister = Di.INSTANCE.printerManager().getCashRegister();
            Dagger.appComponent().cashRegisterEventCreator()
                    .setCashRegister(cashRegister)
                    .create();
        }

        ShiftEvent lastShiftEvent = shiftManager.getCurrentShiftEvent();
        int shiftNum = lastShiftEvent != null ? lastShiftEvent.getShiftNumber() + 1 : 1;
        Date openTime = new Date();

        try {
            paperUsageCounter.resetPaperUsage(PaperUsage.ID_SHIFT);
            Check check = localDaoSession.getCheckDao().getLastCheck();
            int spnd = check == null ? 0 : check.getSnpdNumber();
            shiftManager.openShift(shiftNum, spnd, openTime, BigDecimal.ZERO);
            updateView(aQuery);
        } catch (Exception e) {
            e.printStackTrace();
            toaster.showToast("Открытие смены завершилось с ошибкой.");
        }

    };

    /**
     * моделирует событие закрытия смены
     */
    private OnClickListener closeClickListener = v -> {

        if (!shiftManager.isShiftOpened()) {
            toaster.showToast("Смена не была открыта - закрытие смены не возможно");
            return;
        }

        int spndNumber = localDaoSession.getCheckDao().getLastCheck().getSnpdNumber() + 1;
        PaperUsage paperUsage = paperUsageCounter.getPaperUsage(PaperUsage.ID_SHIFT);

        localDaoSession.beginTransaction();
        try {
            ShiftEvent closeShiftEvent = shiftManager.createCloseShiftEvent(paperUsage.getPaperLength(), paperUsage.isRestarted(), BigDecimal.ZERO);
            shiftManager.closeShiftEventUpdateToComplete(new Date(), closeShiftEvent.getId(), spndNumber);
            localDaoSession.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            toaster.showToast("Закрытие смены завершилось с ошибкой.");
        } finally {
            localDaoSession.endTransaction();
        }

        updateView(aQuery);

    };

    /**
     * Моделирует событие передачи смены
     */
    private OnClickListener transferClickListener = v -> {

        if (!shiftManager.isShiftOpened()) {
            toaster.showToast("Смена закрыта - закрытую смену нельзя передать");
            return;
        }

        //ParametrsPtkEntity parametrsPtkEntity = ParametrsPtkEntity.getParametrsPtkEntity(globals);
        //т.к. мы залогинились заново - обновляем данные для cashier
        LocalUser currentUser = Di.INSTANCE.getUserSessionInfo().getCurrentUser();
        Cashier currentCashier = new Cashier();
        currentCashier.setFio(currentUser.getName());
        currentCashier.setLogin(currentUser.getLogin());
        Di.INSTANCE.getCashierSessionInfo().setCurrentCashier(currentCashier);

        //т.к. это передача смены - увеличиваем officialCode
        CashRegisterEvent lastCashRegisterEvent = localDaoSession.getCashRegisterEventDao().getLastCashRegisterEvent();
        Cashier previousCashier = lastCashRegisterEvent == null ? null : localDaoSession.cashierDao().load(lastCashRegisterEvent.getCashierId());
        currentCashier.setOfficialCode(String.valueOf(Integer.valueOf(previousCashier.getOfficialCode()) + 1));

        CashRegister cashRegister = Di.INSTANCE.printerManager().getCashRegister();
        Dagger.appComponent().cashRegisterEventCreator()
                .setCashRegister(cashRegister)
                .create();

        try {
            shiftManager.transferShift();
            updateView(aQuery);
        } catch (Exception e) {
            e.printStackTrace();
            toaster.showToast("Передача смены завершилась с ошибкой.");
        }
    };
}

package ru.ppr.cppk.logic;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.UUID;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.builder.CheckBuilder;
import ru.ppr.logger.Logger;

/**
 * Менеджер для работы со сменой.
 *
 * @author Aleksandr Brazhkin
 */
public class ShiftManager {

    private static final String TAG = Logger.makeLogTag(ShiftManager.class);

    /**
     * @deprecated Use {@link Globals#getShiftManager()} instead
     */
    public static ShiftManager getInstance() {
        return Globals.getInstance().getShiftManager();
    }

    private ShiftEvent currentShiftEvent;
    private ShiftEvent startShiftEvent;
    private ShiftEvent endShiftEvent;

    private ShiftAlarmManager shiftAlarmManager;

    private boolean isShiftOpened;
    private boolean isShiftOpenedWithTestPd;

    private final Globals globals;

    public ShiftManager(Globals globals, ShiftAlarmManager shiftAlarmManager) {
        this.globals = globals;
        this.shiftAlarmManager = shiftAlarmManager;
        Logger.trace(TAG, "ShiftManager initialized");
    }

    private void destroy() {
        Logger.trace(TAG, "ShiftManager destroyed");
    }

    public boolean isShiftOpened() {
        return isShiftOpened;
    }

    public boolean isShiftClosed() {
        return !isShiftOpened;
    }

    public boolean isShiftOpenedWithTestPd() {
        return isShiftOpenedWithTestPd;
    }

    public boolean isShiftOpenedWithoutTestPd() {
        return isShiftOpened && !isShiftOpenedWithTestPd;
    }

    public ShiftEvent.Status getCurrentShiftStatus() {
        return currentShiftEvent == null ? ShiftEvent.Status.ENDED : currentShiftEvent.getStatus();
    }

    public int getCurrentShiftNumber() {
        return currentShiftEvent == null ? 0 : currentShiftEvent.getShiftNumber();
    }

    public ShiftEvent getCurrentShiftEvent() {
        return currentShiftEvent;
    }

    public ShiftEvent getStartShiftEvent() {
        return startShiftEvent;
    }

    public ShiftEvent getEndShiftEvent() {
        return endShiftEvent;
    }

    public String getCurrentShiftId() {
        return currentShiftEvent == null ? null : currentShiftEvent.getShiftId();
    }

    public void refreshState() {
        boolean isShiftOpenedPreviousState = isShiftOpened;
        currentShiftEvent = getLocalDaoSession().getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        isShiftOpened = isShiftOpenedWithTestPd = currentShiftEvent != null && currentShiftEvent.getStatus() != ShiftEvent.Status.ENDED;

        if (isShiftOpenedWithTestPd) {
            TestTicketEvent testTicketEvent = getLocalDaoSession().getTestTicketDao().getFirstTestTicketForShift(currentShiftEvent.getShiftId(), EnumSet.of(TestTicketEvent.Status.CHECK_PRINTED, TestTicketEvent.Status.COMPLETED));
            isShiftOpenedWithTestPd = testTicketEvent != null;
        }

        if (currentShiftEvent != null) {
            switch (currentShiftEvent.getStatus()) {
                case STARTED: {
                    startShiftEvent = currentShiftEvent;
                    endShiftEvent = null;
                    break;
                }
                case TRANSFERRED: {
                    startShiftEvent = getLocalDaoSession().getShiftEventDao().getFirstShiftEventByShiftId(currentShiftEvent.getShiftId(), ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
                    endShiftEvent = null;
                }
                case ENDED: {
                    startShiftEvent = getLocalDaoSession().getShiftEventDao().getFirstShiftEventByShiftId(currentShiftEvent.getShiftId(), ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
                    endShiftEvent = currentShiftEvent;
                    break;
                }
            }
        }

        if (isShiftOpenedPreviousState != isShiftOpened) {
            if (isShiftOpened && GlobalConstants.ENABLE_AUTO_CLOSE_SHIFT) {
                shiftAlarmManager.startAlarmsForShift(
                        //берем локальное время открытия смены
                        //http://agile.srvdev.ru/browse/CPPKPP-37406
                        getLocalDaoSession().getEventDao().load(startShiftEvent.getEventId()).getCreationTimestamp(),
                        getPrivateSettings().getTimeForShiftCloseMessage());
            } else {
                shiftAlarmManager.stopAlarms();
            }
        }
    }

    private PrivateSettings getPrivateSettings() {
        return globals.getPrivateSettingsHolder().get();
    }

    private LocalDaoSession getLocalDaoSession() {
        return Globals.getInstance().getLocalDaoSession();
    }

    public void openShift(int shiftNum, int spndNumber, Date openTime, BigDecimal cashInFR) throws Exception {

        // Обрезаем секунды для времени открытия смены
        // http://agile.srvdev.ru/browse/CPPKPP-33040
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(openTime);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date openTimeRoundedToMinutes = calendar.getTime();

        Check check = new CheckBuilder()
                .setDocumentNumber(0)
                .setSnpdNumber(spndNumber)
                .setPrintDateTime(openTime)
                .build();

        Dagger.appComponent().shiftEventCreator()
                .setShiftId(UUID.randomUUID().toString())
                .setStatus(ShiftEvent.Status.STARTED)
                .setShiftNumber(shiftNum)
                .setStartTime(openTimeRoundedToMinutes)
                .setOperationTime(openTimeRoundedToMinutes)
                .setPaperConsumption(0)
                .setPaperCounterRestarted(false)
                .setCashInFR(cashInFR)
                .setProgressStatus(ShiftEvent.ShiftProgressStatus.COMPLETED) //не будем сейчас заморачиваться на статусы для событий открытия смены
                .setCheck(check)
                .create();

        Logger.info(TAG, "Смена " + shiftNum + " успешно открыта: " + DateFormatOperations.getDateddMMyyyyNHHmmss(openTimeRoundedToMinutes));
        refreshState();
    }

    public void transferShift() throws Exception {

        Date transferTime = new Date();

        Dagger.appComponent().shiftEventCreator()
                .setShiftId(currentShiftEvent.getShiftId())
                .setStatus(ShiftEvent.Status.TRANSFERRED)
                .setShiftNumber(currentShiftEvent.getShiftNumber())
                .setStartTime(currentShiftEvent.getStartTime())
                .setOperationTime(transferTime)
                .setPaperConsumption(0)
                .setPaperCounterRestarted(false)
                .setCashInFR(null) // Пока нет регистрации суммы в ФР при передаче смены
                .setProgressStatus(ShiftEvent.ShiftProgressStatus.COMPLETED)
                .setCheck(null)
                .create();

        Logger.info(TAG, "Смена " + currentShiftEvent.getShiftNumber() + " успешно передана: " + DateFormatOperations.getDateddMMyyyyNHHmmss(transferTime));
        refreshState();
    }

    /**
     * Обновляет событие закрытия смены до состояния COMPLETED
     *
     * @param closeTime
     * @param shiftEventId
     * @param spndNumber
     */
    public void closeShiftEventUpdateToComplete(Date closeTime, long shiftEventId, int spndNumber) {

        ShiftEvent shiftEvent = getLocalDaoSession().getShiftEventDao().load(shiftEventId);

        getLocalDaoSession().beginTransaction();
        try {

            // В будущем выпилить этот костыль и сделать отдельную сущность для хранения фискальных документов принтера
            // Чек
            Check check = new CheckBuilder()
                    .setDocumentNumber(0)
                    .setSnpdNumber(spndNumber)
                    .setPrintDateTime(closeTime)
                    .build();
            long checkId = getLocalDaoSession().getCheckDao().insertOrThrow(check);

            shiftEvent.setCheckId(checkId);
            shiftEvent.setProgressStatus(ShiftEvent.ShiftProgressStatus.COMPLETED);
            shiftEvent.setCloseTime(closeTime);

            getLocalDaoSession().getShiftEventDao().update(shiftEvent);
            getLocalDaoSession().setTransactionSuccessful();
        } finally {
            getLocalDaoSession().endTransaction();
        }

        Logger.info(TAG, "closeShiftEventUpdateToComplete(shiftEventId=" + shiftEventId + ") Смена " + shiftEvent.getShiftNumber() + " успешно закрыта: " + DateFormatOperations.getDateddMMyyyyNHHmmss(closeTime));
        refreshState();
    }

    /**
     * Создать событие закрытия смены со статусом PRE_PRINTING
     *
     * @param paperConsumption
     * @param paperCounterRestarted
     * @param cashInFR
     * @return
     */
    public ShiftEvent createCloseShiftEvent(long paperConsumption, boolean paperCounterRestarted, BigDecimal cashInFR) {
        if (!isShiftOpened()) {
            // При закрытии смены текущее состояние должно быть
            // и статус должен быть открыта или передана
            throw new IllegalStateException("Shift is not opened");
        }

        ShiftEvent shiftEvent = Dagger.appComponent().shiftEventCreator()
                .setShiftId(currentShiftEvent.getShiftId())
                .setStatus(ShiftEvent.Status.ENDED)
                .setShiftNumber(currentShiftEvent.getShiftNumber())
                .setStartTime(currentShiftEvent.getStartTime())
                .setOperationTime(new Date())
                .setCloseTime(null)
                .setPaperConsumption(paperConsumption)
                .setPaperCounterRestarted(paperCounterRestarted)
                .setCashInFR(cashInFR)
                .setProgressStatus(ShiftEvent.ShiftProgressStatus.PRE_PRINTING)
                .setCheck(null)
                .create();

        Logger.info(TAG, "Создано событие закрытия смены №" + shiftEvent.getShiftNumber() +
                " со статусом: " + shiftEvent.getProgressStatus().toString() +
                " время операции:" + DateFormatOperations.getDateddMMyyyyNHHmmss(shiftEvent.getOperationTime()));
        return shiftEvent;
    }

}

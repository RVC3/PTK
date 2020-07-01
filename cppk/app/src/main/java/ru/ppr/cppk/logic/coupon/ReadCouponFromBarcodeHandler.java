package ru.ppr.cppk.logic.coupon;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import ru.ppr.core.dataCarrier.coupon.base.Coupon;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.CouponReadEvent;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PtsKey;

/**
 * Обработчик события считывания ШК, содержащего талон ТППД.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadCouponFromBarcodeHandler {

    private static final String TAG = Logger.makeLogTag(ReadCouponFromBarcodeHandler.class);

    private final CouponChecker couponChecker;
    private final LocalDaoSession localDaoSession;
    private final ShiftManager shiftManager;
    private final EventBuilder eventBuilder;

    public ReadCouponFromBarcodeHandler(CouponChecker couponChecker,
                                        LocalDaoSession localDaoSession,
                                        ShiftManager shiftManager,
                                        EventBuilder eventBuilder) {
        this.couponChecker = couponChecker;
        this.localDaoSession = localDaoSession;
        this.shiftManager = shiftManager;
        this.eventBuilder = eventBuilder;
    }

    /**
     * Выполняет обработку считанного талона.
     * Разбирает купон и создает событие в БД.
     *
     * @param coupon Талон
     * @return id созданного события в БД, -1, если не удалось обработать талон.
     */
    public long handle(@NonNull Coupon coupon) {

        Logger.trace(TAG, "handle, couponNumber = " + coupon.getNumber());

        CouponChecker.Result checkResult = couponChecker.check(coupon.getNumber());

        Logger.trace(TAG, "handle, checkResult = " + checkResult);

        if (checkResult.isValid()) {
            return createCouponReadEvent(coupon, checkResult);
        }

        return -1;
    }

    /**
     * Добавляет событие {@link CouponReadEvent} в БД.
     *
     * @param checkResult Результат проверки номера талона.
     * @return id созданного события в БД
     */
    private long createCouponReadEvent(@NonNull Coupon coupon, @NonNull CouponChecker.Result checkResult) {
        localDaoSession.beginTransaction();
        try {
            ShiftEvent shiftEvent = shiftManager.getCurrentShiftEvent();
            // добавляем информацию о ПТК
            StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
            if (stationDevice != null) {
                localDaoSession.getStationDeviceDao().insertOrThrow(stationDevice);
            }
            Event event = eventBuilder
                    .setDeviceId(stationDevice.getId())
                    .build();
            localDaoSession.getEventDao().insertOrThrow(event);
            // CouponReadEvent
            PtsKey ptsKey = checkResult.getPtsKey();
            CouponReadEvent couponReadEvent = new CouponReadEvent();
            couponReadEvent.setPreTicketNumber(coupon.getNumber());
            couponReadEvent.setPrintDateTime(calcPrintDateTime(checkResult));
            couponReadEvent.setDeviceId(String.valueOf(ptsKey.getComplexInstanceId()));
            couponReadEvent.setStationCode(ptsKey.getStationExpressCode());
            couponReadEvent.setPtsKeyId(ptsKey == null ? null : ptsKey.getId());
            couponReadEvent.setStatus(CouponReadEvent.Status.CREATED);
            couponReadEvent.setEventId(event == null ? -1 : event.getId());
            couponReadEvent.setShiftEventId(shiftEvent == null ? -1 : shiftEvent.getId());
            long couponReadEventId = localDaoSession.getCouponReadEventDao().insertOrThrow(couponReadEvent);
            localDaoSession.setTransactionSuccessful();
            return couponReadEventId;
        } finally {
            localDaoSession.endTransaction();
        }
    }

    private Date calcPrintDateTime(@NonNull CouponChecker.Result checkResult) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(checkResult.getPtsKey().getValidFromTimeUtc());
        calendar.add(Calendar.SECOND, checkResult.getTimestamp());

        Calendar secondsPrecision = Calendar.getInstance();
        secondsPrecision.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

        return secondsPrecision.getTime();
    }
}

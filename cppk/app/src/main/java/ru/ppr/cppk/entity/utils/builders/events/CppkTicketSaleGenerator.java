package ru.ppr.cppk.entity.utils.builders.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.CouponReadEvent;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * В данный генератор отдельно передается ticketTypeShortName для того,
 * чтобы отвязаться от объекта Globals. Пусть этим занимается "вызывающая сторона".
 * В дальнейшем, когда каждый объект будет содержать в себе ссылку на {@link NsiDaoSession},
 * можно будет оставить только tariff, и из него уже забирать нужные данные.
 * <p>
 * Created by Артем on 28.12.2015.
 */
public class CppkTicketSaleGenerator extends AbstractGenerator implements Generator<CPPKTicketSales> {

    private Event event = null;

    @Nullable
    private TicketStorageType storageTicketType;
    private TicketTapeEvent ticketTapeEvent;
    private ProgressStatus progressStatus;
    private BigDecimal fullTicketPrice;
    private CouponReadEvent couponReadEvent;
    private ConnectionType connectionType;
    private TicketSaleReturnEventBase ticketSaleReturnEventBase;

    public CppkTicketSaleGenerator setTicketSaleReturnEventBase(TicketSaleReturnEventBase ticketSaleReturnEventBase) {
        this.ticketSaleReturnEventBase = ticketSaleReturnEventBase;
        return this;
    }

    public CppkTicketSaleGenerator setTicketTapeEvent(TicketTapeEvent ticketTapeEvent) {
        this.ticketTapeEvent = ticketTapeEvent;
        return this;
    }

    public CppkTicketSaleGenerator setStorageTicketType(TicketStorageType storageTicketType) {
        this.storageTicketType = storageTicketType;
        return this;
    }

    public CppkTicketSaleGenerator setCouponReadEvent(CouponReadEvent couponReadEvent) {
        this.couponReadEvent = couponReadEvent;
        return this;
    }

    public CppkTicketSaleGenerator setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
        return this;
    }

    public CppkTicketSaleGenerator setEvent(Event event) {
        this.event = event;
        return this;
    }

    public CppkTicketSaleGenerator setProgressStatus(ProgressStatus progressStatus) {
        this.progressStatus = progressStatus;

        return this;
    }

    public CppkTicketSaleGenerator setFullTicketPrice(BigDecimal fullTicketPrice) {
        this.fullTicketPrice = fullTicketPrice;

        return this;
    }

    @NonNull
    @Override
    public CPPKTicketSales build() {

        if (ticketTapeEvent == null || progressStatus == null || fullTicketPrice == null
                || event == null || storageTicketType == null || ticketSaleReturnEventBase == null) {
            throw new IllegalArgumentException("Not all entities for insert to database is created");
        }

        CPPKTicketSales sales = new CPPKTicketSales();

        sales.setEventId(event.getId());
        sales.setTicketSaleReturnEventBaseId(ticketSaleReturnEventBase.getId());
        sales.setStorageTypeCode(storageTicketType);
        sales.setTicketTapeEventId(ticketTapeEvent.getId());
        sales.setProgressStatus(progressStatus);
        sales.setFullTicketPrice(fullTicketPrice);
        sales.setCouponReadEventId(couponReadEvent == null ? -1 : couponReadEvent.getId());
        sales.setConnectionType(connectionType);

        return sales;
    }
}

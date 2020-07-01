package ru.ppr.cppk.entity.utils.builders.events;

import com.google.common.base.Preconditions;

import java.util.Date;

import ru.ppr.cppk.entity.event.base34.CPPKServiceSale;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.nsi.entity.ServiceFee;

/**
 * Билдер события продажи услуг.
 *
 * @author Aleksandr Brazhkin
 */
public class CPPKServiceSaleBuilder {

    private Event event;
    private ShiftEvent shiftEvent;
    private TicketTapeEvent ticketTapeEvent;
    private Check check;
    private Price price;
    private ServiceFee serviceFee;
    private Date saleDateTime;

    public CPPKServiceSaleBuilder setEvent(Event event) {
        this.event = event;
        return this;
    }

    public CPPKServiceSaleBuilder setShiftEvent(ShiftEvent shiftEvent) {
        this.shiftEvent = shiftEvent;
        return this;
    }

    public CPPKServiceSaleBuilder setTicketTapeEvent(TicketTapeEvent ticketTapeEvent) {
        this.ticketTapeEvent = ticketTapeEvent;
        return this;
    }

    public CPPKServiceSaleBuilder setCheck(Check check) {
        this.check = check;
        return this;
    }

    public CPPKServiceSaleBuilder setPrice(Price price) {
        this.price = price;
        return this;
    }

    public CPPKServiceSaleBuilder setServiceFee(ServiceFee serviceFee) {
        this.serviceFee = serviceFee;
        return this;
    }

    public CPPKServiceSaleBuilder setSaleDateTime(Date saleDateTime) {
        this.saleDateTime = saleDateTime;
        return this;
    }

    public CPPKServiceSale build() {
        Preconditions.checkNotNull(event, "event is null");
        Preconditions.checkNotNull(shiftEvent, "shiftEvent is null");
        Preconditions.checkNotNull(price, "price is null");
        Preconditions.checkNotNull(serviceFee, "serviceFee is null");
        Preconditions.checkNotNull(saleDateTime, "saleDateTime is null");
        Preconditions.checkNotNull(check, "check is null");

        CPPKServiceSale cppkServiceSale = new CPPKServiceSale();
        cppkServiceSale.setEventId(event.getId());
        cppkServiceSale.setShiftEventId(shiftEvent.getId());
        cppkServiceSale.setTicketTapeEventId(ticketTapeEvent == null ? -1 : ticketTapeEvent.getId());
        cppkServiceSale.setCheckId(check.getId());
        cppkServiceSale.setPriceId(price.getId());
        cppkServiceSale.setServiceFeeCode(serviceFee.getCode());
        cppkServiceSale.setServiceFeeName(serviceFee.getName());
        cppkServiceSale.setSaleDateTime(saleDateTime);

        return cppkServiceSale;
    }
}

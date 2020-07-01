package ru.ppr.cppk.entity.utils.builders.events;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketWayType;

/**
 * Created by Артем on 16.12.2015.
 */
public class TicketEventBaseGenerator extends AbstractGenerator implements Generator<TicketEventBase> {

    private TicketWayType wayType;
    private Date saleTime;
    private Long tariffCode;
    private Integer destinationStationCode;
    private Integer departureStationCode;
    private String ticketTypeShortName;
    private Integer ticketTypeCode;
    private int startDayOffset;
    private Date validFromDate;
    private Date validTillDate;
    private SmartCard smartCard;
    private Integer ticketCategoryCode;
    private String type;
    private ShiftEvent currentShift;

    public TicketEventBaseGenerator setCurrentShift(ShiftEvent currentShift) {
        this.currentShift = currentShift;
        return this;
    }

    public TicketEventBaseGenerator setTicketCategoryCode(Integer ticketCategoryCode) {
        this.ticketCategoryCode = ticketCategoryCode;
        return this;
    }

    public TicketEventBaseGenerator setWayType(TicketWayType wayType) {
        this.wayType = wayType;
        return this;
    }

    public TicketEventBaseGenerator setSaleTime(Date saleTime) {
        this.saleTime = saleTime;
        return this;
    }

    public TicketEventBaseGenerator setTariffCode(Long tariffCode) {
        this.tariffCode = tariffCode;
        return this;
    }

    public TicketEventBaseGenerator setDestinationStationCode(Integer destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
        return this;
    }

    public TicketEventBaseGenerator setDepartureStationCode(Integer departureStationCode) {
        this.departureStationCode = departureStationCode;
        return this;
    }

    public TicketEventBaseGenerator setTicketTypeShortName(String ticketTypeShortName) {
        this.ticketTypeShortName = ticketTypeShortName;
        return this;
    }

    public TicketEventBaseGenerator setTicketTypeCode(Integer ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
        return this;
    }

    public TicketEventBaseGenerator setStartDayOffset(int startDayOffset) {
        this.startDayOffset = startDayOffset;
        return this;
    }

    public TicketEventBaseGenerator setValidFromDate(Date validFromDate) {
        this.validFromDate = validFromDate;
        return this;
    }

    public TicketEventBaseGenerator setValidTillDate(Date validTillDate) {
        this.validTillDate = validTillDate;
        return this;
    }

    public TicketEventBaseGenerator setSmartCard(SmartCard smartCard) {
        this.smartCard = smartCard;
        return this;
    }

    public TicketEventBaseGenerator setType(String type) {
        this.type = type;
        return this;
    }

    @NonNull
    @Override
    public TicketEventBase build() {
        checkNotNull(tariffCode, "TariffCode is null");
        checkNotNull(wayType, "wayType is null");
        checkNotNull(ticketTypeCode, "ticketTypeCode is null");
        checkNotNull(departureStationCode, "departureStationCode is null");
        checkNotNull(destinationStationCode, "destinationStationCode is null");
        checkNotNull(ticketTypeShortName, "ShortName is null");
        checkNotNull(ticketCategoryCode, "TicketCategoryCode is null");
        checkNotNull(currentShift, "CashRegisterWorkingShift is null");

        TicketEventBase ticketEventBase = new TicketEventBase();
        ticketEventBase.setTariffCode(tariffCode);
        ticketEventBase.setWayType(wayType);
        ticketEventBase.setSaletime(saleTime);
        ticketEventBase.setTicketTypeShortName(ticketTypeShortName);
        ticketEventBase.setTicketCategoryCode(ticketCategoryCode);
        ticketEventBase.setTypeCode(ticketTypeCode);
        ticketEventBase.setStartDayOffset(startDayOffset);
        ticketEventBase.setValidFromDate(validFromDate);
        ticketEventBase.setValidTillDate(validTillDate);
        ticketEventBase.setDepartureStationCode(departureStationCode);
        ticketEventBase.setDestinationStationCode(destinationStationCode);
        ticketEventBase.setType(type);
        ticketEventBase.setShiftEventId(currentShift.getId());

        if (smartCard != null)
            ticketEventBase.setSmartCardId(smartCard.getId());

        return ticketEventBase;
    }
}

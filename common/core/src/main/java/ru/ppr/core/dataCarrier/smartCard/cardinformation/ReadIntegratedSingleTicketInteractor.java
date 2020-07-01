package ru.ppr.core.dataCarrier.smartCard.cardinformation;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaTroykaReader;
import ru.ppr.core.dataCarrier.smartCard.pdTrip.TicketMetroPd;
import ru.ppr.core.dataCarrier.smartCard.pdTroyka.MetroPd;

/**
 * @author Kolesnikov Sergik
 */
public class ReadIntegratedSingleTicketInteractor {

    @Inject
    public ReadIntegratedSingleTicketInteractor() {

    }

    private byte BLOCK_PD_TICKET_1 = 1;
    private byte BLOCK_PD_TICKET_2 = 2;

    @NonNull
    public ReadCardResult<CardPdData> readInformation(CardReader cardReader) {
        if(cardReader instanceof TroykaReader) {
            TroykaReader troykaReader = (TroykaReader) cardReader;
            MetroPd informationData = troykaReader.readTicketPd().getData();
            ReadCardResult<TicketMetroPd> informationTicket = troykaReader.readInformationPd(BLOCK_PD_TICKET_1);
            if (informationTicket.isSuccess()) {
                TicketMetroPd ticketPd = informationTicket.getData();
                return new ReadCardResult<>(getCardPdData(informationData, ticketPd));
            } else {
                ReadCardResult<TicketMetroPd> informationTicketReserve = troykaReader.readInformationPd(BLOCK_PD_TICKET_2);
                if (informationTicketReserve.isSuccess()) {
                    TicketMetroPd ticketPd = informationTicketReserve.getData();
                    return new ReadCardResult<>(getCardPdData(informationData, ticketPd));
                } else {
                    throw new IllegalArgumentException("Unknown card reader: " + cardReader.getClass());
                }
            }
        } else {
            StrelkaTroykaReader strelkaReader = (StrelkaTroykaReader) cardReader;
            MetroPd informationData = strelkaReader.readTicketPd().getData();
            ReadCardResult<TicketMetroPd> informationTicket = strelkaReader.readInformationPd(BLOCK_PD_TICKET_1);
            if (informationTicket.isSuccess()) {
                TicketMetroPd ticketPd = informationTicket.getData();
                return new ReadCardResult<>(getCardPdData(informationData, ticketPd));
            } else {
                ReadCardResult<TicketMetroPd> informationTicketReserve = strelkaReader.readInformationPd(BLOCK_PD_TICKET_2);
                if (informationTicketReserve.isSuccess()) {
                    TicketMetroPd ticketPd = informationTicketReserve.getData();
                    return new ReadCardResult<>(getCardPdData(informationData, ticketPd));
                } else {
                    return null;
                }
            }
        }
    }

    private Date convertValidityDateTime(int date_time_now, int service_exp_date_time){
        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long endDateTime;

        final long date_time = TimeUnit.MINUTES.toDays(date_time_now);

        endDateTime = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(date_time)+ TimeUnit.MINUTES.toMillis(service_exp_date_time);
        calendar.setTimeInMillis(endDateTime);
        return calendar.getTime();
    }


    private CardPdData getCardPdData(MetroPd troykaPd, TicketMetroPd ticketPd){
        CardPdData cardPdData = new CardPdData();
        Date validityDateTime =  convertValidityDateTime(troykaPd.getDateTimeNow(), ticketPd.getServiceExpDateTime());
        int ticketType = (ticketPd.getActiveTicketType() == 0)? troykaPd.getTypeTicket1(): troykaPd.getTypeTicket2();
        cardPdData.setTypeTicket(ticketType);
        cardPdData.setCountRematingPerformedTrips(ticketPd.getNumberRematingPerformedTrips());
        cardPdData.setValidityDateTime(validityDateTime);
        cardPdData.setValidFormatData(troykaPd.isValidFormatData());
        return cardPdData;
    }
}

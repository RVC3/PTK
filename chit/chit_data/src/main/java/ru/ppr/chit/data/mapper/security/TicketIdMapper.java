package ru.ppr.chit.data.mapper.security;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class TicketIdMapper {

    private static final String TAG = Logger.makeLogTag(TicketWhiteListItemMapper.class);

    public static final TicketIdMapper INSTANCE = Mappers.getMapper(TicketIdMapper.class);

    private final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat;
        }
    };

    public String mapToEntity(TicketId ticketId) {
        if (ticketId == null) {
            return null;
        }
        return new StringBuilder()
                .append(ticketId.getTicketNumber())
                .append(":")
                .append(ticketId.getDeviceId())
                .append(":")
                .append(dateFormat.get().format(ticketId.getSaleDate()))
                .toString();
    }

    public TicketId mapToModel(String ticketIdStr) {
        if (ticketIdStr == null) {
            return null;
        }
        String[] ticketIdData = ticketIdStr.split(":");
        TicketId ticketId = new TicketId();
        ticketId.setTicketNumber(Long.valueOf(ticketIdData[0]));
        ticketId.setDeviceId(ticketIdData[1]);
        try {
            ticketId.setSaleDate(dateFormat.get().parse(ticketIdData[2]));
        } catch (ParseException e) {
            Logger.error(TAG, e);
        }
        return ticketId;
    }

}

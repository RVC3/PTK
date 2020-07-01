package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.model.local.TicketIssueType;
import ru.ppr.chit.domain.model.local.TicketState;
import ru.ppr.chit.localdb.entity.TicketEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class TicketMapper implements LocalDbMapper<Ticket, TicketEntity> {

    public static final TicketMapper INSTANCE = Mappers.getMapper(TicketMapper.class);

    @Mappings({
            @Mapping(target = "ticketId", ignore = true),
            @Mapping(target = "passenger", ignore = true),
            @Mapping(target = "placeLocation", ignore = true),
            @Mapping(target = "oldPlaceLocation", ignore = true),
    })
    @Override
    public abstract TicketEntity modelToEntity(Ticket model);

    @Mappings({
            @Mapping(target = "ticketId", ignore = true),
            @Mapping(target = "passenger", ignore = true),
            @Mapping(target = "placeLocation", ignore = true),
            @Mapping(target = "oldPlaceLocation", ignore = true),
            @Mapping(target = "departureStation", ignore = true),
            @Mapping(target = "destinationStation", ignore = true),
            @Mapping(target = "ticketType", ignore = true),
    })
    @Override
    public abstract Ticket entityToModel(TicketEntity entity);

    Integer mapTicketIssueType(TicketIssueType ticketIssueType) {
        return ticketIssueType != null ? ticketIssueType.getCode() : null;
    }

    TicketIssueType mapTicketIssueTypeCode(Integer code) {
        return code != null ? TicketIssueType.valueOf(code) : null;
    }

    Integer mapTicketState(TicketState ticketState) {
        return ticketState != null ? ticketState.getCode() : null;
    }

    TicketState mapTicketStateCode(Integer code) {
        return code != null ? TicketState.valueOf(code) : null;
    }

}

package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

import ru.ppr.chit.api.entity.TicketEntity;
import ru.ppr.chit.domain.model.local.Ticket;

/**
 * @author Dmitry Nevolin
 */
@Mapper(uses = {TicketIdMapper.class, PassengerPersonalDataMapper.class, PlaceLocationMapper.class, UtcDateMapper.class})
public interface TicketMapper {

    TicketMapper INSTANCE = Mappers.getMapper(TicketMapper.class);

    @Mappings({
            @Mapping(source = "id", target = "ticketId"),
            @Mapping(source = "issueType", target = "ticketIssueType"),
            @Mapping(source = "rdsVersion", target = "nsiVersion"),
            @Mapping(source = "stateDateTimeUtc", target = "stateDate", qualifiedBy = {UtcDate.class}),
            @Mapping(source = "departureDateTimeUtc", target = "departureDate", qualifiedBy = {UtcDate.class}),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "ticketIdId", ignore = true),
            @Mapping(target = "passengerId", ignore = true),
            @Mapping(target = "placeLocationId", ignore = true),
            @Mapping(target = "oldPlaceLocationId", ignore = true),
            @Mapping(target = "departureStation", ignore = true),
            @Mapping(target = "destinationStation", ignore = true),
            @Mapping(target = "ticketType", ignore = true),
    })
    Ticket entityToModel(TicketEntity ticket);

    List<Ticket> entityListToModelList(List<TicketEntity> ticketList);

}

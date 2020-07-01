package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.TicketDataEntity;
import ru.ppr.chit.domain.model.local.TicketData;

/**
 * @author Dmitry Nevolin
 */
@Mapper(uses = {PassengerMapper.class, LocationMapper.class, SmartCardMapper.class, UtcDateMapper.class})
public interface TicketDataMapper {

    TicketDataMapper INSTANCE = Mappers.getMapper(TicketDataMapper.class);

    @Mappings({
            @Mapping(source = "departureDateTimeUtc", target = "departureDate", qualifiedBy = {UtcDate.class}),
            @Mapping(source = "rdsVersionId", target = "nsiVersion"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "passengerId", ignore = true),
            @Mapping(target = "locationId", ignore = true),
            @Mapping(target = "smartCardId", ignore = true),
            @Mapping(target = "departureStation", ignore = true),
            @Mapping(target = "destinationStation", ignore = true)
    })
    TicketData entityToModel(TicketDataEntity entity);

    @Mappings({
            @Mapping(source = "departureDate", target = "departureDateTimeUtc", qualifiedBy = {UtcDate.class}),
            @Mapping(source = "nsiVersion", target = "rdsVersionId"),
            @Mapping(target = "passenger", ignore = true),
            @Mapping(target = "location", ignore = true),
            @Mapping(target = "smartCard", ignore = true)
    })
    TicketDataEntity modelToEntity(TicketData model);

}

package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

import ru.ppr.chit.api.entity.TicketBoardingEntity;
import ru.ppr.chit.domain.model.local.TicketBoarding;

/**
 * @author Dmitry Nevolin
 */
@Mapper(uses = {TicketIdMapper.class, TicketDataMapper.class, UtcDateMapper.class})
public interface TicketBoardingMapper {

    TicketBoardingMapper INSTANCE = Mappers.getMapper(TicketBoardingMapper.class);

    @Mappings({
            @Mapping(source = "checkDateTimeUtc", target = "checkDate", qualifiedBy = {UtcDate.class}),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "ticketIdId", ignore = true),
            @Mapping(target = "ticketDataId", ignore = true)
    })
    TicketBoarding entityToModel(TicketBoardingEntity ticket);

    @Mappings({
            @Mapping(source = "checkDate", target = "checkDateTimeUtc", qualifiedBy = {UtcDate.class}),
            @Mapping(target = "ticketId", ignore = true),
            @Mapping(target = "ticketData", ignore = true)
    })
    TicketBoardingEntity modelToEntity(TicketBoarding model);

    List<TicketBoarding> entityListToModelList(List<TicketBoardingEntity> ticketList);

    List<TicketBoardingEntity> modelListToEntityList(List<TicketBoarding> ticketList);

}

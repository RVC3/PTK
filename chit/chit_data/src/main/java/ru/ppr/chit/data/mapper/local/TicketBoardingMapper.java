package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.chit.localdb.entity.TicketBoardingEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface TicketBoardingMapper extends LocalDbMapper<TicketBoarding, TicketBoardingEntity> {

    TicketBoardingMapper INSTANCE = Mappers.getMapper(TicketBoardingMapper.class);

    @Mappings({
            @Mapping(target = "ticketId", ignore = true),
            @Mapping(target = "ticketData", ignore = true)
    })
    @Override
    TicketBoarding entityToModel(TicketBoardingEntity entity);

    @Mappings({
            @Mapping(target = "ticketId", ignore = true),
            @Mapping(target = "ticketData", ignore = true)
    })
    @Override
    TicketBoardingEntity modelToEntity(TicketBoarding model);

}

package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.TicketData;
import ru.ppr.chit.localdb.entity.TicketDataEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface TicketDataMapper extends LocalDbMapper<TicketData, TicketDataEntity> {

    TicketDataMapper INSTANCE = Mappers.getMapper(TicketDataMapper.class);

    @Mappings({
            @Mapping(target = "departureStation", ignore = true),
            @Mapping(target = "destinationStation", ignore = true),
            @Mapping(target = "passenger", ignore = true),
            @Mapping(target = "location", ignore = true),
            @Mapping(target = "smartCard", ignore = true)
    })
    @Override
    TicketData entityToModel(TicketDataEntity entity);

    @Mappings({
            @Mapping(target = "passenger", ignore = true),
            @Mapping(target = "location", ignore = true),
            @Mapping(target = "smartCard", ignore = true)
    })
    @Override
    TicketDataEntity modelToEntity(TicketData model);

}

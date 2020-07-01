package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.TicketIdEntity;
import ru.ppr.chit.domain.model.local.TicketId;

/**
 * @author Dmitry Nevolin
 */
@Mapper(uses = {UtcDateMapper.class})
public interface TicketIdMapper {

    TicketIdMapper INSTANCE = Mappers.getMapper(TicketIdMapper.class);

    @Mappings({
            @Mapping(source = "saleDateTimeUtc", target = "saleDate", qualifiedBy = {UtcDate.class}),
            @Mapping(target = "id", ignore = true)
    })
    TicketId entityToModel(TicketIdEntity entity);

    @Mapping(source = "saleDate", target = "saleDateTimeUtc", qualifiedBy = {UtcDate.class})
    TicketIdEntity modelToEntity(TicketId model);

}

package ru.ppr.chit.data.mapper.nsi;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.nsi.TicketType;
import ru.ppr.chit.nsidb.entity.TicketTypeEntity;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public interface TicketTypeMapper extends NsiDbMapper<TicketType, TicketTypeEntity> {

    TicketTypeMapper INSTANCE = Mappers.getMapper(TicketTypeMapper.class);

    @Mappings({
            @Mapping(target = "withPlace", ignore = true),
            @Mapping(target = "validityPeriodCode", ignore = true),
            @Mapping(target = "durationOfValidity", ignore = true),
            @Mapping(target = "tripsNumber", ignore = true)
    })
    @Override
    TicketTypeEntity modelToEntity(TicketType model);

    @Mapping(target = "ticketCategory", ignore = true)
    @Override
    TicketType entityToModel(TicketTypeEntity entity);

}

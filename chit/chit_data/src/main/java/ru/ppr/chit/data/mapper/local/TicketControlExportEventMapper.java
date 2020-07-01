package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.TicketControlExportEvent;
import ru.ppr.chit.localdb.entity.TicketControlExportEventEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface TicketControlExportEventMapper extends LocalDbMapper<TicketControlExportEvent, TicketControlExportEventEntity> {

    TicketControlExportEventMapper INSTANCE = Mappers.getMapper(TicketControlExportEventMapper.class);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "ticketControlEvent", ignore = true)
    })
    @Override
    TicketControlExportEventEntity modelToEntity(TicketControlExportEvent model);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "ticketControlEvent", ignore = true)
    })
    @Override
    TicketControlExportEvent entityToModel(TicketControlExportEventEntity entity);

}

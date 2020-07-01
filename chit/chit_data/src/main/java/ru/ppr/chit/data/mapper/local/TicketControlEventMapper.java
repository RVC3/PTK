package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.localdb.entity.TicketControlEventEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class TicketControlEventMapper implements LocalDbMapper<TicketControlEvent, TicketControlEventEntity> {

    public static final TicketControlEventMapper INSTANCE = Mappers.getMapper(TicketControlEventMapper.class);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "ticketBoarding", ignore = true),
            @Mapping(target = "boardingEvent", ignore = true)
    })
    @Override
    public abstract TicketControlEvent entityToModel(TicketControlEventEntity entity);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "ticketBoarding", ignore = true),
            @Mapping(target = "boardingEvent", ignore = true)
    })
    @Override
    public abstract TicketControlEventEntity modelToEntity(TicketControlEvent model);

    Integer mapTicketControlEventStatus(TicketControlEvent.Status ticketControlEventStatus) {
        return ticketControlEventStatus != null ? ticketControlEventStatus.getCode() : null;
    }

    TicketControlEvent.Status mapTicketControlEventStatusCode(Integer code) {
        return code != null ? TicketControlEvent.Status.valueOf(code) : null;
    }

}

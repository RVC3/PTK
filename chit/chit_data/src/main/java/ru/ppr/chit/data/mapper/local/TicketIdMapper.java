package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.localdb.entity.TicketIdEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface TicketIdMapper extends LocalDbMapper<TicketId, TicketIdEntity> {

    TicketIdMapper INSTANCE = Mappers.getMapper(TicketIdMapper.class);

}

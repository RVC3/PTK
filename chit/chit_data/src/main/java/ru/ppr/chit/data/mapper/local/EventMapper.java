package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.localdb.entity.EventEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface EventMapper extends LocalDbMapper<Event, EventEntity> {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

}

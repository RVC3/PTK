package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.localdb.entity.BoardingEventEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class BoardingEventMapper implements LocalDbMapper<BoardingEvent, BoardingEventEntity> {

    public static final BoardingEventMapper INSTANCE = Mappers.getMapper(BoardingEventMapper.class);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "tripServiceEvent", ignore = true)
    })
    @Override
    public abstract BoardingEventEntity modelToEntity(BoardingEvent model);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "station", ignore = true),
            @Mapping(target = "tripServiceEvent", ignore = true)
    })
    @Override
    public abstract BoardingEvent entityToModel(BoardingEventEntity entity);

    Integer mapBoardingEventStatus(BoardingEvent.Status boardingEventStatus) {
        return boardingEventStatus != null ? boardingEventStatus.getCode() : null;
    }

    BoardingEvent.Status mapBoardingEventStatusCode(Integer code) {
        return code != null ? BoardingEvent.Status.valueOf(code) : null;
    }
    
}

package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.localdb.entity.TripServiceEventEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class TripServiceEventMapper implements LocalDbMapper<TripServiceEvent, TripServiceEventEntity> {

    public static final TripServiceEventMapper INSTANCE = Mappers.getMapper(TripServiceEventMapper.class);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "trainInfo", ignore = true)
    })
    @Override
    public abstract TripServiceEventEntity modelToEntity(TripServiceEvent model);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "trainInfo", ignore = true)
    })
    @Override
    public abstract TripServiceEvent entityToModel(TripServiceEventEntity entity);

    Integer mapTripServiceEventStatus(TripServiceEvent.Status tripServiceEventStatus) {
        return tripServiceEventStatus != null ? tripServiceEventStatus.getCode() : null;
    }

    TripServiceEvent.Status mapTripServiceEventStatusCode(Integer code) {
        return code != null ? TripServiceEvent.Status.valueOf(code) : null;
    }

}

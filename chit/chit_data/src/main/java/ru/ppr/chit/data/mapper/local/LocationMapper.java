package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.Location;
import ru.ppr.chit.localdb.entity.LocationEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface LocationMapper extends LocalDbMapper<Location, LocationEntity> {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

}

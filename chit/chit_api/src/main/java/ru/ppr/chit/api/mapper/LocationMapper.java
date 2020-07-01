package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.LocationEntity;
import ru.ppr.chit.domain.model.local.Location;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    @Mapping(target = "id", ignore = true)
    Location entityToModel(LocationEntity entity);

    LocationEntity modelToEntity(Location model);

}

package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.PlaceLocationEntity;
import ru.ppr.chit.domain.model.local.PlaceLocation;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface PlaceLocationMapper {

    PlaceLocationMapper INSTANCE = Mappers.getMapper(PlaceLocationMapper.class);

    @Mapping(target = "id", ignore = true)
    PlaceLocation entityToModel(PlaceLocationEntity entity);

}

package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.PlaceLocation;
import ru.ppr.chit.localdb.entity.PlaceLocationEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface PlaceLocationMapper extends LocalDbMapper<PlaceLocation, PlaceLocationEntity> {

    PlaceLocationMapper INSTANCE = Mappers.getMapper(PlaceLocationMapper.class);

}

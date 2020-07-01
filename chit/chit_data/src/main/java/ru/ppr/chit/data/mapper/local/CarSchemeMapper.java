package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.CarScheme;
import ru.ppr.chit.localdb.entity.CarSchemeEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface CarSchemeMapper extends LocalDbMapper<CarScheme, CarSchemeEntity> {

    CarSchemeMapper INSTANCE = Mappers.getMapper(CarSchemeMapper.class);

    @Mapping(target = "elements", ignore = true)
    @Override
    CarSchemeEntity modelToEntity(CarScheme model);

    @Mapping(target = "elements", ignore = true)
    @Override
    CarScheme entityToModel(CarSchemeEntity entity);
}

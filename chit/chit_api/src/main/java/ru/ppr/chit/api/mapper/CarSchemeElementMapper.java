package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.CarSchemeElementEntity;
import ru.ppr.chit.domain.model.local.CarSchemeElement;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface CarSchemeElementMapper {

    CarSchemeElementMapper INSTANCE = Mappers.getMapper(CarSchemeElementMapper.class);

    @Mapping(target = "id", ignore = true)
    CarSchemeElement entityToModel(CarSchemeElementEntity entity);

}

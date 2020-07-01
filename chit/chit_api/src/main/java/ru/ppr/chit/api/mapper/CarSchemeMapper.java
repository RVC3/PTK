package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.CarSchemeEntity;
import ru.ppr.chit.domain.model.local.CarScheme;

/**
 * @author Dmitry Nevolin
 */
@Mapper(uses = {CarSchemeElementMapper.class})
public interface CarSchemeMapper {

    CarSchemeMapper INSTANCE = Mappers.getMapper(CarSchemeMapper.class);

    @Mapping(target = "id", ignore = true)
    CarScheme entityToModel(CarSchemeEntity entity);

}

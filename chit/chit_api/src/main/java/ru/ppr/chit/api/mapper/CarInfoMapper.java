package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.CarInfoEntity;
import ru.ppr.chit.domain.model.local.CarInfo;

/**
 * @author Dmitry Nevolin
 */
@Mapper(uses = {CarSchemeMapper.class})
public interface CarInfoMapper {

    CarInfoMapper INSTANCE = Mappers.getMapper(CarInfoMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "schemeId", ignore = true)
    })
    CarInfo entityToModel(CarInfoEntity entity);

}

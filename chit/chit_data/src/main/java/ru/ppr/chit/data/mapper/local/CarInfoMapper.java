package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.CarInfo;
import ru.ppr.chit.localdb.entity.CarInfoEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface CarInfoMapper extends LocalDbMapper<CarInfo, CarInfoEntity> {

    CarInfoMapper INSTANCE = Mappers.getMapper(CarInfoMapper.class);

    @Mappings({
            @Mapping(target = "trainInfoId", ignore = true),
            @Mapping(target = "scheme", ignore = true)
    })
    @Override
    CarInfoEntity modelToEntity(CarInfo model);

    @Mapping(target = "scheme", ignore = true)
    @Override
    CarInfo entityToModel(CarInfoEntity entity);

}

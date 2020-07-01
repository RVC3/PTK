package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.localdb.entity.TrainInfoEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface TrainInfoMapper extends LocalDbMapper<TrainInfo, TrainInfoEntity> {

    TrainInfoMapper INSTANCE = Mappers.getMapper(TrainInfoMapper.class);

    @Mappings({
            @Mapping(target = "stations", ignore = true),
            @Mapping(target = "cars", ignore = true)
    })
    @Override
    TrainInfoEntity modelToEntity(TrainInfo model);

    @Mappings({
            @Mapping(target = "stations", ignore = true),
            @Mapping(target = "cars", ignore = true)
    })
    @Override
    TrainInfo entityToModel(TrainInfoEntity entity);

}

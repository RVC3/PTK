package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.StationInfo;
import ru.ppr.chit.domain.model.local.StationState;
import ru.ppr.chit.localdb.entity.StationInfoEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class StationInfoMapper implements LocalDbMapper<StationInfo, StationInfoEntity> {

    public static final StationInfoMapper INSTANCE = Mappers.getMapper(StationInfoMapper.class);

    @Mappings({
            @Mapping(source = "stationState", target = "stationStateCode"),
            @Mapping(target = "trainInfoId", ignore = true)
    })
    @Override
    public abstract StationInfoEntity modelToEntity(StationInfo model);

    @Mapping(source = "stationStateCode", target = "stationState")
    @Override
    public abstract StationInfo entityToModel(StationInfoEntity entity);

    Integer mapStationState(StationState stationState) {
        return stationState != null ? stationState.getCode() : null;
    }

    StationState mapStationStateCode(Integer code) {
        return code != null ? StationState.valueOf(code) : null;
    }

}

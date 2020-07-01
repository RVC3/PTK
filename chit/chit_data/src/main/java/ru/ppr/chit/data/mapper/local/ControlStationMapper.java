package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.localdb.entity.ControlStationEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface ControlStationMapper extends LocalDbMapper<ControlStation, ControlStationEntity> {

    ControlStationMapper INSTANCE = Mappers.getMapper(ControlStationMapper.class);

}

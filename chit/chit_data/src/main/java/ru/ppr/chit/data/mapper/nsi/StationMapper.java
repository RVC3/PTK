package ru.ppr.chit.data.mapper.nsi;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.nsidb.entity.StationEntity;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public interface StationMapper extends NsiDbMapper<Station, StationEntity> {

    StationMapper INSTANCE = Mappers.getMapper(StationMapper.class);

}

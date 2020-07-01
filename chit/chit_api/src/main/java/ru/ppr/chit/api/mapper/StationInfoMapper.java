package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.StationInfoEntity;
import ru.ppr.chit.domain.model.local.StationInfo;

/**
 * @author Dmitry Nevolin
 */
@Mapper(uses = {UtcDateMapper.class})
public interface StationInfoMapper {

    StationInfoMapper INSTANCE = Mappers.getMapper(StationInfoMapper.class);

    @Mappings({
            @Mapping(source = "arrivalDateTimeUtc", target = "arrivalDate", qualifiedBy = {UtcDate.class}),
            @Mapping(source = "departureDateTimeUtc", target = "departureDate", qualifiedBy = {UtcDate.class}),
            @Mapping(target = "id", ignore = true)
    })
    StationInfo entityToModel(StationInfoEntity entity);

}

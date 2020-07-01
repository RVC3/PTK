package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.TrainInfoEntity;
import ru.ppr.chit.domain.model.local.TrainInfo;

/**
 * @author Dmitry Nevolin
 */
@Mapper(uses = {StationInfoMapper.class, CarInfoMapper.class, UtcDateMapper.class})
public interface TrainInfoMapper {

    TrainInfoMapper INSTANCE = Mappers.getMapper(TrainInfoMapper.class);

    @Mappings({
            @Mapping(source = "departureDateTimeUtc", target = "departureDate", qualifiedBy = {UtcDate.class}),
            @Mapping(source = "destinationDateTimeUtc", target = "destinationDate", qualifiedBy = {UtcDate.class}),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "legacy", ignore = true)
    })
    TrainInfo entityToModel(TrainInfoEntity trainInfo);

}

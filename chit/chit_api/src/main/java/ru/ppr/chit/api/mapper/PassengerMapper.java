package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.PassengerEntity;
import ru.ppr.chit.domain.model.local.Passenger;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface PassengerMapper {

    PassengerMapper INSTANCE = Mappers.getMapper(PassengerMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "documentType", ignore = true)
    })
    Passenger entityToModel(PassengerEntity entity);

    PassengerEntity modelToEntity(Passenger model);

}

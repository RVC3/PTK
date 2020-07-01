package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.PassengerPersonalDataEntity;
import ru.ppr.chit.domain.model.local.PassengerPersonalData;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface PassengerPersonalDataMapper {

    PassengerPersonalDataMapper INSTANCE = Mappers.getMapper(PassengerPersonalDataMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "documentType", ignore = true)
    })
    PassengerPersonalData entityToModel(PassengerPersonalDataEntity entity);

}

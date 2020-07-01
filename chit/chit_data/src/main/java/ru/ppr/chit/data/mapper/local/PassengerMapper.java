package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.Passenger;
import ru.ppr.chit.localdb.entity.PassengerEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface PassengerMapper extends LocalDbMapper<Passenger, PassengerEntity> {

    PassengerMapper INSTANCE = Mappers.getMapper(PassengerMapper.class);

    @Mapping(target = "documentType", ignore = true)
    @Override
    Passenger entityToModel(PassengerEntity entity);

}

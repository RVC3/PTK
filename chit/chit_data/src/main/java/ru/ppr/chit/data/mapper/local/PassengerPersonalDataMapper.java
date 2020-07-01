package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.Gender;
import ru.ppr.chit.domain.model.local.PassengerPersonalData;
import ru.ppr.chit.localdb.entity.PassengerPersonalDataEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class PassengerPersonalDataMapper implements LocalDbMapper<PassengerPersonalData, PassengerPersonalDataEntity> {

    public static final PassengerPersonalDataMapper INSTANCE = Mappers.getMapper(PassengerPersonalDataMapper.class);

    @Mapping(target = "documentType", ignore = true)
    @Override
    public abstract PassengerPersonalData entityToModel(PassengerPersonalDataEntity entity);

    Integer mapGender(Gender gender) {
        return gender != null ? gender.getCode() : null;
    }

    Gender mapGenderCode(Integer code) {
        return code != null ? Gender.valueOf(code) : null;
    }

}

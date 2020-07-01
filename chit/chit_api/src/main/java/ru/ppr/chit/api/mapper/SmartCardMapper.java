package ru.ppr.chit.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.api.entity.SmartCardEntity;
import ru.ppr.chit.domain.model.local.SmartCard;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface SmartCardMapper {

    SmartCardMapper INSTANCE = Mappers.getMapper(SmartCardMapper.class);

    @Mapping(target = "id", ignore = true)
    SmartCard entityToModel(SmartCardEntity entity);

    SmartCardEntity modelToEntity(SmartCard model);

}

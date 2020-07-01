package ru.ppr.chit.api.auth;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.OAuth2Token;

/**
 * @author Dmitry Nevolin
 */
@Mapper
// Не работают method references если снизить видимость
@SuppressWarnings("WeakerAccess")
public interface OAuth2TokenMapper {

    OAuth2TokenMapper INSTANCE = Mappers.getMapper(OAuth2TokenMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "broken", ignore = true),
            @Mapping(target = "authInfoId", ignore = true)
    })
    OAuth2Token entityToModel(OAuth2TokenEntity entity);

}

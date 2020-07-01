package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.OAuth2Token;
import ru.ppr.chit.localdb.entity.OAuth2TokenEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface OAuth2TokenMapper extends LocalDbMapper<OAuth2Token, OAuth2TokenEntity> {

    OAuth2TokenMapper INSTANCE = Mappers.getMapper(OAuth2TokenMapper.class);

    @Mappings({
            @Mapping(target = "authInfo", ignore = true)
    })
    OAuth2TokenEntity modelToEntity(OAuth2Token model);

}

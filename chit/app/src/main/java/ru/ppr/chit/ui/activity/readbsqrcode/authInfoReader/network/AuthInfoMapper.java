package ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.network;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.AuthInfo;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface AuthInfoMapper {

    AuthInfoMapper INSTANCE = Mappers.getMapper(AuthInfoMapper.class);

    @Mapping(target = "id", ignore = true)
    AuthInfo entityToModel(AuthInfoEntity networkAuthData);

}

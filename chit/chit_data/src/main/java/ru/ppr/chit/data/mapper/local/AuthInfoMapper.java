package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.chit.localdb.entity.AuthInfoEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface AuthInfoMapper extends LocalDbMapper<AuthInfo, AuthInfoEntity> {

    AuthInfoMapper INSTANCE = Mappers.getMapper(AuthInfoMapper.class);

}

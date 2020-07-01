package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.localdb.entity.UserEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface UserMapper extends LocalDbMapper<User, UserEntity> {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

}

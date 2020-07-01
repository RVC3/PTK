package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.LocalDbVersion;
import ru.ppr.chit.localdb.entity.LocalDbVersionEntity;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper(uses = {LongDateMapper.class})
public interface LocalDbVersionMapper extends LocalDbMapper<LocalDbVersion, LocalDbVersionEntity> {

    LocalDbVersionMapper INSTANCE = Mappers.getMapper(LocalDbVersionMapper.class);

    LocalDbVersion entityToModel(LocalDbVersionEntity entity);

    LocalDbVersionEntity modelToEntity(LocalDbVersion model);
}

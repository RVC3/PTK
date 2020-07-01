package ru.ppr.chit.data.mapper.nsi;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.nsi.AccessScheme;
import ru.ppr.chit.nsidb.entity.AccessSchemeEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface AccessSchemeMapper extends NsiDbMapper<AccessScheme, AccessSchemeEntity> {

    AccessSchemeMapper INSTANCE = Mappers.getMapper(AccessSchemeMapper.class);

}

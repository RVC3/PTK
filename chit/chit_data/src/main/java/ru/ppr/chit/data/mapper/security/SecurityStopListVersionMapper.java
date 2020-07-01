package ru.ppr.chit.data.mapper.security;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.security.SecurityStopListVersion;
import ru.ppr.chit.securitydb.entity.SecurityStopListVersionEntity;

/**
 * Created by m.sidorov.
 */
@Mapper
public interface SecurityStopListVersionMapper extends SecurityDbMapper<SecurityStopListVersion, SecurityStopListVersionEntity> {

    SecurityStopListVersionMapper INSTANCE = Mappers.getMapper(SecurityStopListVersionMapper.class);

}

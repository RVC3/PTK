package ru.ppr.chit.data.mapper.security;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.security.PtkDataContractsVersion;
import ru.ppr.chit.securitydb.entity.PtkDataContractsVersionEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface PtkDataContractsVersionMapper extends SecurityDbMapper<PtkDataContractsVersion, PtkDataContractsVersionEntity> {

    PtkDataContractsVersionMapper INSTANCE = Mappers.getMapper(PtkDataContractsVersionMapper.class);

}

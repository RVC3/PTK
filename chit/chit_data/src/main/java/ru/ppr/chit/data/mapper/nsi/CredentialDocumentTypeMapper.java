package ru.ppr.chit.data.mapper.nsi;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.nsi.CredentialDocumentType;
import ru.ppr.chit.nsidb.entity.CredentialDocumentTypeEntity;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public interface CredentialDocumentTypeMapper extends NsiDbMapper<CredentialDocumentType, CredentialDocumentTypeEntity> {

    CredentialDocumentTypeMapper INSTANCE = Mappers.getMapper(CredentialDocumentTypeMapper.class);

}
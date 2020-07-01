package ru.ppr.chit.rfid;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.AccessScheme;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface AccessSchemeMapper {

    AccessSchemeMapper INSTANCE = Mappers.getMapper(AccessSchemeMapper.class);

    AccessScheme entityToModel(ru.ppr.chit.domain.model.nsi.AccessScheme accessScheme);

}

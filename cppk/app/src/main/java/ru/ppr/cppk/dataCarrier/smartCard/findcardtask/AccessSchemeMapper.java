package ru.ppr.cppk.dataCarrier.smartCard.findcardtask;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.AccessScheme;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public interface AccessSchemeMapper {

    AccessSchemeMapper INSTANCE = Mappers.getMapper(AccessSchemeMapper.class);

    AccessScheme entityToModel(ru.ppr.nsi.entity.AccessScheme entity);
}

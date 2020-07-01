package ru.ppr.chit.rfid;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.AccessRule;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface AccessRuleMapper {

    AccessRuleMapper INSTANCE = Mappers.getMapper(AccessRuleMapper.class);

    AccessRule entityToModel(ru.ppr.chit.domain.model.nsi.AccessRule accessRule);

}

package ru.ppr.chit.data.mapper.nsi;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.nsi.AccessRule;
import ru.ppr.chit.nsidb.entity.AccessRuleEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class AccessRuleMapper implements NsiDbMapper<AccessRule, AccessRuleEntity> {

    public static final AccessRuleMapper INSTANCE = Mappers.getMapper(AccessRuleMapper.class);

    @Mapping(target = "accessScheme", ignore = true)
    @Override
    public abstract AccessRule entityToModel(AccessRuleEntity entity);

    Integer mapKeyType(AccessRule.KeyType accessRuleKeyType) {
        return accessRuleKeyType != null ? accessRuleKeyType.getCode() : null;
    }

    AccessRule.KeyType mapKeyTypeCode(Integer code) {
        return code != null ? AccessRule.KeyType.valueOf(code) : null;
    }

    Integer mapKeyName(AccessRule.KeyName accessRuleKeyName) {
        return accessRuleKeyName != null ? accessRuleKeyName.getCode() : null;
    }

    AccessRule.KeyName mapKeyNameCode(Integer code) {
        return code != null ? AccessRule.KeyName.valueOf(code) : null;
    }

}

package ru.ppr.cppk.dataCarrier.smartCard.findcardtask;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.AccessRule;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public abstract class AccessRuleMapper {

    public static AccessRuleMapper INSTANCE = Mappers.getMapper(AccessRuleMapper.class);

    abstract AccessRule entityToModel(ru.ppr.nsi.entity.AccessRule entity);

    AccessRule.KeyType mapKeyType(@ru.ppr.nsi.entity.AccessRule.KeyType int keyType) {
        switch (keyType) {
            case ru.ppr.nsi.entity.AccessRule.KEY_TYPE_READ:
                return AccessRule.KeyType.READ;
            case ru.ppr.nsi.entity.AccessRule.KEY_TYPE_WRITE:
                return AccessRule.KeyType.WRITE;
            case ru.ppr.nsi.entity.AccessRule.KEY_TYPE_READ_AND_WRITE:
                return AccessRule.KeyType.READ_AND_WRITE;
        }
        return null;
    }

    AccessRule.KeyName mapKeyName(@ru.ppr.nsi.entity.AccessRule.KeyName int keyName) {
        switch (keyName) {
            case ru.ppr.nsi.entity.AccessRule.KEY_NAME_A:
                return AccessRule.KeyName.A;
            case ru.ppr.nsi.entity.AccessRule.KEY_NAME_B:
                return AccessRule.KeyName.B;
        }
        return null;
    }
}

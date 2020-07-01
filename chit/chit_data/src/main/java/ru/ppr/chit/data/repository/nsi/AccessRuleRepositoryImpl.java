package ru.ppr.chit.data.repository.nsi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.AccessRuleMapper;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.data.repository.nsi.base.BaseVdNsiDbRepository;
import ru.ppr.chit.domain.model.nsi.AccessRule;
import ru.ppr.chit.domain.model.nsi.DeviceType;
import ru.ppr.chit.domain.repository.nsi.AccessRuleRepository;
import ru.ppr.chit.nsidb.entity.AccessRuleEntity;
import ru.ppr.chit.nsidb.entity.AccessSchemeEntity;
import ru.ppr.chit.nsidb.greendao.AccessRuleEntityDao;
import ru.ppr.chit.nsidb.greendao.AccessSchemeEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class AccessRuleRepositoryImpl extends BaseVdNsiDbRepository<AccessRule, AccessRuleEntity> implements AccessRuleRepository {

    private static final String TABLE_PREFIX = "T";

    @Inject
    AccessRuleRepositoryImpl(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Override
    protected AbstractDao<AccessRuleEntity, Void> dao() {
        return daoSession().getAccessRuleEntityDao();
    }

    @Override
    protected NsiDbMapper<AccessRule, AccessRuleEntity> mapper() {
        return AccessRuleMapper.INSTANCE;
    }

    @NonNull
    @Override
    public List<AccessRule> loadAllForNewAccess(int sectorNumber,
                                                @NonNull EnumSet<AccessRule.KeyType> keyTypeSet,
                                                @Nullable List<Long> allowedAccessSchemeCodeList,
                                                @Nullable List<Long> deniedAccessSchemeCodeList,
                                                int nsiVersion) {
        QueryBuilder<AccessRuleEntity> qb = dao().queryBuilder();
        Join accessScheme = qb.join(AccessRuleEntityDao.Properties.AccessSchemeCode, AccessSchemeEntity.class, AccessSchemeEntityDao.Properties.Code);
        accessScheme.where(AccessSchemeEntityDao.Properties.DeviceTypeCode.eq(DeviceType.PTK.getCode()));
        qb.where(AccessRuleEntityDao.Properties.SectorNumber.eq(sectorNumber));
        List<Integer> keyTypeCodeList = new ArrayList<>();
        for (AccessRule.KeyType keyType : keyTypeSet) {
            keyTypeCodeList.add(keyType.getCode());
        }
        qb.where(AccessRuleEntityDao.Properties.KeyType.in(keyTypeCodeList));
        if (allowedAccessSchemeCodeList != null) {
            accessScheme.where(AccessSchemeEntityDao.Properties.Code.in(allowedAccessSchemeCodeList));
        }
        if (deniedAccessSchemeCodeList != null) {
            accessScheme.where(AccessSchemeEntityDao.Properties.Code.notIn(deniedAccessSchemeCodeList));
        }
        qb.where(versionIdEqCondition(nsiVersion, TABLE_PREFIX));
        accessScheme.where(versionIdEqCondition(nsiVersion, accessScheme.getTablePrefix()));
        qb.orderRaw(jointOrderAsc(accessScheme, AccessSchemeEntityDao.Properties.Priority, AccessSchemeEntityDao.Properties.TicketStorageTypeCode));
        return mapper().entityListToModelList(qb.list());
    }

    private String jointOrderAsc(Join join, Property... properties) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < properties.length; i++) {
            sb.append(join.getTablePrefix()).append(".");
            sb.append(properties[i].columnName);
            if (i != properties.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}

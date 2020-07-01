package ru.ppr.chit.data.repository.nsi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.AccessSchemeMapper;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.data.repository.nsi.base.BaseCvdNsiDbRepository;
import ru.ppr.chit.domain.model.nsi.AccessScheme;
import ru.ppr.chit.domain.model.nsi.TicketStorageType;
import ru.ppr.chit.domain.repository.nsi.AccessSchemeRepository;
import ru.ppr.chit.nsidb.entity.AccessSchemeEntity;
import ru.ppr.chit.nsidb.greendao.AccessSchemeEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class AccessSchemeRepositoryImpl extends BaseCvdNsiDbRepository<AccessScheme, AccessSchemeEntity, Long> implements AccessSchemeRepository {

    @Inject
    AccessSchemeRepositoryImpl(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Override
    protected AbstractDao<AccessSchemeEntity, Void> dao() {
        return daoSession().getAccessSchemeEntityDao();
    }

    @Override
    protected NsiDbMapper<AccessScheme, AccessSchemeEntity> mapper() {
        return AccessSchemeMapper.INSTANCE;
    }

    @NonNull
    @Override
    public List<AccessScheme> loadAllByTicketStorageTypeSet(@Nullable EnumSet<TicketStorageType> ticketStorageTypeSet, int nsiVersion) {
        QueryBuilder<AccessSchemeEntity> qb = dao().queryBuilder();
        qb.where(versionIdEqCondition(nsiVersion));
        if (ticketStorageTypeSet != null) {
            List<Integer> ticketStorageTypeCodeList = new ArrayList<>();
            for (TicketStorageType ticketStorageType : ticketStorageTypeSet) {
                ticketStorageTypeCodeList.add(ticketStorageType.getCode());
            }
            qb.where(AccessSchemeEntityDao.Properties.TicketStorageTypeCode.in(ticketStorageTypeCodeList));
        }
        return mapper().entityListToModelList(qb.list());
    }

}

package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.ExchangeEventMapper;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.repository.local.ExchangeEventRepository;
import ru.ppr.chit.localdb.entity.ExchangeEventEntity;
import ru.ppr.chit.localdb.greendao.ExchangeEventEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class ExchangeEventRepositoryImpl extends BaseCrudLocalDbRepository<ExchangeEvent, ExchangeEventEntity, Long> implements ExchangeEventRepository {

    @Inject
    ExchangeEventRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<ExchangeEventEntity, Long> dao() {
        return daoSession().getExchangeEventEntityDao();
    }

    @Override
    protected LocalDbMapper<ExchangeEvent, ExchangeEventEntity> mapper() {
        return ExchangeEventMapper.INSTANCE;
    }

    @Nullable
    @Override
    public ExchangeEvent loadLast() {
        ExchangeEventEntity entity = dao().queryBuilder()
                .orderDesc(ExchangeEventEntityDao.Properties.Id)
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

    @Nullable
    @Override
    public ExchangeEvent loadLastByTypeSetAndStatusSet(@NonNull EnumSet<ExchangeEvent.Type> typeSet, @NonNull EnumSet<ExchangeEvent.Status> statusSet) {
        List<Integer> typeCodeList = new ArrayList<>();
        List<Integer> statusCodeList = new ArrayList<>();
        for (ExchangeEvent.Type type : typeSet) {
            typeCodeList.add(type.getCode());
        }
        for (ExchangeEvent.Status status : statusSet) {
            statusCodeList.add(status.getCode());
        }
        ExchangeEventEntity entity  = dao().queryBuilder()
                .where(ExchangeEventEntityDao.Properties.Type.in(typeCodeList),
                        ExchangeEventEntityDao.Properties.Status.in(statusCodeList))
                .orderDesc(ExchangeEventEntityDao.Properties.Id)
                .limit(1)
                .unique();
        return mapper().entityToModel(entity);
    }

}

package ru.ppr.chit.data.repository.nsi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.data.mapper.nsi.VersionMapper;
import ru.ppr.chit.data.repository.nsi.base.BaseNsiDbRepository;
import ru.ppr.chit.domain.model.nsi.Version;
import ru.ppr.chit.domain.repository.nsi.VersionRepository;
import ru.ppr.chit.nsidb.entity.VersionEntity;
import ru.ppr.chit.nsidb.greendao.VersionEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class VersionRepositoryImpl extends BaseNsiDbRepository<Version, VersionEntity, Integer> implements VersionRepository {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Inject
    VersionRepositoryImpl(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Override
    protected AbstractDao<VersionEntity, Integer> dao() {
        return daoSession().getVersionEntityDao();
    }

    @Override
    protected NsiDbMapper<Version, VersionEntity> mapper() {
        return VersionMapper.INSTANCE;
    }

    @Nullable
    @Override
    public Version load(int versionId) {
        return mapper().entityToModel(dao().load(versionId));
    }

    @Nullable
    @Override
    public Version loadForDate(@NonNull Date date, @Nullable EnumSet<Version.Status> statusSet) {
        QueryBuilder<VersionEntity> qb = dao().queryBuilder();
        qb.where(startingDateTimeLeCondition(date));
        if (statusSet != null) {
            List<Integer> statusCodeList = new ArrayList<>();
            for (Version.Status status : statusSet) {
                statusCodeList.add(status.getCode());
            }
            qb.where(VersionEntityDao.Properties.Status.in(statusCodeList));
        }
        qb.orderDesc(VersionEntityDao.Properties.VersionId).limit(1);
        return mapper().entityToModel(qb.unique());
    }

    private WhereCondition startingDateTimeLeCondition(@NonNull Date date) {
        return new WhereCondition.StringCondition(VersionEntityDao.Properties.StartingDateTime.columnName + "<='" + simpleDateFormat.format(date) + "'");
    }

}

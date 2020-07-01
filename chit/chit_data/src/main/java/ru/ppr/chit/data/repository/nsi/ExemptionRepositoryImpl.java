package ru.ppr.chit.data.repository.nsi;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.ExemptionMapper;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.data.repository.nsi.base.BaseCvdNsiDbRepository;
import ru.ppr.chit.domain.model.nsi.Exemption;
import ru.ppr.chit.domain.repository.nsi.ExemptionRepository;
import ru.ppr.chit.nsidb.entity.ExemptionEntity;

/**
 * @author Aleksandr Brazhkin
 */
public class ExemptionRepositoryImpl extends BaseCvdNsiDbRepository<Exemption, ExemptionEntity, Long> implements ExemptionRepository {

    @Inject
    ExemptionRepositoryImpl(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Override
    protected AbstractDao<ExemptionEntity, Void> dao() {
        return daoSession().getExemptionEntityDao();
    }

    @Override
    protected NsiDbMapper<Exemption, ExemptionEntity> mapper() {
        return ExemptionMapper.INSTANCE;
    }

}

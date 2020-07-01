package ru.ppr.chit.data.repository.nsi;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.data.mapper.nsi.TicketTypeMapper;
import ru.ppr.chit.data.repository.nsi.base.BaseCvdNsiDbRepository;
import ru.ppr.chit.domain.model.nsi.TicketType;
import ru.ppr.chit.domain.repository.nsi.TicketTypeRepository;
import ru.ppr.chit.nsidb.entity.TicketTypeEntity;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketTypeRepositoryImpl extends BaseCvdNsiDbRepository<TicketType, TicketTypeEntity, Long> implements TicketTypeRepository {

    @Inject
    TicketTypeRepositoryImpl(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Override
    protected AbstractDao<TicketTypeEntity, Void> dao() {
        return daoSession().getTicketTypeEntityDao();
    }

    @Override
    protected NsiDbMapper<TicketType, TicketTypeEntity> mapper() {
        return TicketTypeMapper.INSTANCE;
    }

}

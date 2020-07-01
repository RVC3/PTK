package ru.ppr.chit.data.repository.local;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.TicketControlEventMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.repository.local.TicketControlEventRepository;
import ru.ppr.chit.localdb.entity.TicketControlEventEntity;
import ru.ppr.chit.localdb.greendao.TicketControlEventEntityDao;
import ru.ppr.chit.localdb.greendao.TicketControlExportEventEntityDao;

/**
 * @author Dmitry Nevolin
 */
public class TicketControlEventRepositoryImpl extends BaseCrudLocalDbRepository<TicketControlEvent, TicketControlEventEntity, Long> implements TicketControlEventRepository {

    @Inject
    TicketControlEventRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected TicketControlEventEntityDao dao() {
        return daoSession().getTicketControlEventEntityDao();
    }

    @Override
    protected LocalDbMapper<TicketControlEvent, TicketControlEventEntity> mapper() {
        return TicketControlEventMapper.INSTANCE;
    }

    @NonNull
    @Override
    public List<TicketControlEvent> loadAllNotExported() {
        ru.ppr.database.QueryBuilder qb = new ru.ppr.database.QueryBuilder();
        qb.selectAll().from(TicketControlEventEntityDao.TABLENAME);
        qb.leftJoin();
        qb.table(TicketControlExportEventEntityDao.TABLENAME);
        qb.on().field(TicketControlEventEntityDao.TABLENAME, TicketControlEventEntityDao.Properties.Id.columnName).eq()
                .field(TicketControlExportEventEntityDao.TABLENAME, TicketControlExportEventEntityDao.Properties.TicketControlEventId.columnName);
        qb.where().field(TicketControlEventEntityDao.TABLENAME, TicketControlEventEntityDao.Properties.Status.columnName).eq(TicketControlEvent.Status.COMPLETED.getCode());
        qb.and().field(TicketControlExportEventEntityDao.TABLENAME, TicketControlExportEventEntityDao.Properties.Id.columnName).isNull();

        List<TicketControlEvent> ticketControlEvents = new ArrayList<>();
        Cursor cursor = qb.build().run(db());
        while (cursor.moveToNext()) {
            TicketControlEvent ticketControlEvent = mapper().entityToModel(dao().readEntity(cursor, 0));
            ticketControlEvents.add(ticketControlEvent);
        }
        return ticketControlEvents;
    }

}

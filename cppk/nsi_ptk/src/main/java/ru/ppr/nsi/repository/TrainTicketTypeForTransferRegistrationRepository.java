package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.TrainTicketTypeForTransferRegistrationDao;
import ru.ppr.nsi.entity.TrainTicketTypeForTransferRegistration;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Aleksandr Brazhkin
 */
@Singleton
public class TrainTicketTypeForTransferRegistrationRepository extends BaseRepository<TrainTicketTypeForTransferRegistration, Integer> {

    @Inject
    TrainTicketTypeForTransferRegistrationRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<TrainTicketTypeForTransferRegistration, Integer> selfDao() {
        return daoSession().getTrainTicketTypeForTransferRegistrationDao();
    }

    /**
     * Возвращает список кодов типов ПД, допустимых для оформления с привязкой к ПД на поезд
     * с типом {@code trainTicketTypeCode} и напрвлением {@code trainTicketWayTypeCode}.
     *
     * @param trainTicketTypeCode    Тип ПД на поезд
     * @param trainTicketWayTypeCode Код Направления
     * @param versionId              Версия НСИ
     */
    @NonNull
    public List<Long> getTransferTicketTypeCodes(long trainTicketTypeCode, int trainTicketWayTypeCode, int versionId) {
        QueryBuilder qb = new QueryBuilder();
        qb.selectDistinct()
                .field(TrainTicketTypeForTransferRegistrationDao.Properties.TransferTicketTypeCode)
                .from(TrainTicketTypeForTransferRegistrationDao.TABLE_NAME)
                .where()
                .field(TrainTicketTypeForTransferRegistrationDao.Properties.TrainTicketTypeCode).eq(trainTicketTypeCode)
                .and()
                .appendInBrackets(() -> {
                    qb.field(TrainTicketTypeForTransferRegistrationDao.Properties.TrainTicketWayTypeCode).eq(trainTicketWayTypeCode);
                    qb.or().field(TrainTicketTypeForTransferRegistrationDao.Properties.TrainTicketWayTypeCode).isNull();
                })
                .and()
                .appendRaw(NsiUtils.checkVersion(TrainTicketTypeForTransferRegistrationDao.TABLE_NAME, versionId));

        List<Long> ticketTypeCodes = new ArrayList<>();
        Cursor cursor = qb.build().run(daoSession().getNsiDb());

        try {
            while (cursor.moveToNext()) {
                ticketTypeCodes.add(cursor.getLong(0));
            }
        } finally {
            cursor.close();
        }

        return ticketTypeCodes;
    }

}

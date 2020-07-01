package ru.ppr.chit.data.repository.local;

import android.database.Cursor;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.mapper.local.PassengerPersonalDataMapper;
import ru.ppr.chit.data.repository.local.base.BaseCrudLocalDbRepository;
import ru.ppr.chit.domain.model.local.PassengerPersonalData;
import ru.ppr.chit.domain.model.local.PassengerWithTicketId;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.repository.local.PassengerPersonalDataRepository;
import ru.ppr.chit.localdb.entity.PassengerPersonalDataEntity;
import ru.ppr.chit.localdb.greendao.PassengerPersonalDataEntityDao;
import ru.ppr.chit.localdb.greendao.TicketBoardingEntityDao;
import ru.ppr.chit.localdb.greendao.TicketControlEventEntityDao;
import ru.ppr.chit.localdb.greendao.TicketEntityDao;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;

/**
 * @author Dmitry Nevolin
 */
public class PassengerPersonalDataRepositoryImpl extends BaseCrudLocalDbRepository<PassengerPersonalData, PassengerPersonalDataEntity, Long> implements PassengerPersonalDataRepository {

    private static final String IS_CURRENT_STATION_DEPART_COLUMN = "IsCurrentStation";
    private static final String TICKET_BOARDING_TEMP_TABLE = "TICKET_BOARDING_TEMP_TABLE";
    private static final String WAS_BOARDING_FIELD = "WB";
    private static final String SORT_FIELD = "SORT";

    @Inject
    PassengerPersonalDataRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    protected AbstractDao<PassengerPersonalDataEntity, Long> dao() {
        return daoSession().getPassengerPersonalDataEntityDao();
    }

    @Override
    protected LocalDbMapper<PassengerPersonalData, PassengerPersonalDataEntity> mapper() {
        return PassengerPersonalDataMapper.INSTANCE;
    }

    /**
     * Загружает записи пасажиров с билетами
     *
     * @param fioFilter            Фильтр по ФИО
     * @param documentNumberFilter Фильтр по номеру документа
     * @param recordsOffset        Пропускает соответствуещее количество записей
     * @param pageLimit            Ограничение на количество загружаемых записей
     * @param currentStationCode   Текущая станция посадки, нужна для вычисления признака посадки на текущей станции и правильной сортировки данных в запросе
     * @return Список пасажиров с билетами
     */
    @Override
    public List<PassengerWithTicketId> loadPassengersWithTicketId(@Nullable String fioFilter, @Nullable String documentNumberFilter,
                                                                  int recordsOffset, int pageLimit, long currentStationCode) {
        QueryBuilder qb = new QueryBuilder();

/*
        SELECT *,
               CASE
                   WHEN (WB = 1) THEN 2
                   WHEN (IsCurrentStation = 1) THEN 1
                   ELSE 3
               END AS
        SORT
        FROM
          (SELECT PassengerPersonalData.LastName,
                  PassengerPersonalData.FirstName,
                  PassengerPersonalData.MiddleName,
                  PassengerPersonalData.DocumentNumber,
                  PassengerPersonalData.DocumentTypeCode,
                  Ticket._id,
                  Ticket.DepartureStationCode,
                  TICKET_BOARDING_TEMP_TABLE.WasBoarded AS WB,
                  CASE
                      WHEN (TICKET_BOARDING_TEMP_TABLE.WasBoarded IS NULL
                            OR TICKET_BOARDING_TEMP_TABLE.WasBoarded = 0)
                           AND Ticket.DepartureStationCode = 2000007 THEN 1
                      ELSE 0
                  END AS IsCurrentStation
           FROM Ticket
           LEFT JOIN
             (SELECT *
              FROM TicketBoarding
              WHERE _id IN
                  (SELECT TicketControlEvent.TicketBoardingId
                   FROM TicketControlEvent
                   WHERE TicketControlEvent.Status = 20 )
              GROUP BY TicketBoarding.TicketIdId) TICKET_BOARDING_TEMP_TABLE ON Ticket.TicketIdId = TICKET_BOARDING_TEMP_TABLE.TicketIdId
           INNER JOIN PassengerPersonalData ON PassengerPersonalData._id = Ticket.PassengerId
           WHERE 1 = 1 )
        ORDER BY
        SORT,
                 LastName
        LIMIT 50
        OFFSET 0

        */

        //Берем всех пассажиров, билеты к ним, к каждому билету ищем последнее событие посадки со статусом COMPLETED и выводим в список

        qb.selectAll().comma()
                .caseStart()
                .caseWhen().appendRaw("(").field(WAS_BOARDING_FIELD).eq().appendRaw(" 1) ").caseThen().appendRaw(2)
                .caseWhen().appendRaw("(").field(IS_CURRENT_STATION_DEPART_COLUMN).eq().appendRaw(" 1) ").caseThen().appendRaw(1)
                .caseElse().appendRaw(3)
                .caseEnd().as(SORT_FIELD)
                .from().appendRaw("(");

        {
            qb.select()
                    .field(PassengerPersonalDataEntityDao.TABLENAME, PassengerPersonalDataEntityDao.Properties.LastName.columnName).comma()
                    .field(PassengerPersonalDataEntityDao.TABLENAME, PassengerPersonalDataEntityDao.Properties.FirstName.columnName).comma()
                    .field(PassengerPersonalDataEntityDao.TABLENAME, PassengerPersonalDataEntityDao.Properties.MiddleName.columnName).comma()
                    .field(PassengerPersonalDataEntityDao.TABLENAME, PassengerPersonalDataEntityDao.Properties.DocumentNumber.columnName).comma()
                    .field(PassengerPersonalDataEntityDao.TABLENAME, PassengerPersonalDataEntityDao.Properties.DocumentTypeCode.columnName).comma()
                    .field(TicketEntityDao.TABLENAME, TicketEntityDao.Properties.Id.columnName).comma()
                    .field(TicketEntityDao.TABLENAME, TicketEntityDao.Properties.DepartureStationCode.columnName).comma()
                    .field(TICKET_BOARDING_TEMP_TABLE, TicketBoardingEntityDao.Properties.WasBoarded.columnName).as(WAS_BOARDING_FIELD).comma()
                    .caseStart() // Если не посажен и садится на текущей станции, то = 1, иначе = 0
                    .caseWhen()
                    .appendRaw("(").field(TICKET_BOARDING_TEMP_TABLE, TicketBoardingEntityDao.Properties.WasBoarded.columnName).isNull()
                    .or().field(TICKET_BOARDING_TEMP_TABLE, TicketBoardingEntityDao.Properties.WasBoarded.columnName).eq().appendRaw(0)
                    .appendRaw(")")
                    .and().field(TicketEntityDao.TABLENAME, TicketEntityDao.Properties.DepartureStationCode.columnName).eq(currentStationCode)
                    .caseThen().appendRaw(1).caseElse().appendRaw(0)
                    .caseEnd()
                    .as(IS_CURRENT_STATION_DEPART_COLUMN);
            qb.from(TicketEntityDao.TABLENAME);
            qb.leftJoin();
            qb.appendInBrackets(() -> {
                //берем все записи из TicketBoarding для COMPLETED билетов с уникальными TicketIdId (берется последняя запись)
                qb.selectAll().from(TicketBoardingEntityDao.TABLENAME).where().field(TicketBoardingEntityDao.Properties.Id.columnName).in();
                qb.appendInBrackets(() -> {
                    qb.select().field(TicketControlEventEntityDao.TABLENAME, TicketControlEventEntityDao.Properties.TicketBoardingId.columnName).from(TicketControlEventEntityDao.TABLENAME)
                            .where()
                            .field(TicketControlEventEntityDao.TABLENAME, TicketControlEventEntityDao.Properties.Status.columnName).eq(TicketControlEvent.Status.COMPLETED.getCode());
                });
                qb.groupBy(TicketBoardingEntityDao.TABLENAME, TicketBoardingEntityDao.Properties.TicketIdId.columnName);
            });
            qb.appendRaw(TICKET_BOARDING_TEMP_TABLE)
                    .on()
                    .field(TicketEntityDao.TABLENAME, TicketEntityDao.Properties.TicketIdId.columnName)
                    .eq().field(TICKET_BOARDING_TEMP_TABLE, TicketBoardingEntityDao.Properties.TicketIdId.columnName);

            qb.innerJoin(PassengerPersonalDataEntityDao.TABLENAME).on()
                    .field(PassengerPersonalDataEntityDao.TABLENAME, PassengerPersonalDataEntityDao.Properties.Id.columnName)
                    .eq().field(TicketEntityDao.TABLENAME, TicketEntityDao.Properties.PassengerId.columnName);

            //фильтры
            qb.where().trueCond();
            if (fioFilter != null) {
                qb.and().field(PassengerPersonalDataEntityDao.Properties.LastName.columnName).like("%" + fioFilter.toUpperCase() + "%");
            }
            if (documentNumberFilter != null) {
                qb.and().field(PassengerPersonalDataEntityDao.Properties.DocumentNumber.columnName).like("%" + documentNumberFilter.toUpperCase() + "%");
            }
        }
        qb.appendRaw(")");

        //сортировка
        qb.orderBy()
                .field(SORT_FIELD).comma()
                .field(PassengerPersonalDataEntityDao.Properties.LastName.columnName);

        //пагинация
        qb.limit(pageLimit).offset(recordsOffset);

        List<PassengerWithTicketId> passengers = new ArrayList<>();
        Query query = qb.build();
        Cursor cursor = query.run(db());
        try {
            while (cursor.moveToNext()) {
                PassengerWithTicketId passenger = new PassengerWithTicketId();
                passenger.setLastName(cursor.getString(0));
                passenger.setFirstName(cursor.getString(1));
                passenger.setMiddleName(cursor.getString(2));
                passenger.setDocumentNumber(cursor.getString(3));
                passenger.setDocumentTypeCode(cursor.getLong(4));
                passenger.setTicketId(cursor.getLong(5));
                passenger.setDepartureStationCode(cursor.getLong(6));
                passenger.setWasBoarded(cursor.getInt(7) != 0);
                passenger.setIsCurrentStationBoarding(cursor.getInt(8) != 0);
                passengers.add(passenger);
            }
        } finally {
            cursor.close();
        }
        return passengers;
    }
}

package ru.ppr.chit.localdb.daosession;

import java.util.HashMap;
import java.util.Map;

import ru.ppr.chit.localdb.entity.AppPropertyEntity;
import ru.ppr.chit.localdb.entity.AuthInfoEntity;
import ru.ppr.chit.localdb.entity.BoardingEventEntity;
import ru.ppr.chit.localdb.entity.BoardingExportEventEntity;
import ru.ppr.chit.localdb.entity.CarInfoEntity;
import ru.ppr.chit.localdb.entity.CarSchemeElementEntity;
import ru.ppr.chit.localdb.entity.CarSchemeEntity;
import ru.ppr.chit.localdb.entity.ControlStationEntity;
import ru.ppr.chit.localdb.entity.EventEntity;
import ru.ppr.chit.localdb.entity.ExchangeEventEntity;
import ru.ppr.chit.localdb.entity.LocalDbVersionEntity;
import ru.ppr.chit.localdb.entity.LocationEntity;
import ru.ppr.chit.localdb.entity.OAuth2TokenEntity;
import ru.ppr.chit.localdb.entity.PassengerEntity;
import ru.ppr.chit.localdb.entity.PassengerPersonalDataEntity;
import ru.ppr.chit.localdb.entity.PlaceLocationEntity;
import ru.ppr.chit.localdb.entity.SmartCardEntity;
import ru.ppr.chit.localdb.entity.StationInfoEntity;
import ru.ppr.chit.localdb.entity.TicketBoardingEntity;
import ru.ppr.chit.localdb.entity.TicketControlEventEntity;
import ru.ppr.chit.localdb.entity.TicketControlExportEventEntity;
import ru.ppr.chit.localdb.entity.TicketDataEntity;
import ru.ppr.chit.localdb.entity.TicketEntity;
import ru.ppr.chit.localdb.entity.TicketIdEntity;
import ru.ppr.chit.localdb.entity.TrainInfoEntity;
import ru.ppr.chit.localdb.entity.TripServiceEventEntity;
import ru.ppr.chit.localdb.entity.UserEntity;
import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.database.base.BaseTableDao;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.database.references.References;
import ru.ppr.logger.Logger;

/**
 * Класс, хранящий метаинформацию по таблицам (используется в DBGarbageCollector)
 *
 * @author m.sidorov
 */
public class LocalDaoEntities {

    private static final String TAG = Logger.makeLogTag(LocalDaoEntities.class);

    // Список таблиц
    public static Map<String, BaseTableDao> entities = new HashMap<>();

    // Описание связей таблиц
    public static References references = new References();

    // Регистрация метаинформации для всех таблиц
    public static synchronized void registerEntities(){
        Logger.info(TAG, "register local database entities");

        entities = new HashMap<>();
        references = new References();

        registerEntity(AppPropertyEntity.createMeta());
        registerEntity(AuthInfoEntity.createMeta());
        registerEntity(BoardingEventEntity.createMeta());
        registerEntity(BoardingExportEventEntity.createMeta());
        registerEntity(CarInfoEntity.createMeta());
        registerEntity(CarSchemeElementEntity.createMeta());
        registerEntity(CarSchemeEntity.createMeta());
        registerEntity(ControlStationEntity.createMeta());
        registerEntity(EventEntity.createMeta());
        registerEntity(ExchangeEventEntity.createMeta());
        registerEntity(LocalDbVersionEntity.createMeta());
        registerEntity(LocationEntity.createMeta());
        registerEntity(OAuth2TokenEntity.createMeta());
        registerEntity(PassengerEntity.createMeta());
        registerEntity(PassengerPersonalDataEntity.createMeta());
        registerEntity(PlaceLocationEntity.createMeta());
        registerEntity(SmartCardEntity.createMeta());
        registerEntity(StationInfoEntity.createMeta());
        registerEntity(TicketBoardingEntity.createMeta());
        registerEntity(TicketControlEventEntity.createMeta());
        registerEntity(TicketControlExportEventEntity.createMeta());
        registerEntity(TicketDataEntity.createMeta());
        registerEntity(TicketEntity.createMeta());
        registerEntity(TicketIdEntity.createMeta());
        registerEntity(TrainInfoEntity.createMeta());
        registerEntity(TripServiceEventEntity.createMeta());
        registerEntity(UserEntity.createMeta());

        // После регистрации всех таблиц, вызываем проверку корректности их декларациии
        checkDeclaration();
    }

    // Регистрирует таблицу
    private static void registerEntity(BaseLocalMeta entity) {
        entities.put(entity.getTableName(), entity);
        for (ReferenceInfo reference: entity.references){
            references.registerReference(reference);
        }
    }

    // Проверка корректности описания таблиц и их связей
    private static void checkDeclaration(){
        references.checkReferencesDeclaration(entities.values());
    }

}

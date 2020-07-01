package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.localdb.entity.ExchangeEventEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class ExchangeEventMapper implements LocalDbMapper<ExchangeEvent, ExchangeEventEntity> {

    public static final ExchangeEventMapper INSTANCE = Mappers.getMapper(ExchangeEventMapper.class);

    @Mapping(target = "event", ignore = true)
    @Override
    public abstract ExchangeEventEntity modelToEntity(ExchangeEvent model);

    @Mapping(target = "event", ignore = true)
    @Override
    public abstract ExchangeEvent entityToModel(ExchangeEventEntity entity);

    Integer mapExchangeEventType(ExchangeEvent.Type exchangeEventType) {
        return exchangeEventType != null ? exchangeEventType.getCode() : null;
    }

    ExchangeEvent.Type mapExchangeEventTypeCode(Integer code) {
        return code != null ? ExchangeEvent.Type.valueOf(code) : null;
    }

    Integer mapExchangeEventStatus(ExchangeEvent.Status exchangeEventStatus) {
        return exchangeEventStatus != null ? exchangeEventStatus.getCode() : null;
    }

    ExchangeEvent.Status mapExchangeEventStatusCode(Integer code) {
        return code != null ? ExchangeEvent.Status.valueOf(code) : null;
    }

}

package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.SmartCard;
import ru.ppr.chit.domain.model.nsi.TicketStorageType;
import ru.ppr.chit.localdb.entity.SmartCardEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class SmartCardMapper implements LocalDbMapper<SmartCard, SmartCardEntity> {

    public static final SmartCardMapper INSTANCE = Mappers.getMapper(SmartCardMapper.class);

    Integer mapType(TicketStorageType ticketStorageType) {
        return ticketStorageType != null ? ticketStorageType.getCode() : null;
    }

    TicketStorageType mapTypeCode(Integer code) {
        return code != null ? TicketStorageType.valueOf(code) : null;
    }

}

package ru.ppr.chit.data.mapper.security;

import org.mapstruct.Mapper;

import ru.ppr.chit.domain.model.nsi.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public abstract class TicketStorageTypeMapper {

    Integer modelToEntity(TicketStorageType model) {
        return model == null ? null : model.getCode();
    }

    TicketStorageType entityToModel(Integer entity) {
        return entity == null ? null : TicketStorageType.valueOf(entity);
    }
}

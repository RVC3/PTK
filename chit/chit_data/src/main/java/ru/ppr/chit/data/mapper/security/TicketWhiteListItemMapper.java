package ru.ppr.chit.data.mapper.security;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.security.TicketWhiteListItem;
import ru.ppr.chit.securitydb.entity.TicketWhiteListItemEntity;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper(uses = {TicketIdMapper.class})
public interface TicketWhiteListItemMapper extends SecurityDbMapper<TicketWhiteListItem, TicketWhiteListItemEntity> {

    TicketWhiteListItemMapper INSTANCE = Mappers.getMapper(TicketWhiteListItemMapper.class);

}

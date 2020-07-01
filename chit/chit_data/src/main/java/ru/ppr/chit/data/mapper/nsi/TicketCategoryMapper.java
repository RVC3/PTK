package ru.ppr.chit.data.mapper.nsi;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.nsi.TicketCategory;
import ru.ppr.chit.nsidb.entity.TicketCategoryEntity;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public interface TicketCategoryMapper extends NsiDbMapper<TicketCategory, TicketCategoryEntity> {

    TicketCategoryMapper INSTANCE = Mappers.getMapper(TicketCategoryMapper.class);

    @Mappings({
            @Mapping(target = "expressTicketCategoryCode", ignore = true),
            @Mapping(target = "abbreviation", ignore = true),
            @Mapping(target = "presale", ignore = true),
            @Mapping(target = "delayPassback", ignore = true),
            @Mapping(target = "changedDateTime", ignore = true),
            @Mapping(target = "compositeType", ignore = true)
    })
    @Override
    TicketCategoryEntity modelToEntity(TicketCategory model);

}

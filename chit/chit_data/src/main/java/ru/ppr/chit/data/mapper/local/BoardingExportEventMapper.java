package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.BoardingExportEvent;
import ru.ppr.chit.localdb.entity.BoardingExportEventEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public interface BoardingExportEventMapper extends LocalDbMapper<BoardingExportEvent, BoardingExportEventEntity> {

    BoardingExportEventMapper INSTANCE = Mappers.getMapper(BoardingExportEventMapper.class);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "boardingEvent", ignore = true)
    })
    @Override
    BoardingExportEventEntity modelToEntity(BoardingExportEvent model);

    @Mappings({
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "boardingEvent", ignore = true)
    })
    @Override
    BoardingExportEvent entityToModel(BoardingExportEventEntity entity);
    
}

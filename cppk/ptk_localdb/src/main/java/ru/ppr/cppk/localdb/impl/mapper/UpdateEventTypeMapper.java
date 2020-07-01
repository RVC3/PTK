package ru.ppr.cppk.localdb.impl.mapper;

import android.support.annotation.Nullable;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.cppk.localdb.model.UpdateEventType;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public abstract class UpdateEventTypeMapper {

    public static final UpdateEventTypeMapper INSTANCE = Mappers.getMapper(UpdateEventTypeMapper.class);

    @Nullable
    public UpdateEventType entityToModel(@Nullable Integer entity) {
        return entity == null ? null : UpdateEventType.valueOf(entity);
    }

    @Nullable
    public Integer modelToEntity(@Nullable UpdateEventType model) {
        return model == null ? null : model.getCode();
    }
}

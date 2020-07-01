package ru.ppr.chit.data.mapper.local;

import android.support.annotation.Nullable;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Date;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public class LongDateMapper {

    public static final LongDateMapper INSTANCE = Mappers.getMapper(LongDateMapper.class);

    public Date entityToModel(@Nullable Long entity) {
        return entity == null ? null : new Date(entity);
    }

    public Long modelToEntity(@Nullable Date model) {
        return model == null ? null : model.getTime();
    }
}

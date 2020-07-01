package ru.ppr.chit.data.mapper.local;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.local.CarSchemeElement;
import ru.ppr.chit.domain.model.local.PlaceDirection;
import ru.ppr.chit.localdb.entity.CarSchemeElementEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class CarSchemeElementMapper implements LocalDbMapper<CarSchemeElement, CarSchemeElementEntity> {

    public static final CarSchemeElementMapper INSTANCE = Mappers.getMapper(CarSchemeElementMapper.class);

    @Mapping(target = "carSchemeId", ignore = true)
    @Override
    public abstract CarSchemeElementEntity modelToEntity(CarSchemeElement model);

    Integer mapCarSchemeElementKind(CarSchemeElement.Kind carSchemeElementKind) {
        return carSchemeElementKind != null ? carSchemeElementKind.getCode() : null;
    }

    CarSchemeElement.Kind mapCarSchemeElementKindCode(Integer code) {
        return code != null ? CarSchemeElement.Kind.valueOf(code) : null;
    }

    Integer mapPlaceDirection(PlaceDirection placeDirection) {
        return placeDirection != null ? placeDirection.getCode() : null;
    }

    PlaceDirection mapPlaceDirectionCode(Integer code) {
        return code != null ? PlaceDirection.valueOf(code) : null;
    }

}

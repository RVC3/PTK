package ru.ppr.chit.data.mapper.nsi;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.nsi.Exemption;
import ru.ppr.chit.nsidb.entity.ExemptionEntity;

/**
 * @author Aleksandr Brazhkin
 */
@Mapper
public interface ExemptionMapper extends NsiDbMapper<Exemption, ExemptionEntity> {

    ExemptionMapper INSTANCE = Mappers.getMapper(ExemptionMapper.class);

}

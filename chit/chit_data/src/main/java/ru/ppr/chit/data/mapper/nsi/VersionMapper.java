package ru.ppr.chit.data.mapper.nsi;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ru.ppr.chit.domain.model.nsi.Version;
import ru.ppr.chit.nsidb.entity.VersionEntity;

/**
 * @author Dmitry Nevolin
 */
@Mapper
public abstract class VersionMapper implements NsiDbMapper<Version, VersionEntity> {

    public static final VersionMapper INSTANCE = Mappers.getMapper(VersionMapper.class);

    Integer mapVersionStatus(Version.Status versionStatus) {
        return versionStatus != null ? versionStatus.getCode() : null;
    }

    Version.Status mapVersionStatusCode(Integer code) {
        return code != null ? Version.Status.valueOf(code) : null;
    }

}

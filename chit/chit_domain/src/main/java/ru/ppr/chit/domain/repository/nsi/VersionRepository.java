package ru.ppr.chit.domain.repository.nsi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.EnumSet;

import ru.ppr.chit.domain.model.nsi.Version;
import ru.ppr.chit.domain.repository.nsi.base.NsiDbRepository;

/**
 * @author Dmitry Nevolin
 */
public interface VersionRepository extends NsiDbRepository {

    @Nullable
    Version load(int versionId);

    @Nullable
    Version loadForDate(@NonNull Date date, @Nullable EnumSet<Version.Status> statusSet);

}

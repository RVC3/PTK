package ru.ppr.chit.domain.repository.security;

import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.security.SecurityStopListVersion;

/**
 * Created by m.sidorov.
 */
public interface SecurityStopListVersionRepository {

    @Nullable
    SecurityStopListVersion loadLast();

}

package ru.ppr.chit.domain.provider;

import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.security.PtkDataContractsVersion;
import ru.ppr.chit.domain.model.security.SecurityStopListVersion;
import ru.ppr.chit.domain.repository.security.PtkDataContractsVersionRepository;
import ru.ppr.chit.domain.repository.security.SecurityStopListVersionRepository;

/**
 * Предоставляет версию базы безопасности
 *
 * @author Dmitry Nevolin
 */
public class SecurityVersionProvider {

    /**
     * Начальная версия базы безопасности, используется когда мы не можем
     * получить текущую версию базы безопасности из-за её отсутствия
     */
    private static final int INITIAL_SECURITY_VERSION = 0;

    private final PtkDataContractsVersionRepository ptkDataContractsVersionRepository;
    private final SecurityStopListVersionRepository securityStopListVersionRepository;

    private final SimpleDateFormat utcDateFormat;


    @Inject
    SecurityVersionProvider(PtkDataContractsVersionRepository ptkDataContractsVersionRepository,
                            SecurityStopListVersionRepository securityStopListVersionRepository ) {
        this.ptkDataContractsVersionRepository = ptkDataContractsVersionRepository;
        this.securityStopListVersionRepository = securityStopListVersionRepository;

        utcDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public int getCurrentSecurityVersion() {
        PtkDataContractsVersion ptkDataContractsVersion =ptkDataContractsVersionRepository.loadLast();
        return ptkDataContractsVersion != null ? ptkDataContractsVersion.getVersion() : INITIAL_SECURITY_VERSION;
    }

    @Nullable
    public SecurityStopListVersion getSecurityStopListVersion() {
        return securityStopListVersionRepository.loadLast();
    }

    public String formatUtcDate(Date date){
        if (date == null){
            return null;
        }
        return utcDateFormat.format(date);
    }

}

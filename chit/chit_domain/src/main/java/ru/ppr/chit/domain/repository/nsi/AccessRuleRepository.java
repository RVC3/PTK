package ru.ppr.chit.domain.repository.nsi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumSet;
import java.util.List;

import ru.ppr.chit.domain.model.nsi.AccessRule;
import ru.ppr.chit.domain.repository.nsi.base.NsiDbRepository;

/**
 * Репозиторий для правил доступа
 *
 * @author Dmitry Nevolin
 */
public interface AccessRuleRepository extends NsiDbRepository {

    /**
     * Загружает все правила доступа для указанных агрументов
     *
     * @param sectorNumber номер сектора mifare карты
     * @param keyTypeSet тип ключа (r/w/rw)
     * @param allowedAccessSchemeCodeList список кодов разрешенных для выборки схем доступа
     * @param deniedAccessSchemeCodeList список кодов запрещенных для выборки схем доступа
     * @param nsiVersion версия НСИ
     * @return все найденные правила доступа
     */
    @NonNull
    List<AccessRule> loadAllForNewAccess(int sectorNumber,
                                         @NonNull EnumSet<AccessRule.KeyType> keyTypeSet,
                                         @Nullable List<Long> allowedAccessSchemeCodeList,
                                         @Nullable List<Long> deniedAccessSchemeCodeList,
                                         int nsiVersion);

}

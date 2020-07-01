package ru.ppr.chit.rfid;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.nsi.TicketStorageType;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.domain.repository.nsi.AccessRuleRepository;
import ru.ppr.chit.domain.repository.nsi.AccessSchemeRepository;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.AccessRule;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.AccessScheme;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.NsiDataProvider;

/**
 * @author Aleksandr Brazhkin
 */
public class NsiDataProviderImpl implements NsiDataProvider {

    private static final EnumSet<ru.ppr.chit.domain.model.nsi.AccessRule.KeyType> READ_KEY_TYPE_SET = EnumSet.of(
            ru.ppr.chit.domain.model.nsi.AccessRule.KeyType.READ,
            ru.ppr.chit.domain.model.nsi.AccessRule.KeyType.READ_AND_WRITE);
    private static final EnumSet<ru.ppr.chit.domain.model.nsi.AccessRule.KeyType> WRITE_KEY_TYPE_SET = EnumSet.of(
            ru.ppr.chit.domain.model.nsi.AccessRule.KeyType.WRITE,
            ru.ppr.chit.domain.model.nsi.AccessRule.KeyType.READ_AND_WRITE);

    private final AccessRuleRepository accessRuleRepository;
    private final AccessSchemeRepository accessSchemeRepository;
    private final NsiVersionProvider nsiVersionProvider;

    @Inject
    NsiDataProviderImpl(AccessRuleRepository accessRuleRepository,
                        AccessSchemeRepository accessSchemeRepository,
                        NsiVersionProvider nsiVersionProvider) {
        this.accessRuleRepository = accessRuleRepository;
        this.accessSchemeRepository = accessSchemeRepository;
        this.nsiVersionProvider = nsiVersionProvider;
    }

    @Override
    public List<Pair<AccessScheme, AccessRule>> provideSchemeRules(int sectorNumber,
                                                                   boolean forRead,
                                                                   @Nullable List<Integer> accessSchemeCodesIn,
                                                                   @Nullable List<Integer> accessSchemeCodesNotIn) {
        List<Long> allowedAccessSchemeCodeList = null;
        if (accessSchemeCodesIn != null) {
            allowedAccessSchemeCodeList = new ArrayList<>();
            for (Integer code : accessSchemeCodesIn) {
                allowedAccessSchemeCodeList.add(Long.valueOf(code));
            }
        }
        List<Long> deniedAccessSchemeCodeList = null;
        if (accessSchemeCodesNotIn != null) {
            deniedAccessSchemeCodeList = new ArrayList<>();
            for (Integer code : accessSchemeCodesNotIn) {
                deniedAccessSchemeCodeList.add(Long.valueOf(code));
            }
        }
        int nsiVersion = nsiVersionProvider.getCurrentNsiVersion();
        List<ru.ppr.chit.domain.model.nsi.AccessRule> accessRuleList = accessRuleRepository.loadAllForNewAccess(
                sectorNumber,
                forRead ? READ_KEY_TYPE_SET : WRITE_KEY_TYPE_SET,
                allowedAccessSchemeCodeList,
                deniedAccessSchemeCodeList,
                nsiVersion);
        List<Pair<AccessScheme, AccessRule>> providedSchemeRules = new ArrayList<>();
        for (ru.ppr.chit.domain.model.nsi.AccessRule accessRule : accessRuleList) {
            providedSchemeRules.add(new Pair<>(
                    AccessSchemeMapper.INSTANCE.entityToModel(accessRule.getAccessScheme(accessSchemeRepository, nsiVersion)),
                    AccessRuleMapper.INSTANCE.entityToModel(accessRule)));
        }
        return providedSchemeRules;
    }

    @Override
    public List<Integer> getAccessSchemeCodes(Integer ticketStorageTypeCode) {
        List<Integer> accessSchemeCodeList = new ArrayList<>();
        List<ru.ppr.chit.domain.model.nsi.AccessScheme> accessSchemeList = accessSchemeRepository.loadAllByTicketStorageTypeSet(
                ticketStorageTypeCode != null ? EnumSet.of(TicketStorageType.valueOf(ticketStorageTypeCode)) : null,
                nsiVersionProvider.getCurrentNsiVersion());
        for (ru.ppr.chit.domain.model.nsi.AccessScheme accessScheme : accessSchemeList) {
            accessSchemeCodeList.add(accessScheme.getCode().intValue());
        }
        return accessSchemeCodeList;
    }

}

package ru.ppr.cppk.dataCarrier.smartCard.findcardtask;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.AccessRule;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.AccessScheme;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.NsiDataProvider;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class NsiDataProviderImpl implements NsiDataProvider {

    private static final List<Integer> KEY_TYPES_FOR_READ = Arrays.asList(ru.ppr.nsi.entity.AccessRule.KEY_TYPE_READ, ru.ppr.nsi.entity.AccessRule.KEY_TYPE_READ_AND_WRITE);
    private static final List<Integer> KEY_TYPES_FOR_WRITE = Arrays.asList(ru.ppr.nsi.entity.AccessRule.KEY_TYPE_WRITE, ru.ppr.nsi.entity.AccessRule.KEY_TYPE_READ_AND_WRITE);

    private final NsiDaoSession nsiDaoSession;
    private final NsiVersionManager nsiVersionManager;

    @Inject
    public NsiDataProviderImpl(NsiDaoSession nsiDaoSession, NsiVersionManager nsiVersionManager) {
        this.nsiDaoSession = nsiDaoSession;
        this.nsiVersionManager = nsiVersionManager;
    }

    @Override
    public List<Pair<AccessScheme, AccessRule>> provideSchemeRules(int sectorNumber, boolean forRead, @Nullable List<Integer> accessSchemeCodesIn, @Nullable List<Integer> accessSchemeCodesNotIn) {
        Map<ru.ppr.nsi.entity.AccessScheme, List<ru.ppr.nsi.entity.AccessRule>> map = nsiDaoSession.getAccessRuleDao().getNewAccess(
                sectorNumber,
                forRead ? KEY_TYPES_FOR_READ : KEY_TYPES_FOR_WRITE,
                accessSchemeCodesIn,
                accessSchemeCodesNotIn,
                nsiVersionManager.getCurrentNsiVersionId()
        );

        ArrayList<Pair<AccessScheme, AccessRule>> out = new ArrayList<>();

        for (Map.Entry<ru.ppr.nsi.entity.AccessScheme, List<ru.ppr.nsi.entity.AccessRule>> accessSchemeEntry : map.entrySet()) {
            ru.ppr.nsi.entity.AccessScheme accessScheme = accessSchemeEntry.getKey();
            List<ru.ppr.nsi.entity.AccessRule> accessRules = accessSchemeEntry.getValue();
            if (accessRules.size() != 1) {
                throw new IllegalStateException("Invalid AccessRule count for numSector = " + sectorNumber);
            }
            out.add(new Pair<>(AccessSchemeMapper.INSTANCE.entityToModel(accessScheme), AccessRuleMapper.INSTANCE.entityToModel(accessRules.get(0))));
        }

        return out;
    }

    @Override
    public List<Integer> getAccessSchemeCodes(Integer ticketStorageTypeCode) {
        List<ru.ppr.nsi.entity.TicketStorageType> ticketStorageTypes = ticketStorageTypeCode == null ? null : Collections.singletonList(ru.ppr.nsi.entity.TicketStorageType.getTypeByDBCode(ticketStorageTypeCode));
        return nsiDaoSession.getAccessSchemeDao().getAccessSchemeCodes(ticketStorageTypes, nsiVersionManager.getCurrentNsiVersionId());
    }
}

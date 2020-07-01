package ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi;

import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ppr.logger.Logger;
import ru.ppr.rfid.SamAccessRule;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfidreal.RfidReal;

/**
 * Реализация алгоритма авторизации по SAM-модулю, основанная на данных из НСИ.
 *
 * @author Aleksandr Brazhkin
 */
public class NsiSamAuthorizationStrategy implements SamAuthorizationStrategy {

    private final NsiDataProvider nsiDataProvider;

    private List<Integer> accessSchemeCodesIn = new ArrayList<>();
    /**
     * Неподошедшие схемы доступа из БД
     */
    private List<Integer> accessSchemeCodesNotIn = new ArrayList<>();

    private List<Pair<AccessScheme, AccessRule>> samDbAccess = null;
    private int index = 0;
    private boolean lastAuthSuccess = false;
    private int currentSectorNum;
    private boolean currentForRead;
    private Pair<AccessScheme, AccessRule> currentPair;

    private Set<SamAccessRule> checkedSamKeys;

    public static final String TAG="teest SAM";

    /**
     * Сохраненное состояние авторизационного процесса
     */
    private State savedState = null;

    /**
     * Состояние авторизационного процесса
     */
    private class State {

        final List<Integer> accessSchemeCodesIn;
        final List<Integer> accessSchemeCodesNotIn;
        final List<Pair<AccessScheme, AccessRule>> samDbAccess;
        final int index;
        final boolean lastAuthSuccess;
        final int currentSectorNum;
        final boolean currentForRead;
        final Pair<AccessScheme, AccessRule> currentPair;


        private State(List<Integer> accessSchemeCodesIn,
                      List<Integer> accessSchemeCodesNotIn,
                      List<Pair<AccessScheme, AccessRule>> samDbAccess,
                      int index, boolean lastAuthSuccess,
                      int currentSectorNum,
                      boolean currentForRead,
                      Pair<AccessScheme, AccessRule> currentPair) {
            this.accessSchemeCodesIn = accessSchemeCodesIn;
            this.accessSchemeCodesNotIn = accessSchemeCodesNotIn;
            this.samDbAccess = samDbAccess;
            this.index = index;
            this.lastAuthSuccess = lastAuthSuccess;
            this.currentSectorNum = currentSectorNum;
            this.currentForRead = currentForRead;
            this.currentPair = currentPair;
        }
    }

    public NsiSamAuthorizationStrategy(NsiDataProvider nsiDataProvider, @Nullable Integer ticketStorageTypeCode) {
        this.nsiDataProvider = nsiDataProvider;
        setTicketStorageTypeCode(ticketStorageTypeCode);
        checkedSamKeys = new LinkedHashSet<>();
    }

    @Override
    public SamAccessRule getKey(int sectorNum, boolean forRead) {
        Logger.debug(TAG, "getKey() called with: sectorNum = [" + sectorNum + "], forRead = [" + forRead + "]");

        if (lastAuthSuccess) {
            if (sectorNum == currentPair.second.getSectorNumber()
                    && (currentPair.second.getKeyType() == AccessRule.KeyType.READ_AND_WRITE
                    || forRead && currentPair.second.getKeyType() == AccessRule.KeyType.READ
                    || !forRead && currentPair.second.getKeyType() == AccessRule.KeyType.WRITE)) {
                // Текущий ключ удовлетворяет требованиям, вернем его же.

                SamAccessRule samAccessRule = new SamAccessRule();
                samAccessRule.setSamSlotNumber(currentPair.first.getSamSlotNumber());
                samAccessRule.setSectorNumber(currentPair.second.getSectorNumber());
                samAccessRule.setSamKeyVersion(currentPair.second.getSamKeyVersion());
                samAccessRule.setCellNumber(currentPair.second.getCellNumber());
                samAccessRule.setKeyName(currentPair.second.getKeyName().getCode());

                return samAccessRule;
            }
        }

        if (currentSectorNum != sectorNum || currentForRead != forRead) {
            // Условия изменилсь, нужно загрузить свежие данные из БД.
            // Загрузим схемы для текущего предполагаемого типа карты, например СКМ.
            samDbAccess = nsiDataProvider.provideSchemeRules(
                    sectorNum,
                    forRead,
                    accessSchemeCodesIn.isEmpty() ? null : accessSchemeCodesIn,
                    accessSchemeCodesNotIn.isEmpty() ? null : accessSchemeCodesNotIn
            );

            if (lastAuthSuccess && samDbAccess.size() == 0) {
                //Если схем к новому сектору для того же типа карты с которым последний раз удалось авторизоваться получить не удалось
                //значит мы сделали неверное предположение насчет типа карты, пробуем получить для любого типа.
                addFailedSheme(currentPair.first);
                setTicketStorageTypeCode(null);
                samDbAccess = nsiDataProvider.provideSchemeRules(
                        sectorNum,
                        forRead,
                        accessSchemeCodesIn.isEmpty() ? null : accessSchemeCodesIn,
                        accessSchemeCodesNotIn.isEmpty() ? null : accessSchemeCodesNotIn
                );
            }

            index = 0;
        }

        currentSectorNum = sectorNum;
        currentForRead = forRead;

        while (index < samDbAccess.size()) {

            currentPair = samDbAccess.get(index);

            AccessScheme accessScheme = currentPair.first;
            AccessRule accessRule = currentPair.second;

            if (isSchemeFailed(accessScheme)) {
                Logger.trace(RfidReal.class, "схему " + accessScheme.getName()
                        + " (SchemeCode:" + accessScheme.getCode()
                        + ") - даже пробовать не будем, точно не подходит!");

                index++;
                continue;
            }

            StringBuilder log = new StringBuilder("Подбор Схемы Авторизации:\n");

            StringBuilder schemesList = new StringBuilder("Номера схем в порядке приоритета: ");

            for (int j = 0; j < samDbAccess.size(); j++) {
                if (j == index) schemesList.append("current=");
                schemesList.append(samDbAccess.get(j).first.getCode()).append(" ");
            }
            schemesList.append("\n");
            log.append(schemesList);
            log.append(accessRule.toString());

            SamAccessRule samAccessRule = new SamAccessRule();
            samAccessRule.setSamSlotNumber(currentPair.first.getSamSlotNumber());
            samAccessRule.setSectorNumber(currentPair.second.getSectorNumber());
            samAccessRule.setSamKeyVersion(currentPair.second.getSamKeyVersion());
            samAccessRule.setCellNumber(currentPair.second.getCellNumber());
            samAccessRule.setKeyName(currentPair.second.getKeyName().getCode());

            if(checkedSamKeys.contains(samAccessRule)){
                Logger.debug(TAG, "getKey: sam key [" + samAccessRule + "] already tested");
                addFailedSheme(currentPair.first);
                index++;
                continue;
            }

            return samAccessRule;
        }

        return null;
    }

    @Override
    public void setLastSamAccessRuleStatus(boolean success) {
        lastAuthSuccess = success;
        if (success) {
            // запоминаем тип карты от которой подошли ключи, в
            // след. раз будем использовать ее
            addSuccessScheme(currentPair.first);
        } else {
            index++;
            // запоминаем номер схемы доступа
            addFailedSheme(currentPair.first);
            AccessRule rule = currentPair.second;
            checkedSamKeys.add(new SamAccessRule(
                    currentPair.first.getSamSlotNumber(),
                    rule.getSectorNumber(),
                    rule.getSamKeyVersion(),
                    rule.getCellNumber(),
                    rule.getKeyName().getCode()));
        }
    }

    @Override
    public void saveState() {
        savedState = new State(
                accessSchemeCodesIn == null ? null : new ArrayList<>(accessSchemeCodesIn),
                accessSchemeCodesNotIn == null ? null : new ArrayList<>(accessSchemeCodesNotIn),
                samDbAccess == null ? null : new ArrayList<>(samDbAccess),
                index,
                lastAuthSuccess,
                currentSectorNum,
                currentForRead,
                currentPair == null ? null : new Pair<>(currentPair.first, currentPair.second)
        );
    }

    @Override
    public void restoreState() {
        if (savedState != null) {
            accessSchemeCodesIn = savedState.accessSchemeCodesIn == null ? null : new ArrayList<>(savedState.accessSchemeCodesIn);
            accessSchemeCodesNotIn = savedState.accessSchemeCodesNotIn == null ? null : new ArrayList<>(savedState.accessSchemeCodesNotIn);
            samDbAccess = savedState.samDbAccess == null ? null : new ArrayList<>(savedState.samDbAccess);
            index = savedState.index;
            lastAuthSuccess = savedState.lastAuthSuccess;
            currentSectorNum = savedState.currentSectorNum;
            currentForRead = savedState.currentForRead;
            currentPair = savedState.currentPair == null ? null : new Pair<>(savedState.currentPair.first, savedState.currentPair.second);
        }
    }

    private void setTicketStorageTypeCode(Integer ticketStorageTypeCode) {
        accessSchemeCodesIn = ticketStorageTypeCode == null ? Collections.emptyList() : nsiDataProvider.getAccessSchemeCodes(ticketStorageTypeCode);
    }

    /**
     * Проверяет схему, может с такими параметрами мы уже пробовали авторизоваться и она не подошла
     *
     * @return - true - ножно не пробовать эту схему - точно не подойдет
     */
    private boolean isSchemeFailed(AccessScheme accessScheme) {
        return accessSchemeCodesNotIn.contains(accessScheme.getCode());
    }

    /**
     * Добавит в список подошедшую схему
     */
    private void addSuccessScheme(AccessScheme accessScheme) {
        setTicketStorageTypeCode(accessScheme.getTicketStorageTypeCode());
    }

    /**
     * Добавит в список косячную схему
     */
    private void addFailedSheme(AccessScheme accessScheme) {
        accessSchemeCodesNotIn.add(accessScheme.getCode());
    }
}

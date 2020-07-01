package ru.ppr.cppk.legacy;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.EnumSet;

import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.logic.SmartCardStopListChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.security.entity.StopCriteriaType;

/**
 * Валидатор смарт-карты.
 *
 * @author Aleksandr Brazhkin
 */
@Deprecated
public class BscInformationChecker {

    private static final String TAG = Logger.makeLogTag(BscInformationChecker.class);

    private final BscInformation bscInformation;
    private final NsiVersionManager nsiVersionManager;
    private final SmartCardCancellationReasonRepository smartCardCancellationReasonRepository;


    public BscInformationChecker(BscInformation bscInformation,
                                 NsiVersionManager nsiVersionManager,
                                 SmartCardCancellationReasonRepository smartCardCancellationReasonRepository) {
        this.bscInformation = bscInformation;
        this.nsiVersionManager = nsiVersionManager;
        this.smartCardCancellationReasonRepository = smartCardCancellationReasonRepository;
    }

    /**
     * Возвращает результат проверки карты по стоплистам и по сроку действия.
     *
     * @return
     */
    public boolean isCardValid(boolean forControl) {
        boolean res = !(isStopList(forControl) || !bscInformation.cardTimeIsValid());
        Logger.trace(TAG, "isCardValid: " + res);
        return res;
    }

    /**
     * Проверяет карту по стоп-листам
     *
     * @return true - карта в стоп-листе
     */
    public boolean isStopList(boolean forControl) {
        boolean result = getStopListItem(forControl) != null;
        Logger.trace(TAG, "card in stop list - " + result);

        return result;
    }

    /**
     * Возвращает StopListItem для данной карты. При отсутствии карты в
     * стоплисте возвращается null
     *
     * @return
     */
    @Nullable
    public Pair<SmartCardStopListItem, String> getStopListItem(boolean forControl) {

        final SmartCard smartCard = new SmartCardBuilder().setBscInformation(bscInformation).build();
        Logger.trace(TAG, "Get stop list item for smart card: " + smartCard.toString());

        SmartCardStopListChecker smartCardStopListChecker = new SmartCardStopListChecker(
                Dagger.appComponent().smartCardStopListItemRepository(),
                smartCardCancellationReasonRepository);
        Pair<SmartCardStopListItem, String> stopItemResult = smartCardStopListChecker.findSmartCardStopListItem(bscInformation.getSmartCardTypeBsc(),
                bscInformation.getOuterNumberString(),
                bscInformation.getCrustalSerialNumberString(),
                forControl ? EnumSet.of(StopCriteriaType.READ_AND_WRITE) : EnumSet.of(StopCriteriaType.READ_AND_WRITE, StopCriteriaType.WRITE),
                nsiVersionManager.getCurrentNsiVersionId());
        Logger.trace(TAG, "Smart card: " + String.valueOf(smartCard)
                + "; stop list item: " + String.valueOf(stopItemResult));
        return stopItemResult;
    }
}

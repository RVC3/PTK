package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo;


import java.util.Date;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
interface CardInfoView extends MvpView {

    void setTicketStorageType(TicketStorageType ticketStorageType);

    void setCardStatus(CardStatus cardStatus);

    void setInStopListReasonVisible(boolean visible);

    void setInStopListReason(String inStopListReason);

    void setValidToVisible(boolean visible);

    void setValidToDate(Date date);

    void setExemptionInfoVisible(boolean visible);

    void setInfoVisiblePassageMark(boolean visible);

    void setExemptionCode(int exemptionCode);

    void setExemptionGroupName(String groupName);

    void setExemptionPercentageValue(int percentageValue);

    void setExemptionPercentageVisible(boolean visible);

    void setFio(String firstName, String secondName, String lastName);

    void setCardNumber(String cardNumber);

    void setLastPassageTime(String lastPassageTime);

    void setPassageStation(String station, PassageStationType type);

    void setProhodValid(int colorRes, int stringRes);

    void setWalletUnitsLeft(String walletUnits);

    void setTroykaPoezdkiLeft(String poiezdki);

    void showOutComeStation(String outStation);

    void showValidityDateTime(String dateTimeStr);

    void showNameTypeTicket(String nameTypeTicket);

    void setResOpeningClosureTrip(int resOpeningTrip);

    void setVisibleOpeningClosureTrip(boolean visibleOpenness);

    enum CardStatus {
        VALID,
        IN_STOP_LIST,
        HAS_EXPIRED
    }

    enum PassageStationType {
        UNKNOWN,
        STATION,
        PTK
    }

    enum State {
        SEARCH_CARD,
        READ_CARD,
        PROCESSING_DATA,
        SEARCH_CARD_ERROR,
        UNKNOWN_ERROR
    }

    void setTimerValue(int value);

    void setState(State state);
}

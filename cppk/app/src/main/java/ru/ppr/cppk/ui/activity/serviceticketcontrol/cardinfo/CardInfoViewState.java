package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;
import ru.ppr.nsi.entity.TicketStorageType;


/**
 * @author Aleksandr Brazhkin
 */
class CardInfoViewState extends BaseMvpViewState<CardInfoView> implements CardInfoView {

    private TicketStorageType ticketStorageType = null;
    private CardStatus cardStatus = CardStatus.VALID;
    private boolean inStopListReasonVisible;
    private String inStopListReason;
    private boolean validToVisible;
    private Date validToDate;
    private boolean exemptionInfoVisible;
    private int exemptionCode;
    private String exemptionGroupName;
    private int exemptionPercentageValue;
    private boolean exemptionPercentageVisible;
    private String cardNumber;
    private String firstName;
    private String secondName;
    private String lastName;
    private String lastPassageTime;
    private String passageStation;
    private int colorProhodRes;
    private int textProhodRes;
    private String walletUnitsLeft;
    private String troykaPoezdkiLeft;
    private String dateTimeStr;
    private String outComeStation;
    private boolean isVisiblePassageMarkView;
    private int resOpeningClosureTrip;
    private boolean visibleOpeningClosureTrip;
    private int valueTimer;
    private State state;
    private String nameTypeTicket;
    private PassageStationType passageStationType;

    @Inject
    CardInfoViewState(){

    }

    @Override
    protected void onViewAttached(CardInfoView view) {
        view.setTicketStorageType(ticketStorageType);
        view.setCardStatus(cardStatus);
        view.setInStopListReasonVisible(inStopListReasonVisible);
        view.setInStopListReason(inStopListReason);
        view.setValidToVisible(validToVisible);
        view.setValidToDate(validToDate);
        view.setExemptionInfoVisible(exemptionInfoVisible);
        view.setExemptionCode(exemptionCode);
        view.setExemptionGroupName(exemptionGroupName);
        view.setExemptionPercentageValue(exemptionPercentageValue);
        view.setExemptionPercentageVisible(exemptionPercentageVisible);
        view.setExemptionInfoVisible(exemptionPercentageVisible);
        view.setCardNumber(cardNumber);
        view.setProhodValid(colorProhodRes, textProhodRes);
        view.setWalletUnitsLeft(walletUnitsLeft);
        view.setTroykaPoezdkiLeft(troykaPoezdkiLeft);
        view.setInfoVisiblePassageMark(isVisiblePassageMarkView);
        view.showNameTypeTicket(nameTypeTicket);
        view.showValidityDateTime(dateTimeStr);
        view.setFio(firstName, secondName, lastName);
        view.setLastPassageTime(lastPassageTime);
        view.setPassageStation(passageStation, passageStationType);
        view.showOutComeStation(outComeStation);
        view.setResOpeningClosureTrip(resOpeningClosureTrip);
        view.setVisibleOpeningClosureTrip(visibleOpeningClosureTrip);
        view.setTimerValue(valueTimer);
        view.setState(state);
    }

    @Override
    protected void onViewDetached(CardInfoView view) {

    }

    @Override
    public void setTicketStorageType(TicketStorageType ticketStorageType) {
        this.ticketStorageType = ticketStorageType;
        forEachView(view -> view.setTicketStorageType(this.ticketStorageType));
    }

    @Override
    public void setCardStatus(CardStatus cardStatus) {
        this.cardStatus = cardStatus;
        forEachView(view -> view.setCardStatus(this.cardStatus));
    }

    @Override
    public void setInStopListReasonVisible(boolean visible) {
        this.inStopListReasonVisible = visible;
        forEachView(view -> view.setInStopListReasonVisible(this.inStopListReasonVisible));
    }

    @Override
    public void setInStopListReason(String inStopListReason) {
        this.inStopListReason = inStopListReason;
        forEachView(view -> view.setInStopListReason(this.inStopListReason));
    }

    @Override
    public void setValidToVisible(boolean visible) {
        this.validToVisible = visible;
        forEachView(view -> view.setValidToVisible(this.validToVisible));
    }

    @Override
    public void setValidToDate(Date date) {
        this.validToDate = date;
        forEachView(view -> view.setValidToDate(this.validToDate));
    }

    @Override
    public void setExemptionInfoVisible(boolean visible) {
        this.exemptionInfoVisible = visible;
        forEachView(view -> view.setExemptionInfoVisible(this.exemptionInfoVisible));
    }

    @Override
    public void setInfoVisiblePassageMark(boolean visible) {
        this.isVisiblePassageMarkView = visible;
        forEachView(view -> view.setInfoVisiblePassageMark(this.isVisiblePassageMarkView));
    }

    @Override
    public void setExemptionCode(int exemptionCode) {
        this.exemptionCode = exemptionCode;
        forEachView(view -> view.setExemptionCode(this.exemptionCode));
    }

    @Override
    public void setExemptionGroupName(String groupName) {
        this.exemptionGroupName = groupName;
        forEachView(view -> view.setExemptionGroupName(this.exemptionGroupName));
    }

    @Override
    public void setExemptionPercentageValue(int percentageValue) {
        this.exemptionPercentageValue = percentageValue;
        forEachView(view -> view.setExemptionPercentageValue(this.exemptionPercentageValue));
    }

    @Override
    public void setExemptionPercentageVisible(boolean visible) {
        this.exemptionPercentageVisible = visible;
        forEachView(view -> view.setExemptionPercentageVisible(this.exemptionPercentageVisible));
    }

    @Override
    public void setFio(String firstName, String secondName, String lastName) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.lastName = lastName;
        forEachView(view -> view.setFio(this.firstName, this.secondName, this.lastName));
    }

    @Override
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        forEachView(view -> view.setCardNumber(this.cardNumber));
    }

    @Override
    public void setLastPassageTime(String lastPassageTime) {
        this.lastPassageTime = lastPassageTime;
        forEachView(view -> view.setLastPassageTime(this.lastPassageTime));
    }

    @Override
    public void setPassageStation(String station, PassageStationType type) {
        this.passageStation = station;
        this.passageStationType = type;
        forEachView(view -> view.setPassageStation(this.passageStation, this.passageStationType));
    }

    @Override
    public void setProhodValid(int color, int text) {
        this.colorProhodRes = color;
        this.textProhodRes = text;
        forEachView(view -> view.setProhodValid(this.colorProhodRes, this.textProhodRes));
    }

    @Override
    public void setTroykaPoezdkiLeft(String troykaPoezdkiLeft) {
        this.troykaPoezdkiLeft = troykaPoezdkiLeft;
        forEachView(view -> view.setTroykaPoezdkiLeft(troykaPoezdkiLeft));
    }

    @Override
    public void showOutComeStation(String outComeStation) {
        this.outComeStation = outComeStation;
        forEachView(view -> view.showOutComeStation(outComeStation));
    }

    @Override
    public void showValidityDateTime(String dateTimeStr) {
        this.dateTimeStr = dateTimeStr;
        forEachView(view -> view.showValidityDateTime(troykaPoezdkiLeft));
    }

    @Override
    public void showNameTypeTicket(String nameTypeTicket) {
        this.nameTypeTicket = nameTypeTicket;
        forEachView(view -> view.showNameTypeTicket(nameTypeTicket));
    }

    @Override
    public void setResOpeningClosureTrip(int res) {
        this.resOpeningClosureTrip = res;
        forEachView(view -> view.setResOpeningClosureTrip(res));
    }

    @Override
    public void setVisibleOpeningClosureTrip(boolean visible) {
        this.visibleOpeningClosureTrip = visible;
        forEachView(view -> view.setVisibleOpeningClosureTrip(visible));
    }

    @Override
    public void setTimerValue(int valueTimer) {
        this.valueTimer = valueTimer;
        forEachView(view -> view.setTimerValue(valueTimer));
    }

    @Override
    public void setState(State state) {
        this.state = state;
        forEachView(view -> view.setState(state));
    }

    @Override
    public void setWalletUnitsLeft(String walletUnits) {
        this.walletUnitsLeft = walletUnits;
        forEachView(view -> view.setWalletUnitsLeft(walletUnitsLeft));
    }
}

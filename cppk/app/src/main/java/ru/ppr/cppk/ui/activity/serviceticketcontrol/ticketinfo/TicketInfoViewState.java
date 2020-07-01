package ru.ppr.cppk.ui.activity.serviceticketcontrol.ticketinfo;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;


/**
 * @author Aleksandr Brazhkin
 */
class TicketInfoViewState extends BaseMvpViewState<TicketInfoView> implements TicketInfoView {

    private List<ServiceZoneInfo> serviceZones = Collections.emptyList();
    private Date startDate;
    private Date endDate;
    private boolean checkDocumentsLabelVisible;
    private boolean valid;
    private boolean travelAllowed;
    private boolean validFromError;
    private boolean validToError;
    private boolean outOfAreaBtnVisible;
    private boolean noDocumentsBtnVisible;
    private boolean saleNewPdBtnVisible;
    private ValidityErrorDesc validityErrorDesc;
    private DataErrorDesc dataErrorDesc;
    private State state = State.DATA;

    @Inject
    TicketInfoViewState(){

    }

    @Override
    protected void onViewAttached(TicketInfoView view) {
        view.setState(state);
        view.setServiceZones(serviceZones);
        view.setValidityTime(this.startDate, this.endDate);
        view.setCheckDocumentsLabelVisible(checkDocumentsLabelVisible);
        view.setValid(valid);
        view.setTravelAllowed(travelAllowed);
        view.setValidFromError(validFromError);
        view.setValidToError(validToError);
        view.setOutOfAreaBtnVisible(outOfAreaBtnVisible);
        view.setNoDocumentsBtnVisible(noDocumentsBtnVisible);
        view.setSaleNewPdBtnVisible(saleNewPdBtnVisible);
        view.setValidityErrorDesc(validityErrorDesc);
        view.setDataErrorDesc(dataErrorDesc);
    }

    @Override
    protected void onViewDetached(TicketInfoView view) {

    }

    @Override
    public void setState(State state) {
        this.state = state;
        forEachView(view -> view.setState(this.state));
    }

    @Override
    public void setServiceZones(List<ServiceZoneInfo> serviceZones) {
        this.serviceZones = serviceZones;
        forEachView(view -> view.setServiceZones(this.serviceZones));
    }

    @Override
    public void setValidityTime(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        forEachView(view -> view.setValidityTime(this.startDate, this.endDate));
    }

    @Override
    public void setValidFromError(boolean error) {
        this.validFromError = error;
        forEachView(view -> view.setValidFromError(this.validFromError));
    }

    @Override
    public void setValidToError(boolean error) {
        this.validToError = error;
        forEachView(view -> view.setValidToError(this.validToError));
    }

    @Override
    public void setCheckDocumentsLabelVisible(boolean visible) {
        this.checkDocumentsLabelVisible = visible;
        forEachView(view -> view.setCheckDocumentsLabelVisible(this.checkDocumentsLabelVisible));
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
        forEachView(view -> view.setValid(this.valid));
    }

    @Override
    public void setTravelAllowed(boolean allowed) {
        this.travelAllowed = allowed;
        forEachView(view -> view.setTravelAllowed(this.travelAllowed));
    }

    @Override
    public void setOutOfAreaBtnVisible(boolean visible) {
        this.outOfAreaBtnVisible = visible;
        forEachView(view -> view.setOutOfAreaBtnVisible(this.outOfAreaBtnVisible));
    }

    @Override
    public void setNoDocumentsBtnVisible(boolean visible) {
        this.noDocumentsBtnVisible = visible;
        forEachView(view -> view.setNoDocumentsBtnVisible(this.noDocumentsBtnVisible));
    }

    @Override
    public void setSaleNewPdBtnVisible(boolean visible) {
        this.saleNewPdBtnVisible = visible;
        forEachView(view -> view.setSaleNewPdBtnVisible(this.saleNewPdBtnVisible));
    }

    @Override
    public void setValidityErrorDesc(ValidityErrorDesc validityErrorDesc) {
        this.validityErrorDesc = validityErrorDesc;
        forEachView(view -> view.setValidityErrorDesc(this.validityErrorDesc));
    }

    @Override
    public void setDataErrorDesc(DataErrorDesc dataErrorDesc) {
        this.dataErrorDesc = dataErrorDesc;
        forEachView(view -> view.setDataErrorDesc(this.dataErrorDesc));
    }

    @Override
    public void showSaleNewPdConfirmDialog() {
        forEachView(TicketInfoView::showSaleNewPdConfirmDialog);
    }
}

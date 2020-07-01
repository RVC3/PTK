package ru.ppr.cppk.ui.activity.serviceticketcontrol;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardDataStorage;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.interactor.CardValidityChecker;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.StopCriteriaType;


/**
 * @author Aleksandr Brazhkin
 */
class ServiceTicketControlPresenter extends BaseMvpViewStatePresenter<ServiceTicketControlView, ServiceTicketControlViewState> {

    private static final String TAG = Logger.makeLogTag(ServiceTicketControlPresenter.class);

    private final CardValidityChecker cardValidityChecker;
    private final ServiceTicketControlCardDataStorage serviceTicketControlCardDataStorage;
    private final NsiVersionManager nsiVersionManager;
    private final PermissionChecker permissionChecker;
    private Navigator navigator;

    @Inject
    ServiceTicketControlPresenter(ServiceTicketControlViewState viewState,
                                  CardValidityChecker cardValidityChecker,
                                  ServiceTicketControlCardDataStorage serviceTicketControlCardDataStorage,
                                  NsiVersionManager nsiVersionManager,
                                  PermissionChecker permissionChecker) {
        super(viewState);
        this.cardValidityChecker = cardValidityChecker;
        this.serviceTicketControlCardDataStorage = serviceTicketControlCardDataStorage;
        this.nsiVersionManager = nsiVersionManager;
        this.permissionChecker = permissionChecker;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    protected void onInitialize2() {
        Logger.trace(TAG, "onInitialize");

        ServiceTicketControlCardData serviceTicketControlCardData = serviceTicketControlCardDataStorage.getLastCardData();
        if (serviceTicketControlCardData == null) {
            Logger.trace(TAG, "Information not found");
            return;
        }

        ServiceTicketControlEvent serviceTicketControlEvent = serviceTicketControlCardData.getServiceTicketControlEvent();
        Logger.trace(TAG, "ServiceTicketControlEventId = " + serviceTicketControlEvent.getId());

        CardValidityChecker.Result validityCheckResult = cardValidityChecker.check(
                serviceTicketControlCardData.getCardInformation(),
                StopCriteriaType.SERVICE_TICKET_USAGE,
                true,
                nsiVersionManager.getCurrentNsiVersionId());

        Logger.trace(TAG, "CheckResult = " + validityCheckResult.toString());

        if (!validityCheckResult.isValid()) {
            navigator.navigateToCardInfo();
        }
    }

    void onRfidBtnClicked() {
        Logger.trace(TAG, "onRfidBtnClicked");

        if (!permissionChecker.checkPermission(PermissionDvc.ReedBskNfc)) {
            Logger.warning(TAG, "Недоступен контроль БСК для текущей роли");
            return;
        }

        navigator.navigateToControlReadBsc();
    }

    void onBarcodeBtnClicked() {
        Logger.trace(TAG, "onBarcodeBtnClicked");

        if (!permissionChecker.checkPermission(PermissionDvc.ReedPdBarcode)) {
            Logger.warning(TAG, "Недоступен контроль ШК для текущей роли");
            return;
        }

        navigator.navigateToControlReadBarcode();
    }

    interface Navigator {
        void navigateToCardInfo();

        void navigateToControlReadBsc();

        void navigateToControlReadBarcode();
    }
}

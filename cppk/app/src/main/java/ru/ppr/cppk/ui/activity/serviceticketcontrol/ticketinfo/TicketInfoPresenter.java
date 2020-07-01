package ru.ppr.cppk.ui.activity.serviceticketcontrol.ticketinfo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT1.CoverageAreaT1;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT2.CoverageAreaT2;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT3.CoverageAreaT3;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT4.CoverageAreaT4;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.local.repository.ServiceTicketControlEventRepository;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardDataStorage;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.localdb.model.ServiceTicketPassageResult;
import ru.ppr.cppk.localdb.model.ServiceZoneType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.logic.servicedatacontrol.ValidityChecker;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Direction;
import ru.ppr.nsi.entity.ProductionSection;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.repository.DirectionRepository;
import ru.ppr.nsi.repository.ProductionSectionRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.security.entity.PermissionDvc;
import rx.Completable;


/**
 * @author Aleksandr Brazhkin
 */
class TicketInfoPresenter extends BaseMvpViewStatePresenter<TicketInfoView, TicketInfoViewState> {

    private static final String TAG = Logger.makeLogTag(TicketInfoPresenter.class);

    private final UiThread uiThread;
    private final ServiceTicketControlCardDataStorage serviceTicketControlCardDataStorage;
    private final StationRepository stationRepository;
    private final ProductionSectionRepository productionSectionRepository;
    private final DirectionRepository directionRepository;
    private final ServiceTicketControlEventRepository serviceTicketControlEventRepository;
    private final NsiVersionManager nsiVersionManager;
    private final PermissionChecker permissionChecker;
    private final PrivateSettings privateSettings;
    private ServiceTicketControlEvent serviceTicketControlEvent;
    private Navigator navigator;

    @Inject
    TicketInfoPresenter(TicketInfoViewState viewState,
                        UiThread uiThread,
                        ServiceTicketControlCardDataStorage serviceTicketControlCardDataStorage,
                        StationRepository stationRepository,
                        ProductionSectionRepository productionSectionRepository,
                        DirectionRepository directionRepository,
                        ServiceTicketControlEventRepository serviceTicketControlEventRepository,
                        NsiVersionManager nsiVersionManager,
                        PermissionChecker permissionChecker,
                        PrivateSettings privateSettings) {
        super(viewState);
        this.uiThread = uiThread;
        this.serviceTicketControlCardDataStorage = serviceTicketControlCardDataStorage;
        this.stationRepository = stationRepository;
        this.productionSectionRepository = productionSectionRepository;
        this.directionRepository = directionRepository;
        this.serviceTicketControlEventRepository = serviceTicketControlEventRepository;
        this.nsiVersionManager = nsiVersionManager;
        this.permissionChecker = permissionChecker;
        this.privateSettings = privateSettings;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    protected void onInitialize2() {
        Logger.trace(TAG, "onInitialize");
        Completable
                .fromAction(() -> {
                    uiThread.post(() -> view.setState(TicketInfoView.State.VALIDATING));

                    ServiceTicketControlCardData serviceTicketControlCardData = serviceTicketControlCardDataStorage.getLastCardData();

                    if (serviceTicketControlCardData == null) {
                        Logger.trace(TAG, "Information not found");
                        uiThread.post(() -> {
                            view.setState(TicketInfoView.State.ERROR);
                            view.setDataErrorDesc(TicketInfoView.DataErrorDesc.NO_DATA);
                        });
                        return;
                    }

                    ValidityChecker.Result checkResult = serviceTicketControlCardData.getCheckResult();

                    if (!checkResult.isDeviceIdValid()) {
                        Logger.trace(TAG, "Invalid device id");
                        uiThread.post(() -> {
                            view.setState(TicketInfoView.State.ERROR);
                            view.setDataErrorDesc(TicketInfoView.DataErrorDesc.INVALID_DEVICE_ID);
                        });
                        return;
                    }

                    serviceTicketControlEvent = serviceTicketControlCardData.getServiceTicketControlEvent();

                    List<CoverageArea> coverageAreaList = serviceTicketControlCardData.getCoverageAreaList();

                    List<TicketInfoView.ServiceZoneInfo> serviceZones = new ArrayList<>();
                    for (CoverageArea coverageArea : coverageAreaList) {
                        if (coverageArea instanceof CoverageAreaT1) {
                            serviceZones.add(new TicketInfoView.ServiceZoneInfo(ServiceZoneType.Polygone, 0, null));
                        } else if (coverageArea instanceof CoverageAreaT2) {
                            CoverageAreaT2 coverageAreaT2 = (CoverageAreaT2) coverageArea;
                            long stationCode = coverageAreaT2.getStationCode();
                            Station station = stationRepository.load(stationCode, nsiVersionManager.getCurrentNsiVersionId());
                            String stationName = station == null ? null : station.getName();
                            serviceZones.add(new TicketInfoView.ServiceZoneInfo(ServiceZoneType.Station, stationCode, stationName));
                        } else if (coverageArea instanceof CoverageAreaT3) {
                            CoverageAreaT3 coverageAreaT3 = (CoverageAreaT3) coverageArea;
                            long directionCode = coverageAreaT3.getDirectionCode();
                            Direction direction = directionRepository.load(directionCode, nsiVersionManager.getCurrentNsiVersionId());
                            String directionName = direction == null ? null : direction.getName();
                            serviceZones.add(new TicketInfoView.ServiceZoneInfo(ServiceZoneType.Direction, directionCode, directionName));
                        } else if (coverageArea instanceof CoverageAreaT4) {
                            CoverageAreaT4 coverageAreaT4 = (CoverageAreaT4) coverageArea;
                            long productionSectionCode = coverageAreaT4.getProductionSectionCode();
                            ProductionSection productionSection = productionSectionRepository.load(productionSectionCode, nsiVersionManager.getCurrentNsiVersionId());
                            String productionSectionName = productionSection == null ? null : productionSection.getName();
                            serviceZones.add(new TicketInfoView.ServiceZoneInfo(ServiceZoneType.ProductionSection, productionSectionCode, productionSectionName));
                        }
                    }

                    uiThread.post(() -> view.setServiceZones(serviceZones));

                    uiThread.post(() -> {
                        view.setValidityTime(serviceTicketControlEvent.getValidFrom(), serviceTicketControlEvent.getValidTo());
                        view.setCheckDocumentsLabelVisible(serviceTicketControlEvent.isRequireCheckDocument());
                    });

                    boolean outOfAreaBtnVisible = checkResult.isValid() && !checkResult.isForAllAreas();
                    uiThread.post(() -> {
                        view.setTravelAllowed(checkResult.isTravelAllowed());
                        view.setValidFromError(!checkResult.isStartDateValid());
                        view.setValidToError(!checkResult.isEndDateValid());
                        view.setValid(checkResult.isValid());

                        view.setNoDocumentsBtnVisible(checkResult.isValid() && serviceTicketControlEvent.isRequireCheckDocument());
                        view.setOutOfAreaBtnVisible(outOfAreaBtnVisible);
                        view.setSaleNewPdBtnVisible(!checkResult.isValid());

                        if (checkResult.getCheckSignResultState() == CheckSignResultState.KEY_REVOKED) {
                            view.setValidityErrorDesc(TicketInfoView.ValidityErrorDesc.REVOKED_EDS_KEY);
                        } else if (checkResult.getCheckSignResultState() == CheckSignResultState.INVALID) {
                            view.setValidityErrorDesc(TicketInfoView.ValidityErrorDesc.INVALID_EDS_KEY);
                        }
                    });

                    Logger.trace(TAG, "Displaying data");

                    uiThread.post(() -> view.setState(TicketInfoView.State.DATA));
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onOutOfAreaBtnClicked() {
        Logger.trace(TAG, "onOutOfAreaBtnClicked");
        serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.NO_VALID_ZONE);
        serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
        serviceTicketControlEventRepository.update(serviceTicketControlEvent);
        if (privateSettings.isSaleEnabled() && permissionChecker.checkPermission(PermissionDvc.SalePd)) {
            view.showSaleNewPdConfirmDialog();
        } else {
            navigator.navigateToPreviousScreen();
        }
    }

    void onNoDocumentsBtnClicked() {
        Logger.trace(TAG, "onNoDocumentsBtnClicked");
        serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.DOCUMENT_NOT_PRESENT);
        serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
        serviceTicketControlEventRepository.update(serviceTicketControlEvent);
        if (privateSettings.isSaleEnabled() && permissionChecker.checkPermission(PermissionDvc.SalePd)) {
            view.showSaleNewPdConfirmDialog();
        } else {
            navigator.navigateToPreviousScreen();
        }
    }

    void onSaleNewPdBtnClicked() {
        Logger.trace(TAG, "onSaleNewPdBtnClicked");
        navigateToSaleNewPd();
    }

    void onSaleNewPdDialogYesBtnClicked() {
        Logger.trace(TAG, "onSaleNewPdDialogYesBtnClicked");
        navigateToSaleNewPd();
    }

    void onSaleNewPdDialogNoBtnClicked() {
        Logger.trace(TAG, "onSaleNewPdDialogNoBtnClicked");
        navigator.navigateToPreviousScreen();
    }

    private void navigateToSaleNewPd() {
        PdSaleParams pdSaleParams = new PdSaleParams();
        pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
        pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
        navigator.navigateToSaleNewPd(pdSaleParams);
    }

    private void updateEventOnDestroy() {
        if (serviceTicketControlEvent == null) {
            // Нет информации о событии
            return;
        }
        if (serviceTicketControlEvent.getStatus() != ServiceTicketControlEvent.Status.COMPLETED) {
            Logger.trace(TAG, "Completing event on destroy");
            serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.SUCCESS_PASSAGE);
            serviceTicketControlEvent.setPassageSign(true);
            serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
            serviceTicketControlEventRepository.update(serviceTicketControlEvent);
        }
    }

    @Override
    public void destroy() {
        updateEventOnDestroy();
        super.destroy();
    }

    interface Navigator {
        void navigateToSaleNewPd(PdSaleParams pdSaleParams);

        void navigateToPreviousScreen();
    }
}

package ru.ppr.chit.ui.activity.root;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.backup.DbsBackupCreator;
import ru.ppr.chit.backup.DbsBackupRestorer;
import ru.ppr.chit.backup.FullBackupCreator;
import ru.ppr.chit.backup.FullBackupRestorer;
import ru.ppr.chit.backup.LogsBackupCreator;
import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.domain.model.RfidType;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class RootPresenter extends BaseMvpViewStatePresenter<RootView, RootViewState> {

    private static final String TAG = Logger.makeLogTag(RootPresenter.class);

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final UiThread uiThread;
    private final AppPropertiesRepository appPropertiesRepository;
    private final FullBackupCreator fullBackupCreator;
    private final LogsBackupCreator logsBackupCreator;
    private final DbsBackupCreator dbsBackupCreator;
    private final FullBackupRestorer fullBackupRestorer;
    private final DbsBackupRestorer dbsBackupRestorer;
    //endregion
    //region Other
    private Navigator navigator;
    private boolean backToSplash;
    private List<EdsType> edsTypeList;
    private List<BarcodeType> barcodeTypeList;
    private List<RfidType> rfidTypeList;
    private int edsTypeListSelection;
    private int barcodeTypeListSelection;
    private int rfidTypeListSelection;
    //endregion

    @Inject
    RootPresenter(RootViewState rootViewState,
                  UiThread uiThread,
                  AppPropertiesRepository appPropertiesRepository,
                  LogsBackupCreator logsBackupCreator,
                  DbsBackupCreator dbsBackupCreator,
                  FullBackupCreator fullBackupCreator,
                  FullBackupRestorer fullBackupRestorer,
                  DbsBackupRestorer dbsBackupRestorer) {
        super(rootViewState);
        this.uiThread = uiThread;
        this.appPropertiesRepository = appPropertiesRepository;
        this.logsBackupCreator = logsBackupCreator;
        this.dbsBackupCreator = dbsBackupCreator;
        this.fullBackupCreator = fullBackupCreator;
        this.fullBackupRestorer = fullBackupRestorer;
        this.dbsBackupRestorer = dbsBackupRestorer;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        edsTypeList = new ArrayList<>(EnumSet.allOf(EdsType.class));
        barcodeTypeList = new ArrayList<>(EnumSet.allOf(BarcodeType.class));
        rfidTypeList = new ArrayList<>(EnumSet.allOf(RfidType.class));

        AppProperties appProperties = appPropertiesRepository.load();
        edsTypeListSelection = edsTypeList.indexOf(appProperties.getEdsType());
        barcodeTypeListSelection = barcodeTypeList.indexOf(appProperties.getBarcodeType());
        rfidTypeListSelection = rfidTypeList.indexOf(appProperties.getRfidType());

        uiThread.post(() -> {
            view.setEdsTypeList(edsTypeList);
            view.setEdsTypeListSelection(edsTypeListSelection);
            view.setBarcodeTypeList(barcodeTypeList);
            view.setBarcodeTypeListSelection(barcodeTypeListSelection);
            view.setRfidTypeList(rfidTypeList);
            view.setRfidTypeListSelection(rfidTypeListSelection);
        });
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void setBackToSplash(boolean backToSplash) {
        this.backToSplash = backToSplash;
    }

    void onBackPressed() {
        Logger.trace(TAG, "onBackPressed");
        if (backToSplash) {
            navigator.navigateToSplash();
        } else {
            navigator.navigateBack();
        }
    }

    void onEdsTypeSelected(int position) {
        Logger.trace(TAG, "onEdsTypeSelected: " +
                "last=" + edsTypeList.get(edsTypeListSelection) + " | " +
                "new=" + edsTypeList.get(position));
        if (position != edsTypeListSelection) {
            Logger.trace(TAG, "onEdsTypeSelected: trying change");
            edsTypeListSelection = position;
            AppProperties appProperties = appPropertiesRepository.load();
            appProperties.setEdsType(edsTypeList.get(position));
            appPropertiesRepository.merge(appProperties);
        }
    }

    void onBarcodeTypeSelected(int position) {
        Logger.trace(TAG, "onBarcodeTypeSelected: " +
                "last=" + barcodeTypeList.get(barcodeTypeListSelection) + " | " +
                "new=" + barcodeTypeList.get(position));
        if (position != barcodeTypeListSelection) {
            Logger.trace(TAG, "onBarcodeTypeSelected: trying change");
            barcodeTypeListSelection = position;
            AppProperties appProperties = appPropertiesRepository.load();
            appProperties.setBarcodeType(barcodeTypeList.get(position));
            appPropertiesRepository.merge(appProperties);
        }
    }

    void onRfidTypeSelected(int position) {
        Logger.trace(TAG, "onRfidTypeSelected: " +
                "last=" + rfidTypeList.get(rfidTypeListSelection) + " | " +
                "new=" + rfidTypeList.get(position));
        if (position != rfidTypeListSelection) {
            Logger.trace(TAG, "onRfidTypeSelected: trying change");
            rfidTypeListSelection = position;
            AppProperties appProperties = appPropertiesRepository.load();
            appProperties.setRfidType(rfidTypeList.get(position));
            appPropertiesRepository.merge(appProperties);
        }
    }

    void onCreateFullBtnClicked() {
        Logger.trace(TAG, "onCreateFullBtnClicked");
        view.setCreateFullBackupProgressVisible(true);
        fullBackupCreator.rxStart()
                .subscribeOn(AppSchedulers.background())
                .subscribe(() -> uiThread.post(() -> view.setCreateFullBackupProgressVisible(false)),
                        error -> {
                            Logger.error(TAG, error);
                            uiThread.post(() -> view.setCreateFullBackupProgressVisible(false));
                        });
    }

    void onCreateLogsBtnClicked() {
        Logger.trace(TAG, "onCreateLogsBtnClicked");
        view.setCreateLogsBackupProgressVisible(true);
        logsBackupCreator.rxStart()
                .subscribeOn(AppSchedulers.background())
                .subscribe(() -> uiThread.post(() -> view.setCreateLogsBackupProgressVisible(false)),
                        error -> {
                            Logger.error(TAG, error);
                            uiThread.post(() -> view.setCreateLogsBackupProgressVisible(false));
                        });
    }

    void onCreateDbsBtnClicked() {
        Logger.trace(TAG, "onCreateDbsBtnClicked");
        view.setCreateDbsBackupProgressVisible(true);
        dbsBackupCreator.rxStart()
                .subscribeOn(AppSchedulers.background())
                .subscribe(() -> uiThread.post(() -> view.setCreateDbsBackupProgressVisible(false)),
                        error -> {
                            Logger.error(TAG, error);
                            uiThread.post(() -> view.setCreateDbsBackupProgressVisible(false));
                        });
    }

    void onRestoreFullBtnClicked() {
        Logger.trace(TAG, "onRestoreFullBtnClicked");
        view.setRestoreFullBackupProgressVisible(true);
        fullBackupRestorer.rxStart()
                .subscribeOn(AppSchedulers.background())
                .subscribe(() -> uiThread.post(() -> view.setRestoreFullBackupProgressVisible(false)),
                        error -> {
                            Logger.error(TAG, error);
                            uiThread.post(() -> view.setRestoreFullBackupProgressVisible(false));
                        });
    }

    void onRestoreDbsBtnClicked() {
        Logger.trace(TAG, "onRestoreDbsBtnClicked");
        view.setRestoreDbsBackupProgressVisible(true);
        dbsBackupRestorer.rxStart()
                .subscribeOn(AppSchedulers.background())
                .subscribe(() -> uiThread.post(() -> view.setRestoreDbsBackupProgressVisible(false)),
                        error -> {
                            Logger.error(TAG, error);
                            uiThread.post(() -> view.setRestoreDbsBackupProgressVisible(false));
                        });
    }

    interface Navigator {

        void navigateBack();

        void navigateToSplash();

    }

}

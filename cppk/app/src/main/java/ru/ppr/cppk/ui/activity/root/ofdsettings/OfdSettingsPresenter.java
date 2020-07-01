package ru.ppr.cppk.ui.activity.root.ofdsettings;

import java.util.regex.Pattern;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.ikkm.model.OfdSettings;
import ru.ppr.logger.Logger;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Grigoriy Kashka
 */
class OfdSettingsPresenter extends BaseMvpViewStatePresenter<OfdSettingsView, OfdSettingsViewState> {

    private static final String TAG = Logger.makeLogTag(OfdSettingsPresenter.class);

    private static final Pattern IP = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );
    private static final Pattern PORT = Pattern.compile("([0-9]{1,5})");
    private static final Pattern TIMEOUT = Pattern.compile("([0-9]{1,4})");

    private final PrinterManager printerManager;

    private boolean mInitialized = false;

    @Inject
    OfdSettingsPresenter(OfdSettingsViewState ofdSettingsViewState, PrinterManager printerManager) {
        super(ofdSettingsViewState);
        this.printerManager = printerManager;
    }

    void initialize() {
        if (!mInitialized) {
            mInitialized = true;
        }
    }

    /**
     * Обработчик кнопки OK
     */
    void onReadBtnClick() {
        view.showProgress();
        view.setState(OfdSettingsView.State.DEFAULT);
        printerManager.getOperationFactory().getOfdSettingsOperation().call()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    view.setIp(result.getIp());
                    view.setPort(result.getPort());
                    view.setTimeout(result.getTimeout());
                })
                .subscribeOn(SchedulersCPPK.printer())
                .subscribe(result -> {
                    view.setState(OfdSettingsView.State.SUCCESS);
                    view.hideProgress();
                }, throwable -> {
                    view.setState(OfdSettingsView.State.ERROR_GET_DATA);
                    view.hideProgress();
                });
    }

    /**
     * Обработчик кнопки Write
     */
    void onWriteBtnClick(String ip, String port, String timeout) {
        if (!validateIp(ip)) {
            view.setState(OfdSettingsView.State.ERROR_IP);
        } else if (!validatePort(port)) {
            view.setState(OfdSettingsView.State.ERROR_PORT);
        } else if (!validateTimeout(timeout)) {
            view.setState(OfdSettingsView.State.ERROR_TIMEOUT);
        } else {
            setDataToPrinter(ip, Integer.valueOf(port), Integer.valueOf(timeout));
        }
    }

    private void setDataToPrinter(String ip, int port, int timeout) {
        view.setState(OfdSettingsView.State.DEFAULT);
        view.showProgress();
        OfdSettings ofdSettings = new OfdSettings();
        ofdSettings.setIp(ip);
        ofdSettings.setPort(port);
        ofdSettings.setTimeout(timeout);
        printerManager.getOperationFactory().setOfdSettingsOperation(ofdSettings).call()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(SchedulersCPPK.printer())
                .subscribe(result -> {
                    view.setState(OfdSettingsView.State.SUCCESS);
                    view.hideProgress();
                }, throwable -> {
                    Logger.error(TAG, throwable);
                    view.setState(OfdSettingsView.State.ERROR_GET_DATA);
                    view.hideProgress();
                });
    }

    private boolean validateIp(String ip) {
        return IP.matcher(ip).matches();
    }

    private boolean validatePort(String port) {
        return PORT.matcher(port).matches();
    }

    private boolean validateTimeout(String timeout) {
        return TIMEOUT.matcher(timeout).matches();
    }


}

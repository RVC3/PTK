package ru.ppr.cppk.ui.activity.readpdfortransfer;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;


/**
 * @author Aleksandr Brazhkin
 */
class ReadPdForTransferPresenter extends BaseMvpViewStatePresenter<ReadPdForTransferView, ReadPdForTransferViewState> {

    private static final String TAG = Logger.makeLogTag(ReadPdForTransferPresenter.class);

    // Common fields start
    private boolean initialized = false;
    // Common fields end
    private Navigator navigator;

    @Inject
    ReadPdForTransferPresenter(ReadPdForTransferViewState readPdForTransferViewState) {
        super(readPdForTransferViewState);
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {

    }

    void onReadBscBtnClicked() {
        Logger.trace(TAG, "Нажали на кнопку Трансфер.БСК");
        navigator.navigateToReadBsc();
    }

    void onReadBarcodeBtnClicked() {
        navigator.navigateToReadBarcode();
    }

    interface Navigator {
        void navigateToReadBsc();

        void navigateToReadBarcode();
    }
}

package ru.ppr.cppk.ui.activity.enterETicketData;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.regex.Pattern;

import ru.ppr.core.ui.mvp.presenter.BaseMvpPresenter;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.logger.Logger;

/**
 * @author Grigoriy Kashka
 */
public class EnterETicketDataPresenter extends BaseMvpPresenter<EnterETicketDataView> {

    private static final String TAG = Logger.makeLogTag(EnterETicketDataPresenter.class);

    private static final Pattern PHONE
            = Pattern.compile(
            "(\\d{10})|(\\+7\\d{10})|(8\\d{10})"
    );

    private boolean mInitialized = false;
    private ETicketDataParams eTicketDataParams;

    private InteractionListener interactionListener;

    public EnterETicketDataPresenter() {

    }

    void initialize(ETicketDataParams eTicketDataParams) {
        if (!mInitialized) {
            mInitialized = true;
            this.eTicketDataParams = eTicketDataParams == null ? new ETicketDataParams() : eTicketDataParams;
            view.setEmailAndPhone(this.eTicketDataParams);
        }
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    /**
     * Обработчик кнопки OK
     */
    void onOkBtnClick(String data) {
        checkData(data);
    }

    /**
     * Обработчик нажатия ГОТОВО на клавиатуре в поле ввода телефона
     */
    void onPhoneDoneClick(String data) {
        checkData(data);
    }

    /**
     * Обработчик кнопки Cancel
     */
    void onCancelBtnClick() {
        interactionListener.returnToPreviousScreen(null);
    }

    private boolean validateEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validatePhone(String phone) {
        return PHONE.matcher(phone).matches();
    }

    private void checkData(String data) {
        eTicketDataParams.setPhone("");
        eTicketDataParams.setEmail("");
        boolean isDataOk = true;
        if (!TextUtils.isEmpty(data)) {
            if (validatePhone(data)) {
                eTicketDataParams.setPhone(data);
            } else if (validateEmail(data)) {
                eTicketDataParams.setEmail(data);
            } else {
                isDataOk = false;
            }
        }

        view.showError(isDataOk);
        if (isDataOk)
            interactionListener.returnToPreviousScreen(eTicketDataParams);

    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void returnToPreviousScreen(ETicketDataParams eTicketDataParams);
    }

}

package ru.ppr.cppk.ui.fragment.exemptionManualInput;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.logic.exemptionChecker.ExemptionCheckResultStringifyer;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class ExemptionManualInputFragment extends LegacyMvpFragment implements ExemptionManualInputView {

    private static final String TAG = Logger.makeLogTag(ExemptionManualInputFragment.class);
    public static final String FRAGMENT_TAG = ExemptionManualInputFragment.class.getSimpleName();

    public static ExemptionManualInputFragment newInstance() {
        return new ExemptionManualInputFragment();
    }

    /**
     * Di
     */
    private final ExemptionManualInputDi di = new ExemptionManualInputDi(di());

    private InteractionListener mInteractionListener;

    //Views
    private EditText codeField;
    private TextView orLabel;
    private Button readFromCardBtn;
    //region Other
    private ExemptionManualInputPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exemption_manual_input, container, false);

        codeField = (EditText) view.findViewById(R.id.code);
        codeField.setOnKeyListener(codeFieldOnKeyListener);
        orLabel = (TextView) view.findViewById(R.id.orLabel);
        readFromCardBtn = (Button) view.findViewById(R.id.readFromCardBtn);
        readFromCardBtn.setOnClickListener(v -> presenter.onReadFromCardClicked());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().postDelayed(() -> {
            InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(codeField, 0);
        }, 200);
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(ExemptionManualInputPresenter::new, ExemptionManualInputPresenter.class);
    }

    public void initialize(SelectExemptionParams selectExemptionParams) {
        presenter.bindInteractionListener(exemptionManualInputInteractionListener);
        presenter.initialize(selectExemptionParams, di.exemptionChecker(), di.nsiVersionManager(), di.ticketTypeRepository(), di.exemptionRepository());
    }

    @Override
    public void setReadFromCardBtnVisible(boolean visible) {
        orLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        readFromCardBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showExemptionNotFoundMessage(int exemptionExpressCode) {
        showExemptionUsageDisabledMessage(getString(R.string.exemption_manual_input_msg_not_found, exemptionExpressCode));
    }

    @Override
    public void showExemptionUsageDisabledMessage(ExemptionUsageDisabledMessage exemptionUsageDisabledMessage) {
        String msg = new ExemptionCheckResultStringifyer(getActivity()).getString(
                exemptionUsageDisabledMessage.checkResult,
                exemptionUsageDisabledMessage.exemptionExpressCode,
                exemptionUsageDisabledMessage.ticketTypeName
        );
        showExemptionUsageDisabledMessage(msg);
    }

    private void showExemptionUsageDisabledMessage(String msg) {
        Fragment existingFragment = getFragmentManager().findFragmentByTag(SimpleDialog.FRAGMENT_TAG);
        SimpleDialog exemptionUsageDialog;
        if (existingFragment == null) {
            exemptionUsageDialog = SimpleDialog.newInstance(null, msg, getString(R.string.exemption_read_from_card_close_dialog_btn), null, LinearLayout.VERTICAL, 0);
            exemptionUsageDialog.setOnDismissListener(dialog -> {
                getView().postDelayed(() -> {
                    InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(codeField, 0);
                }, 200);
            });
            exemptionUsageDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        }
    }

    private View.OnKeyListener codeFieldOnKeyListener = (v, keyCode, event) -> {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
            presenter.onCodeEntered(codeField.getText().toString());
            return true;
        }
        return false;
    };

    private ExemptionManualInputPresenter.InteractionListener exemptionManualInputInteractionListener = new ExemptionManualInputPresenter.InteractionListener() {

        @Override
        public void navigateToReadFromCard() {
            mInteractionListener.navigateToReadFromCard();
        }

        @Override
        public void navigateToEnterSurname(List<ExemptionForEvent> exemptionsForEvent) {
            mInteractionListener.navigateToEnterSurname(exemptionsForEvent);
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void navigateToReadFromCard();

        void navigateToEnterSurname(List<ExemptionForEvent> exemptionsForEvent);
    }
}

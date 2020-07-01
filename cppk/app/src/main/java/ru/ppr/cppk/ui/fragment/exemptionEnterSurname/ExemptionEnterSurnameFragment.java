package ru.ppr.cppk.ui.fragment.exemptionEnterSurname;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.cppk.ui.widget.DateView;
import ru.ppr.cppk.utils.validators.ConcreteInputFilter;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class ExemptionEnterSurnameFragment extends LegacyMvpFragment implements ExemptionEnterSurnameView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(ExemptionEnterSurnameFragment.class);
    public static final String FRAGMENT_TAG = ExemptionEnterSurnameFragment.class.getSimpleName();

    public static ExemptionEnterSurnameFragment newInstance() {
        return new ExemptionEnterSurnameFragment();
    }

    /**
     * Di
     */
    private final ExemptionEnterSurnameDi di = new ExemptionEnterSurnameDi(di());

    private InteractionListener mInteractionListener;

    //Views
    private EditText fieldSurname = null;
    private EditText fieldDocumentNumber = null;
    private DateView fieldIssueDate = null;
    private TextView exemptionCode;
    private TextView exemptionName;
    private TextView exemptionPercentage;
    // Other
    private final DateFormat issueDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    //region Other
    private ExemptionEnterSurnamePresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_exemption_enter_surname, container, false);

        exemptionPercentage = ((TextView) view.findViewById(R.id.percentage));
        exemptionName = ((TextView) view.findViewById(R.id.groupName));
        exemptionCode = ((TextView) view.findViewById(R.id.expressCode));
        Button useExemptionBtn = (Button) view.findViewById(R.id.useExemptionBtn);
        useExemptionBtn.setOnClickListener(v -> presenter.onUseExemptionBtnClicked(fieldSurname.getText().toString(), fieldDocumentNumber.getText().toString(), fieldIssueDate.getDate()));
        fieldSurname = (EditText) view.findViewById(R.id.fieldSurname);
        fieldSurname.setFilters(new InputFilter[]{ConcreteInputFilter. getFilterForLetterDotSpace(), new InputFilter.LengthFilter(50)});
        fieldDocumentNumber = (EditText) view.findViewById(R.id.fieldDocumentNumber);
        fieldIssueDate = (DateView) view.findViewById(R.id.fieldIssueDate);
        fieldIssueDate.setDateFormat(issueDateFormat);
        fieldIssueDate.setOnClickListener(v -> {
            showIssueDatePickerDialog();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().postDelayed(() -> {
            if (fieldDocumentNumber.hasFocus()) {
                InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(fieldDocumentNumber, 0);
            } else if (fieldSurname.hasFocus()) {
                InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(fieldSurname, 0);
            }
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
        presenter = getMvpDelegate().getPresenter(ExemptionEnterSurnamePresenter::new, ExemptionEnterSurnamePresenter.class);
    }

    public void initialize(List<ExemptionForEvent> exemptionsForEvent) {
        presenter.bindInteractionListener(exemptionManualInputInteractionListener);
        presenter.initialize(exemptionsForEvent, di.exemptionGroupRepository(), di.exemptionRepository(), di.fioNormalizer());
    }

    @Override
    public void showEmptyDocumentError(boolean isSnils) {
        hideKeyboard();
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(isSnils ? R.string.add_exemption_manual_msg_error_empty_snils_number : R.string.add_exemption_manual_msg_error_empty_document_number),
                getString(R.string.dialog_close),
                null,
                LinearLayout.VERTICAL,
                0
        );
        simpleDialog.setOnDismissListener(dialogInterface -> showKeyboard(fieldDocumentNumber));
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    @Override
    public void showInvalidSnilsError() {
        hideKeyboard();
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(R.string.add_exemption_manual_msg_error_invalid_snils),
                getString(R.string.dialog_close),
                null,
                LinearLayout.VERTICAL,
                0
        );
        simpleDialog.setOnDismissListener(dialogInterface -> showKeyboard(fieldDocumentNumber));
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    @Override
    public void showInvalidFioError() {
        hideKeyboard();
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(R.string.add_exemption_manual_msg_error_empty_fio),
                getString(R.string.dialog_close),
                null,
                LinearLayout.VERTICAL,
                0
        );
        simpleDialog.setOnDismissListener(dialogInterface -> showKeyboard(fieldSurname));
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    @Override
    public void showEmptyIssueDateError() {
        hideKeyboard();
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(R.string.add_exemption_manual_msg_error_empty_issue_date),
                getString(R.string.dialog_close),
                null,
                LinearLayout.VERTICAL,
                0
        );
        simpleDialog.setOnDismissListener(dialogInterface -> showIssueDatePickerDialog());
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    @Override
    public void setSnilsFieldVisible(boolean visible) {
        if (visible) {
            fieldDocumentNumber.setHint(R.string.exemption_enter_surname_snils_number);
            fieldDocumentNumber.setFilters(new InputFilter[]{ConcreteInputFilter.getFilterForDigit(), new InputFilter.LengthFilter(20)});
            fieldDocumentNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    @Override
    public void setIssueDateFieldVisible(boolean visible) {
        fieldIssueDate.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setDocumentNumberFieldVisible(boolean visible) {
        if (visible) {
            fieldDocumentNumber.setInputType(InputType.TYPE_CLASS_TEXT);
            fieldDocumentNumber.setFilters(new InputFilter[]{ConcreteInputFilter.getFilterForCyrilicLetterAndDigit(), new InputFilter.LengthFilter(20)});
        }
    }

    @Override
    public void setExemptionInfo(ExemptionInfo exemptionInfo) {
        exemptionCode.setText(exemptionInfo == null ? "" : String.valueOf(exemptionInfo.exemptionExpressCode));
        exemptionPercentage.setText(exemptionInfo == null ? "" : getResources().getString(R.string.exemption_enter_surname_percentage, exemptionInfo.percentage));
        exemptionName.setText(exemptionInfo == null ? "" : exemptionInfo.groupName);
    }

    /**
     * Отображает диалог выбора даты выпуска документа, подтверждающего право использования льготы.
     */
    private void showIssueDatePickerDialog() {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view1, year1, monthOfYear, dayOfMonth) -> {
            if (view1.isShown()) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year1, monthOfYear, dayOfMonth, 0, 0, 0);
                fieldIssueDate.setDate(calendar.getTime());
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * Открывает клавиатуру с фокусом на поле ввода
     */
    private void showKeyboard(EditText editText) {
        editText.post(() -> {
            editText.requestFocus();
            InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    /**
     * Скрывает клавиатуру
     */
    private void hideKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public boolean onBackPress() {
        return presenter.onBackPressed();
    }

    private ExemptionEnterSurnamePresenter.InteractionListener exemptionManualInputInteractionListener = new ExemptionEnterSurnamePresenter.InteractionListener() {

        @Override
        public void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionsForEvent) {
            mInteractionListener.onExemptionSelected(exemptionsForEvent);
        }

        @Override
        public void onCancelSelectExemption() {
            mInteractionListener.onCancelSelectExemption();
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionForEvents);

        void onCancelSelectExemption();
    }
}

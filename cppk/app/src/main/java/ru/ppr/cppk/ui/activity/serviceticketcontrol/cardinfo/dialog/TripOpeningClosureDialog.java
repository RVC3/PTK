package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;

import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.systembar.FeedbackDialog;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.StationsAdapter;
import ru.ppr.cppk.ui.fragment.base.MvpDialogFragment;
import ru.ppr.cppk.ui.widget.StationEditText;
import ru.ppr.cppk.utils.validators.CyrillicTextWatcher;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;

public class TripOpeningClosureDialog extends MvpDialogFragment implements TripStartView  {

    public static final String FRAGMENT_TAG = TripOpeningClosureDialog.class.getSimpleName();

    private Button btnValidateOpeningClosureTrip;
    private TextView tvTitleOpeningClosureTrip;
    private StationEditText departureStationEditText;

    private View focusableContainer;
    private ProgressDialog loadingDialog;

    // Dependencies
    private TripSaleStartComponent component;
    private TripOpeningClosurePresenter presenter;

    //Adapter
    private StationsAdapter departureStationsAdapter;

    // Stations in modes
    private static final int STATION_IN_EDIT_MODE_NONE = 0;
    private static final int STATION_IN_EDIT_MODE_DEP = 1;
    private static final int STATION_IN_EDIT_MODE_DEST = 2;

    private int stationInEditMode = STATION_IN_EDIT_MODE_NONE;

    private static final String STR_VALID_PROHOD = "STR_VALID_PROHOD";

    private boolean validProhod;

    private TripOpeningClosureDialog.DialogBtnClickListener dialogPositiveBtnClickListener;
    private DialogInterface.OnCancelListener onCancelListener;
    private DialogInterface.OnDismissListener onDismissListener;

    public static TripOpeningClosureDialog newInstance(boolean validProhod) {
        TripOpeningClosureDialog fragment = new TripOpeningClosureDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(STR_VALID_PROHOD, validProhod);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerTripSaleStartComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(getActivity()))
                .build();
        super.onCreate(savedInstanceState);

        Logger.info(FRAGMENT_TAG, "Create Dialog Fragment Trip Validity");

        Bundle args = getArguments();
        validProhod = args.getBoolean(STR_VALID_PROHOD);

        departureStationsAdapter = new StationsAdapter(getActivity());
        departureStationsAdapter.setFilter(depStationAdapterFilter);

        presenter = getMvpDelegate().getPresenter(component::tripOpeningClosurePresenter, TripOpeningClosurePresenter.class);
        presenter.initialize2();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new FeedbackDialog(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_opening_closure_trip, null);

        btnValidateOpeningClosureTrip = (Button) view.findViewById(R.id.scb_validate_opening_closure_trip);
        tvTitleOpeningClosureTrip = (TextView) view.findViewById(R.id.tv_title_opening_closure_trip);
        departureStationEditText = (StationEditText) view.findViewById(R.id.departure_station_edit_text);
        focusableContainer = view.findViewById(R.id.focusableContainer);

        try {
            departureStationEditText.addTextChangedListener(new CyrillicTextWatcher(departureStationEditText));
            departureStationEditText.setOnItemClickListener(depStationOnItemClickListener);
            departureStationEditText.setOnBackListener(stationBackListener);
            departureStationEditText.setOnEditorActionListener(depStationEditorListener);
            departureStationEditText.setOnFocusChangeListener(depStationViewFocusChangeListener);
            departureStationEditText.setOnClickListener(stationClickListener);
        } catch (Throwable e){
            Logger.error(FRAGMENT_TAG, e.getMessage());
        }

        btnValidateOpeningClosureTrip.setOnClickListener(v -> {
            if(dialogPositiveBtnClickListener != null && presenter.getIdSelectStation()!= null) {
                    dialogPositiveBtnClickListener.onBtnClick(TripOpeningClosureDialog.this, validProhod, presenter.getIdSelectStation());
            }
        });

        tvTitleOpeningClosureTrip.setText(getString((validProhod) ? R.string.str_title_closure_trip : R.string.str_title_opening_trip));
        btnValidateOpeningClosureTrip.setText(getString((validProhod) ? R.string.str_validate_closure : R.string.str_validate_opening));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.cppk_dialog_width);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(params);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    /**
     * Слушатель нажатий на кнопки диалога.
     */
    public interface DialogBtnClickListener {
        void onBtnClick(DialogFragment dialog, boolean validProzod, Integer idStation);
    }

    /**
     * Фильтр станций отправления
     */
    private final Filter depStationAdapterFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String text = constraint == null ? "" : constraint.toString();
            List<Station> stations = presenter.onDepartureStationTextChanged(text);
            FilterResults filterResults = new FilterResults();
            filterResults.values = stations;
            filterResults.count = stations.size();

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // nop
            // Презентер напрямую пробрасывает результаты в адаптер
            Logger.info(FRAGMENT_TAG, "Publish Results");
        }

    };

    /**
     * @param onCancelListener
     */
    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    /**
     * @param onDismissListener
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.info(FRAGMENT_TAG, "Resume Dialog Fragment");
        // Устанавливаем адаптер в onResume, чтобы избежать срабатывания Filter.performFiltering() в onRestoreInstanceState()
        departureStationEditText.setAdapter(departureStationsAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.info(FRAGMENT_TAG, "Pause Dialog Fragment");
        departureStationEditText.setAdapter(null);
    }


    public void setDialogPositiveBtnClickListener(TripOpeningClosureDialog.DialogBtnClickListener dialogPositiveBtnClickListener) {
        this.dialogPositiveBtnClickListener = dialogPositiveBtnClickListener;
    }

    /**
     * Обработчик выбора станции отправления.
     */
    private final AdapterView.OnItemClickListener depStationOnItemClickListener = (parent, view, position, id) -> {
        presenter.onDepartureStationSelected(position);
        hideKeyboard();
    };


    @Override
    public void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(getActivity());
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setMessage(getString(R.string.transfer_sale_preparation_dialog_loading));
        }

        loadingDialog.show();

        Logger.info(FRAGMENT_TAG, "Show Loading Dialog");
    }

    @Override
    public void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        Logger.info(FRAGMENT_TAG, "Hide Loading Dialog");
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        departureStationEditText.setText(departureStationName, false);
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        Logger.info(FRAGMENT_TAG, "Destination Station Name");
    }

    @Override
    public void setDepartureStations(@NonNull List<Station> stations) {
        departureStationsAdapter.setItems(stations);
    }

    @Override
    public void setContinueBtnEnable(boolean enable) {
        Logger.info(FRAGMENT_TAG, "Continue Enable: " + enable);
        btnValidateOpeningClosureTrip.setEnabled(enable);
    }

    /**
     * Обработчик отмены выбора станции.
     */
    private final StationEditText.OnBackListener stationBackListener = () -> {
        cancelEditStation();
        return true;
    };

    private void cancelEditStation() {
        Logger.info(FRAGMENT_TAG, "cancelEditStation");
        if (stationInEditMode == STATION_IN_EDIT_MODE_NONE) {
            return;
        }
        hideKeyboard();
        if (stationInEditMode == STATION_IN_EDIT_MODE_DEP) {
            presenter.onDepartureStationEditCanceled();
        } else if (stationInEditMode == STATION_IN_EDIT_MODE_DEST) {
            presenter.onDestinationStationEditCanceled();
        }
    }

    private void hideKeyboard() {
        Logger.info(FRAGMENT_TAG, "hideKeyboard");
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusableContainer.getWindowToken(), 0);
        focusableContainer.requestFocus();
    }

    /**
     * Обработчик набора текста станции отправления.
     */
    private final TextView.OnEditorActionListener depStationEditorListener = (v, actionId, event) -> {
        // Не реагируем на кнопку "Done"
        return actionId == EditorInfo.IME_ACTION_DONE;
    };

    /**
     * Обработчик смены фокуса станции отправления.
     */
    private final View.OnFocusChangeListener depStationViewFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            if (stationInEditMode == STATION_IN_EDIT_MODE_DEST) {
                presenter.onDestinationStationEditCanceled();
            }
            stationInEditMode = STATION_IN_EDIT_MODE_DEP;
            departureStationEditText.setText("", false);
            departureStationEditText.showDropDown();
        }
    };

    /**
     * Обработчик нажатия.
     */
    private final View.OnClickListener stationClickListener = (v -> ((AutoCompleteTextView) v).showDropDown());

}

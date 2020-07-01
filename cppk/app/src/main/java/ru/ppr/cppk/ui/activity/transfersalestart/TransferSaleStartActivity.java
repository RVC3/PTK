package ru.ppr.cppk.ui.activity.transfersalestart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.logic.CriticalNsiVersionDialogDelegate;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleParams;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.StationsAdapter;
import ru.ppr.cppk.ui.adapter.spinner.TicketTypeAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.widget.StationEditText;
import ru.ppr.cppk.utils.validators.CyrillicTextWatcher;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketType;

/**
 * Экран начала оформления трансфера.
 *
 * @author Aleksandr Brazhkin
 */
public class TransferSaleStartActivity extends MvpActivity implements TransferSaleStartView {

    private static final String TAG = Logger.makeLogTag(TransferSaleStartActivity.class);

    // Dialog tags
    private static final String DIALOG_CRITICAL_NSI_CLOSE_TAG = "DIALOG_CRITICAL_NSI_CLOSE_TAG";
    private static final String DIALOG_NO_ROUTES_TAG = "DIALOG_NO_ROUTES_TAG";
    // Stations in modes
    private static final int STATION_IN_EDIT_MODE_NONE = 0;
    private static final int STATION_IN_EDIT_MODE_DEP = 1;
    private static final int STATION_IN_EDIT_MODE_DEST = 2;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, TransferSaleStartActivity.class);
    }

    // Dependencies
    private TransferSaleStartComponent component;
    private TransferSaleStartPresenter presenter;
    // Views
    private ProgressDialog loadingDialog;
    private StationEditText departureStationEditText;
    private StationEditText destinationStationEditText;
    private View ticketTypeContainer;
    private Spinner ticketTypeSpinner;
    private View buttonsLayout;
    private Button continueBtn;
    private View focusableContainer;
    // Other
    private TicketTypeAdapter ticketTypeAdapter;
    private StationsAdapter departureStationsAdapter;
    private StationsAdapter destinationStationsAdapter;
    private int selectedTicketTypePosition = 0;
    private int stationInEditMode = STATION_IN_EDIT_MODE_NONE;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        component = DaggerTransferSaleStartComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_sale_start);

        departureStationsAdapter = new StationsAdapter(this);
        departureStationsAdapter.setFilter(depStationAdapterFilter);
        destinationStationsAdapter = new StationsAdapter(this);
        destinationStationsAdapter.setFilter(destStationAdapterFilter);

        departureStationEditText = (StationEditText) findViewById(R.id.departure_station_edit_text);
        departureStationEditText.addTextChangedListener(new CyrillicTextWatcher(departureStationEditText));
        departureStationEditText.setOnItemClickListener(depStationOnItemClickListener);
        departureStationEditText.setOnBackListener(stationBackListener);
        departureStationEditText.setOnEditorActionListener(depStationEditorListener);
        departureStationEditText.setOnFocusChangeListener(depStationViewFocusChangeListener);
        departureStationEditText.setOnClickListener(stationClickListener);
        destinationStationEditText = (StationEditText) findViewById(R.id.destination_station_edit_text);
        destinationStationEditText.addTextChangedListener(new CyrillicTextWatcher(destinationStationEditText));
        destinationStationEditText.setOnItemClickListener(destStationOnItemClickListener);
        destinationStationEditText.setOnBackListener(stationBackListener);
        destinationStationEditText.setOnEditorActionListener(destStationEditorListener);
        destinationStationEditText.setOnFocusChangeListener(destStationViewFocusChangeListener);
        destinationStationEditText.setOnClickListener(stationClickListener);
        ticketTypeContainer = findViewById(R.id.ticketTypeContainer);
        ticketTypeSpinner = (Spinner) findViewById(R.id.ticketTypeSpinner);
        ticketTypeAdapter = new TicketTypeAdapter(this);
        ticketTypeSpinner.setAdapter(ticketTypeAdapter);
        ticketTypeSpinner.setOnTouchListener(ticketTypeSpinnerOnTouchListener);
        ticketTypeSpinner.setOnItemSelectedListener(ticketTypeSpinnerOnItemSelectedListener);
        buttonsLayout = findViewById(R.id.buttonsLayout);
        continueBtn = (Button) findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(v -> presenter.onContinueBtnClicked());
        focusableContainer = findViewById(R.id.focusableContainer);

        presenter = getMvpDelegate().getPresenter(component::transferSaleStartPresenter, TransferSaleStartPresenter.class);
        presenter.setInteractionListener(preparationPresenterStep1InteractionListener);

        presenter.initialize2();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Устанавливаем адаптер в onResume, чтобы избежать срабатывания Filter.performFiltering() в onRestoreInstanceState()
        departureStationEditText.setAdapter(departureStationsAdapter);
        destinationStationEditText.setAdapter(destinationStationsAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        departureStationEditText.setAdapter(null);
        destinationStationEditText.setAdapter(null);
    }

    @Override
    public void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setMessage(getString(R.string.transfer_sale_preparation_dialog_loading));
        }

        loadingDialog.show();
    }

    @Override
    public void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        departureStationEditText.setText(departureStationName, false);
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        destinationStationEditText.setText(destinationStationName, false);
    }

    @Override
    public void setDepartureStations(@NonNull List<Station> stations) {
        departureStationsAdapter.setItems(stations);
    }

    @Override
    public void setDestinationStations(@NonNull List<Station> stations) {
        destinationStationsAdapter.setItems(stations);
    }

    @Override
    public void setTicketTypes(@NonNull List<TicketType> ticketTypes) {
        Logger.trace(TAG, "setTicketTypes, size = " + ticketTypes.size());
        ticketTypeAdapter.setItems(ticketTypes);
    }

    @Override
    public void setTicketTypesSelectVisible(boolean visible) {
        ticketTypeContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSelectedTicketTypePosition(int position) {
        // В нулевой позиции мнимый элемент
        // https://aj.srvdev.ru/browse/CPPKPP-28012
        int uiPosition = position + 1;
        Logger.trace(TAG, "setSelectedTicketTypePosition, uiPosition = " + uiPosition);
        ticketTypeAdapter.setSelectedPosition(uiPosition);
        ticketTypeSpinner.setSelection(uiPosition);
        selectedTicketTypePosition = uiPosition;
    }

    @Override
    public void setContinueBtnVisible(boolean visible) {
        buttonsLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setCriticalNsiBackDialogVisible(boolean visible) {
        SimpleDialog criticalNsiVersionDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_CRITICAL_NSI_CLOSE_TAG);

        if (visible) {
            // http://agile.srvdev.ru/browse/CPPKPP-35280
            // (Сделать проверки при разблокировке ПТК и при открытии экрана продажи.)
            // ShiftAlarmManager тут не работает
            CriticalNsiVersionDialogDelegate criticalNsiVersionDialogDelegate = new CriticalNsiVersionDialogDelegate(
                    Dagger.appComponent().criticalNsiChecker(),
                    getFragmentManager(),
                    getResources(),
                    DIALOG_CRITICAL_NSI_CLOSE_TAG);

            if (criticalNsiVersionDialog == null) {
                criticalNsiVersionDialogDelegate.showCriticalNsiCloseDialogIfNeeded(onCriticalNsiBackDialogShownListener);
            } else {
                criticalNsiVersionDialog.setDialogPositiveBtnClickListener(onCriticalNsiBackDialogShownListener);
            }
        } else {
            if (criticalNsiVersionDialog != null) {
                criticalNsiVersionDialog.dismiss();
            }
        }
    }

    @Override
    public void setCriticalNsiCloseShiftDialogVisible(boolean visible) {
        SimpleDialog criticalNsiVersionDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_CRITICAL_NSI_CLOSE_TAG);

        if (visible) {
            // http://agile.srvdev.ru/browse/CPPKPP-35280
            // (Сделать проверки при разблокировке ПТК и при открытии экрана продажи.)
            // ShiftAlarmManager тут не работает
            CriticalNsiVersionDialogDelegate criticalNsiVersionDialogDelegate = new CriticalNsiVersionDialogDelegate(
                    Dagger.appComponent().criticalNsiChecker(),
                    getFragmentManager(),
                    getResources(),
                    DIALOG_CRITICAL_NSI_CLOSE_TAG);

            if (criticalNsiVersionDialog == null) {
                criticalNsiVersionDialogDelegate.showCriticalNsiCloseDialogIfNeeded(criticalNsiDialogListener);
            } else {
                criticalNsiVersionDialog.setDialogPositiveBtnClickListener(criticalNsiDialogListener);
            }
        } else {
            if (criticalNsiVersionDialog != null) {
                criticalNsiVersionDialog.dismiss();
            }
        }
    }

    @Override
    public void showNoStationsForSaleError() {
        SimpleDialog noTransferCanBeSoldDialog = SimpleDialog.newInstance(null,
                getString(R.string.transfer_sale_start_no_routes_msg),
                getString(R.string.transfer_sale_start_no_routes_close_btn),
                null,
                LinearLayout.HORIZONTAL,
                0);
        noTransferCanBeSoldDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> finish());
        noTransferCanBeSoldDialog.show(getFragmentManager(), DIALOG_NO_ROUTES_TAG);
    }

    /**
     * Скрывает клавиатуру для view
     */
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusableContainer.getWindowToken(), 0);
        focusableContainer.requestFocus();
    }

    private void cancelEditStation() {
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

    /**
     * Обработчик нажатия.
     */
    private final View.OnClickListener stationClickListener = (v -> ((AutoCompleteTextView) v).showDropDown());

    /**
     * Обработчик отмены выбора станции.
     */
    private final StationEditText.OnBackListener stationBackListener = () -> {
        cancelEditStation();
        return true;
    };

    /**
     * Обработчик выбора станции отправления.
     */
    private final AdapterView.OnItemClickListener depStationOnItemClickListener = (parent, view, position, id) -> {
        presenter.onDepartureStationSelected(position);
        hideKeyboard();
    };

    /**
     * Обработчик выбора станции прибытия.
     */
    private final AdapterView.OnItemClickListener destStationOnItemClickListener = (parent, view, position, id) -> {
        presenter.onDestinationStationSelected(position);
        hideKeyboard();
    };

    /**
     * Обработчик набора текста станции отправления.
     */
    private final TextView.OnEditorActionListener depStationEditorListener = (v, actionId, event) -> {
        // Не реагируем на кнопку "Done"
        return actionId == EditorInfo.IME_ACTION_DONE;
    };

    /**
     * Обработчик набора текста станции прибытия.
     */
    private final TextView.OnEditorActionListener destStationEditorListener = (v, actionId, event) -> {
        // Не реагируем на кнопку "Done"
        return actionId == EditorInfo.IME_ACTION_DONE;
    };

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
        }

    };

    /**
     * Фильтр станций назначения
     */
    private final Filter destStationAdapterFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String text = constraint == null ? "" : constraint.toString();
            List<Station> stations = presenter.onDestinationStationTextChanged(text);
            FilterResults filterResults = new FilterResults();
            filterResults.values = stations;
            filterResults.count = stations.size();

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // nop
            // Презентер напрямую пробрасывает результаты в адаптер
        }

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
     * Обработчик смены фокуса станции назнчения.
     */
    private final View.OnFocusChangeListener destStationViewFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            if (stationInEditMode == STATION_IN_EDIT_MODE_DEP) {
                presenter.onDepartureStationEditCanceled();
            }
            stationInEditMode = STATION_IN_EDIT_MODE_DEST;
            destinationStationEditText.setText("", false);
            destinationStationEditText.showDropDown();
        }
    };

    /**
     * Блокировщик открытия выпадающего списка типов ПД, если в нем нет иных вариантов.
     */
    private final View.OnTouchListener ticketTypeSpinnerOnTouchListener = (v, event) -> {
        for (int i = 0; i < ticketTypeAdapter.getItems().size(); i++) {
            if (ticketTypeAdapter.getSelectedPosition() - 1 != i) {
                return false;
            }
        }
        return true;
    };

    /**
     * Обработчик типа билета.
     */
    private final AdapterView.OnItemSelectedListener ticketTypeSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Logger.trace(TAG, "onItemSelected, uiPosition = " + position);
            if (selectedTicketTypePosition == position) {
                Logger.trace(TAG, "onItemSelected skipped");
            } else {
                // В нулевой позиции мнимый элемент
                // https://aj.srvdev.ru/browse/CPPKPP-28012
                ticketTypeAdapter.setSelectedPosition(position);
                selectedTicketTypePosition = position;
                presenter.onTicketTypeSelected(position - 1);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            /* NOP */
        }
    };

    private final TransferSaleStartPresenter.InteractionListener preparationPresenterStep1InteractionListener = new TransferSaleStartPresenter.InteractionListener() {

        @Override
        public void navigateBack() {
            finish();
        }

        @Override
        public void navigateToCloseShiftActivity() {
            Navigator.navigateToCloseShiftActivity(TransferSaleStartActivity.this, true, true);
        }

        @Override
        public void navigateToReadSourceTicket(ReadForTransferParams params) {
            Navigator.navigateToReadPdForTransferActivity(TransferSaleStartActivity.this, params);
        }

        @Override
        public void navigateToTransferSale(TransferSaleParams params) {
            Navigator.navigateToTransferSaleActivity(TransferSaleStartActivity.this, params);
        }

    };

    private final SimpleDialog.DialogBtnClickListener onCriticalNsiBackDialogShownListener = (dialog, dialogId) -> presenter.onCriticalNsiBackDialogRead();

    private final SimpleDialog.DialogBtnClickListener criticalNsiDialogListener = (dialog, dialogId) -> presenter.onCriticalNsiOkBtnClicked();
}

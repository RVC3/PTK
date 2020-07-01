package ru.ppr.cppk.ui.activity.selectTransferStations;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.StationsAdapter;
import ru.ppr.cppk.ui.widget.StationEditText;
import ru.ppr.cppk.utils.validators.CyrillicTextWatcher;
import ru.ppr.nsi.entity.Station;


/**
 * Экран выбора станций маршрута работы ПТК в режиме контроля трансфера.
 *
 * @author Grigoriy Kashka
 */
public class SelectTransferStationsActivity extends MvpActivity implements SelectTransferStationsView {

    private static final int STATION_IN_EDIT_MODE_NONE = 0;
    private static final int STATION_IN_EDIT_MODE_DEP = 1;
    private static final int STATION_IN_EDIT_MODE_DEST = 2;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SelectTransferStationsActivity.class);
    }

    // region Di
    private SelectTransferStationsComponent component;
    // endregion
    // region Views
    // Views
    private ProgressDialog mProgressDialog;
    private StationEditText departureStationEditText;
    private StationEditText destinationStationEditText;
    private View blockingView;
    private Button closeBtn;
    //endregion
    //region Other
    ////////////////////////////////////////////////
    // Adapters, etc.
    private StationsAdapter departureStationsAdapter;
    private StationsAdapter destinationStationsAdapter;
    ////////////////////////////////////////////////
    private int stationInEditMode = STATION_IN_EDIT_MODE_NONE;
    ////////////////////////////////////////////////
    private SelectTransferStationsPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerSelectTransferStationsComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::selectTransferStationsPresenter, SelectTransferStationsPresenter.class);

        departureStationsAdapter = new StationsAdapter(SelectTransferStationsActivity.this);
        departureStationsAdapter.setFilter(depStationAdapterFilter);

        destinationStationsAdapter = new StationsAdapter(SelectTransferStationsActivity.this);
        destinationStationsAdapter.setFilter(destStationAdapterFilter);

        setContentView(R.layout.activity_select_transfer_stations);

        //////////////////////////////////////////////////////////////////////////////////////
        departureStationEditText = (StationEditText) findViewById(R.id.departureStationEditText);
        departureStationEditText.addTextChangedListener(new CyrillicTextWatcher(departureStationEditText));
        departureStationEditText.setOnItemClickListener(depStationOnItemClickListener);
        departureStationEditText.setOnBackListener(stationBackListener);
        departureStationEditText.setOnEditorActionListener(depStationEditorListener);
        departureStationEditText.setOnFocusChangeListener(depStationViewFocusChangeListener);
        departureStationEditText.setOnClickListener(stationClickListener);
        //////////////////////////////////////////////////////////////////////////////////////
        destinationStationEditText = (StationEditText) findViewById(R.id.destinationStationEditText);
        destinationStationEditText.addTextChangedListener(new CyrillicTextWatcher(destinationStationEditText));
        destinationStationEditText.setOnItemClickListener(destStationOnItemClickListener);
        destinationStationEditText.setOnBackListener(stationBackListener);
        destinationStationEditText.setOnEditorActionListener(destStationEditorListener);
        destinationStationEditText.setOnFocusChangeListener(destStationViewFocusChangeListener);
        destinationStationEditText.setOnClickListener(stationClickListener);
        ///////////////////////////////////////////////////////////////////////////////////////
        closeBtn = (Button) findViewById(R.id.selectTransferStationsCloseBtn);
        closeBtn.setOnClickListener(v -> presenter.onCloseBtnClicked());
        ///////////////////////////////////////////////////////////////////////////////////////
        blockingView = findViewById(R.id.blockingView);
        ///////////////////////////////////////////////////////////////////////////////////////

        mProgressDialog = new FeedbackProgressDialog(SelectTransferStationsActivity.this);
        mProgressDialog.setCancelable(false);

        presenter.setNavigator(navigator);
        presenter.initialize2();
    }

    @Override
    public void onClickSettings() {
        // Переопределяем метод чтобы нельзя было выйти в меню с этого окна
    }

    @Override
    public void onResume() {
        super.onResume();
        // Устанавливаем адаптер в onResume, чтобы избежать срабатывания Filter.performFiltering() в onRestoreInstanceState()
        departureStationEditText.setAdapter(departureStationsAdapter);
        destinationStationEditText.setAdapter(destinationStationsAdapter);
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
    public void setDepartureStations(List<Station> stations) {
        departureStationsAdapter.setItems(stations);
    }

    @Override
    public void setDestinationStations(List<Station> stations) {
        destinationStationsAdapter.setItems(stations);
    }

    @Override
    public void showProgress() {
        blockingView.setVisibility(View.VISIBLE);
        mProgressDialog.show();
    }

    @Override
    public void hideProgress() {
        blockingView.setVisibility(View.GONE);
        mProgressDialog.dismiss();
    }

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
     * Обработчик нажатия.
     */
    private View.OnClickListener stationClickListener = (v -> ((AutoCompleteTextView) v).showDropDown());

    /**
     * Обработчик отмены выбора станции.
     */
    private final StationEditText.OnBackListener stationBackListener = () -> {
        cancelEditStation();
        return true;
    };

    /**
     * Обработчик набора текста станции отправления.
     */
    private final TextView.OnEditorActionListener depStationEditorListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // Не реагируем на кнопку "Done"
            return true;
        }
        return false;
    };

    /**
     * Обработчик смены фокуса станции отправления.
     */
    private View.OnFocusChangeListener depStationViewFocusChangeListener = (v, hasFocus) -> {
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
    private View.OnFocusChangeListener destStationViewFocusChangeListener = (v, hasFocus) -> {
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
     * Обработчик набора текста станции прибытия.
     */
    private final TextView.OnEditorActionListener destStationEditorListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // Не реагируем на кнопку "Done"
            return true;
        }
        return false;
    };

    /**
     * Фильтр для станций отправления.
     *
     * @see Filter
     */
    private final Filter depStationAdapterFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String text = constraint == null ? "" : constraint.toString();
            List<Station> stations = presenter.onDepartureStationTextChanged(text);
            final FilterResults filterResults = new FilterResults();
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
     * Фильтр для станций прибытия.
     *
     * @see Filter
     */
    private final Filter destStationAdapterFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String text = constraint == null ? "" : constraint.toString();
            List<Station> stations = presenter.onDestinationStationTextChanged(text);
            final FilterResults filterResults = new FilterResults();
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
     * Скрывает клавиатуру для view
     */
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        getWindow().getDecorView().getRootView().requestFocus();
    }

    private SelectTransferStationsPresenter.Navigator navigator = () -> finish();

}

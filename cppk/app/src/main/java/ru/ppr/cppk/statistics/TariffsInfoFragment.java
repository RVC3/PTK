package ru.ppr.cppk.statistics;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.TariffsChain;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.StationsAdapter;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.widget.AutoCompleteTextViewWithBack;
import ru.ppr.cppk.ui.widget.SelectStationMask;
import ru.ppr.cppk.ui.widget.SmartAutoCompleteTextView;
import ru.ppr.cppk.utils.validators.CyrillicTextWatcher;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.utils.ObjectUtils;
import rx.Completable;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Экран с информацией о тарифах.
 */
public class TariffsInfoFragment extends FragmentParent implements FragmentOnBackPressed {

    public static final String FRAGMENT_TAG = TariffsInfoFragment.class.getSimpleName();
    private static final String TAG = Logger.makeLogTag(TariffsInfoFragment.class);

    private static final long PD_TYPE_CODE_FULL = 1;
    private static final long PD_TYPE_CHILD = 2;
    private static final long TRAIN_CATEGORY_CODE_PASSENGER = 1;
    private static final long TRAIN_CATEGORY_CODE_FAST = 2;

    public static Fragment newInstance() {
        return new TariffsInfoFragment();
    }

    public interface OnFragmentInteractionListener {

    }

    private SmartAutoCompleteTextView departureStationTextView;
    private SmartAutoCompleteTextView destinationStationTextView;
    private StationsAdapter departureStationsAdapter;
    private StationsAdapter destinationStationsAdapter;

    private View passengerTrainView;
    private View fastTrainView;

    private TextView passengerFullPd;
    private TextView passengerKidPd;

    private TextView fastFullPd;
    private TextView fastKidPd;

    private Station departureStation = null;
    private Station destinationStation = null;

    private Globals globals;
    private NsiDaoSession nsiDaoSession;
    private NsiVersionManager nsiVersionManager;
    private TariffPlanRepository tariffPlanRepository;

    private Tariff passengerFullPdTariff = null;
    private Tariff passengerKidPdTariff = null;
    private Tariff fastFullPdTariff = null;
    private Tariff fastKidPdTariff = null;

    private SelectStationMask selectStationMask;

    private OnFragmentInteractionListener onFragmentInteractionListener = null;

    private FeedbackProgressDialog progressDialog;

    private boolean isProcessing = false;
    /**
     * Текущее время
     */
    private Date timestamp;
    /**
     * Текущая версия НСИ
     */
    private int nsiVersion;
    /**
     * Кешированный список станций отправления в выпадающем списке без фильтра по введенным символам.
     */
    private List<Station> mDepartureStationsWithoutFilter = null;
    /**
     * Кешированный список станций назначения в выпадающем списке без фильтра по введенным символам.
     */
    private List<Station> mDestinationStationsWithoutFilter = null;

    private PdSaleEnv pdSaleEnv;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
        }
    }

    @Override
    public void onDetach() {
        onFragmentInteractionListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globals = (Globals) getActivity().getApplication();

        nsiDaoSession = Di.INSTANCE.getDbManager().getNsiDaoSession().get();
        nsiVersionManager = Di.INSTANCE.nsiVersionManager();
        tariffPlanRepository = Dagger.appComponent().tariffPlanRepository();

        timestamp = new Date();
        nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

        pdSaleEnv = Dagger.appComponent().pdSaleEnvFactory().pdSaleEnvForTariffsInfo();
        pdSaleEnv.pdSaleRestrictions().update(Dagger.appComponent().pdSaleRestrictionsParamsBuilder().create(timestamp, nsiVersion));

        departureStationsAdapter = new StationsAdapter(getActivity());
        departureStationsAdapter.setFilter(departureStationsAdapterFilter);

        destinationStationsAdapter = new StationsAdapter(getActivity());
        destinationStationsAdapter.setFilter(destinationStationsAdapterFilter);

        progressDialog = new FeedbackProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.statistics_tariffs, container, false);

        departureStationTextView = (SmartAutoCompleteTextView) view.findViewById(R.id.statistics_tariffs_departureStation);
        destinationStationTextView = (SmartAutoCompleteTextView) view.findViewById(R.id.statistics_tariffs_destinationStation);

        //////////////////////////////////////////////////////////////////////////////////////

        departureStationTextView.setThreshold(1);
        departureStationTextView.addTextChangedListener(new CyrillicTextWatcher(departureStationTextView.getTextView()));
        departureStationTextView.setOnItemClickListener(depStationOnItemClickListener);
        departureStationTextView.setOnBackListener(stationBackListener);
        departureStationTextView.setOnEditorActionListener(depStationEditorListener);
        departureStationTextView.setOnFocusChangeListener(stationViewFocusChangeListener);
        departureStationTextView.setOnHamburgerClickListener(stationHamburgerClickListener);
        departureStationTextView.setOnClickListener(stationHamburgerClickListener);
        departureStationTextView.setAdapter(departureStationsAdapter);

        //////////////////////////////////////////////////////////////////////////////////////

        destinationStationTextView.setThreshold(1);
        destinationStationTextView.addTextChangedListener(new CyrillicTextWatcher(destinationStationTextView.getTextView()));
        destinationStationTextView.setOnItemClickListener(destStationOnItemClickListener);
        destinationStationTextView.setOnBackListener(stationBackListener);
        destinationStationTextView.setOnEditorActionListener(destStationEditorListener);
        destinationStationTextView.setOnFocusChangeListener(stationViewFocusChangeListener);
        destinationStationTextView.setOnHamburgerClickListener(stationHamburgerClickListener);
        destinationStationTextView.setOnClickListener(stationHamburgerClickListener);
        destinationStationTextView.setAdapter(destinationStationsAdapter);

        //////////////////////////////////////////////////////////////////////////////////////

        passengerFullPd = (TextView) view.findViewById(R.id.statistics_tariffs_cost_full_pd_for_passenger_train);
        passengerKidPd = (TextView) view.findViewById(R.id.statistics_tariffs_cost_kids_pd_for_passenger_train);
        fastFullPd = (TextView) view.findViewById(R.id.statistics_tariffs_cost_full_pd_for_fast_train);
        fastKidPd = (TextView) view.findViewById(R.id.statistics_tariffs_cost_kids_pd_for_fast_train);

        passengerTrainView = view.findViewById(R.id.statistics_tariffs_passenger_train_layout);
        fastTrainView = view.findViewById(R.id.statistics_tariffs_fast_train_layout);

        selectStationMask = (SelectStationMask) view.findViewById(R.id.selectStationMask);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        onInitialize();
    }

    @Override
    public void onDestroyView() {
        if (selectStationMask != null) {
            selectStationMask.clearResources();
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        super.onDestroyView();
    }

    /**
     * Фильтр для станций отправления.
     *
     * @see Filter
     */
    private Filter departureStationsAdapterFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Station> stations = getDepartureStations(constraint.toString());
            // Assign the data to the FilterResults
            FilterResults filterResults = new Filter.FilterResults();
            filterResults.values = stations;
            filterResults.count = stations.size();
            return filterResults;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                departureStationsAdapter.setItems((List<Station>) results.values);
            } else {
                departureStationsAdapter.notifyDataSetInvalidated();
            }

        }
    };

    /**
     * Фильтр для станций прибытия.
     *
     * @see Filter
     */
    private Filter destinationStationsAdapterFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Station> stations = getDestinationStations(constraint.toString());
            // Assign the data to the FilterResults
            FilterResults filterResults = new FilterResults();
            filterResults.values = stations;
            filterResults.count = stations.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                destinationStationsAdapter.setItems((List<Station>) results.values);
            } else {
                destinationStationsAdapter.notifyDataSetInvalidated();
            }
        }
    };

    /**
     * Обработчик выбора станции отправления.
     */
    private AdapterView.OnItemClickListener depStationOnItemClickListener = (parent, view, position, id) -> {
        showMask(false, departureStationTextView, false);
        hideKeyboard();
        Station station = departureStationsAdapter.getItem(position);
        callObservableWithProgressBar(setDepartureStationObservable(station)
                .flatMap(aVoid -> findTariffForStationsObservable()));
    };

    /**
     * Обработчик выбора станции прибытия.
     */
    private AdapterView.OnItemClickListener destStationOnItemClickListener = (parent, view, position, id) -> {
        showMask(false, destinationStationTextView, false);
        hideKeyboard();
        Station station = destinationStationsAdapter.getItem(position);
        callObservableWithProgressBar(setDestinationStationObservable(station)
                .flatMap(aVoid -> findTariffForStationsObservable()));
    };

    /**
     * Обработчик возврата при выборе станции.
     */
    private AutoCompleteTextViewWithBack.OnBackListener stationBackListener = new AutoCompleteTextViewWithBack.OnBackListener() {
        @Override
        public boolean onBackPressed() {
            if (selectStationMask.isMaskShown()) {
                showMask(false, null, true);
                hideKeyboard();
                return true;
            }
            return false;
        }
    };

    /**
     * Обработчик набора текста станции отправления.
     */
    private TextView.OnEditorActionListener depStationEditorListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            showMask(false, null, false);
            hideKeyboard();
            callObservableWithProgressBar(setDepartureStationObservable(null)
                    .flatMap(aVoid -> findTariffForStationsObservable()));
        }
        return false;
    };

    /**
     * Обработчик набора текста станции прибытия.
     */
    private TextView.OnEditorActionListener destStationEditorListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            showMask(false, null, false);
            hideKeyboard();
            callObservableWithProgressBar(setDestinationStationObservable(null)
                    .flatMap(aVoid -> findTariffForStationsObservable()));
        }
        return false;
    };

    /**
     * Обработчик смены фокуса.
     */
    private View.OnFocusChangeListener stationViewFocusChangeListener = (v, hasFocus) -> showMask(hasFocus, v, hasFocus);

    /**
     * Обработчик нажатия.
     */
    private View.OnClickListener stationHamburgerClickListener = (v -> showMask(true, v, false));

    /**
     * Метод для отображения маски.
     *
     * @param state
     * @param autoCompleteTextView
     * @param refreshDisplayedStations
     */
    private void showMask(boolean state, View autoCompleteTextView, boolean refreshDisplayedStations) {
        if (refreshDisplayedStations) {
            refreshDisplayedDepartureAndDestinationStationsSync();
        }

        selectStationMask.showMask(state, autoCompleteTextView);
        // убираем углы
        if (autoCompleteTextView == null) {
            //сюда попадаем если надо свернуть список
            departureStationTextView.showTopCorners(state);
            destinationStationTextView.showTopCorners(state);
            departureStationTextView.hideDropDown();
            destinationStationTextView.hideDropDown();
        } else {
            //сюда попадаем если надо развернуть список
            if (!(autoCompleteTextView instanceof SmartAutoCompleteTextView)) {
                throw new IllegalStateException("Show mask should call for SmartAutoCompleteTextView");
            }
            SmartAutoCompleteTextView locCompleteTextView = (SmartAutoCompleteTextView) autoCompleteTextView;
            if (state) {
                if (locCompleteTextView == departureStationTextView) {
                    departureStationsAdapter.setItems(Collections.<Station>emptyList());
                } else if (locCompleteTextView == destinationStationTextView) {
                    destinationStationsAdapter.setItems(Collections.<Station>emptyList());
                }
                locCompleteTextView.setText("");
                locCompleteTextView.showDropDown();
            }
            locCompleteTextView.showTopCorners(state);
        }
    }

    @Override
    public boolean onBackPress() {

        if (selectStationMask != null && selectStationMask.isMaskShown()) {
            showMask(false, null, true);
            hideKeyboard();
            return true;
        }

        return false;
    }

    /**
     * Скрывает клавиатуру для view
     */
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(departureStationTextView.getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(destinationStationTextView.getWindowToken(), 0);
        departureStationTextView.clearFocus();
        destinationStationTextView.clearFocus();
    }

    ////////////////////////SyncMethods//////////////////////////

    /**
     * Метод для обновления отображения станций.
     */
    private void refreshDisplayedDepartureAndDestinationStationsSync() {
        Logger.trace(TAG, "refreshDisplayedDepartureAndDestinationStationsSync - START");
        String newDepartureStationText = departureStation == null ? "" : departureStation.getName().trim();
        String oldDepartureStationText = departureStationTextView.getText().toString();
        if (!TextUtils.equals(oldDepartureStationText, newDepartureStationText)) {
            departureStationTextView.setText(newDepartureStationText);
        }
        departureStationTextView.setSelection(0);

        String newDestinationStationText = destinationStation == null ? "" : destinationStation.getName().trim();
        String oldDestinationStationText = destinationStationTextView.getText().toString();
        if (!TextUtils.equals(oldDestinationStationText, newDestinationStationText)) {
            destinationStationTextView.setText(newDestinationStationText);
        }
        destinationStationTextView.setSelection(0);
        Logger.trace(TAG, "refreshDisplayedDepartureAndDestinationStationsSync - FINISH");
    }

    /**
     * Метод для обновления отображаемых элементов.
     */
    private void updateViewSync() {
        if (passengerFullPdTariff != null) {
            passengerFullPd.setText(String.format(getString(R.string.rub_cent_as_single), passengerFullPdTariff.getPricePd()));
        } else {
            passengerFullPd.setText("");
        }

        if (passengerKidPdTariff != null) {
            passengerKidPd.setText(String.format(getString(R.string.rub_cent_as_single), passengerKidPdTariff.getPricePd()));
        } else {
            passengerKidPd.setText("");
        }

        if (fastFullPdTariff != null) {
            fastFullPd.setText(String.format(getString(R.string.rub_cent_as_single), fastFullPdTariff.getPricePd()));
        } else {
            fastFullPd.setText("");
        }

        if (fastKidPdTariff != null) {
            fastKidPd.setText(String.format(getString(R.string.rub_cent_as_single), fastKidPdTariff.getPricePd()));
        } else {
            fastKidPd.setText("");
        }

        if (passengerFullPdTariff != null || passengerKidPdTariff != null) {
            passengerTrainView.setVisibility(View.VISIBLE);
        } else {
            passengerTrainView.setVisibility(View.GONE);
        }

        if (fastFullPdTariff != null || fastKidPdTariff != null) {
            fastTrainView.setVisibility(View.VISIBLE);
        } else {
            fastTrainView.setVisibility(View.GONE);
        }
    }

    ///////////////////////Observables///////////////////////////


    /**
     * Метод для обновления писка станций отправления.
     *
     * @return
     */
    private Observable<Void> updateDepartureStationsListObservable() {
        return Observable.just(null);
    }

    /**
     * Метод для обновления писка станций прибытия.
     *
     * @return
     */
    private Observable<Void> updateDestinationStationsListObservable() {
        return Observable.just(null);
    }

    /**
     * Метод для установки станции прибытия.
     *
     * @param destinationStation станция прибытия.
     * @return
     */
    private Observable<Void> setDestinationStationObservable(Station destinationStation) {
        return Observable
                .create((Subscriber<? super Observable<Void>> subscriber) -> {
                    Logger.trace(TAG, "setDestinationStationObservable - START");
                    if (ObjectUtils.equals(TariffsInfoFragment.this.destinationStation, destinationStation)) {
                        Logger.trace(TAG, "setDestinationStationObservable - FINISH");
                        subscriber.onNext(refreshDisplayedDepartureAndDestinationStationsObservable());
                    } else {
                        TariffsInfoFragment.this.destinationStation = destinationStation;
                        Logger.trace(TAG, "setDestinationStationObservable - FINISH");
                        subscriber.onNext(refreshDisplayedDepartureAndDestinationStationsObservable()
                                .flatMap(aVoid -> updateDepartureStationsListObservable())
                                .flatMap(aVoid -> Observable.fromCallable((Callable<Void>) () -> {
                                    Thread.sleep(300);
                                    return null;
                                })));
                    }
                    subscriber.onCompleted();
                })
                .flatMap(voidObservable -> voidObservable)
                .subscribeOn(SchedulersCPPK.background());
    }

    /**
     * Метод для установки станции отправления.
     *
     * @param departureStation станция отправления.
     * @return
     */
    private Observable<Void> setDepartureStationObservable(Station departureStation) {
        return Observable
                .create((Subscriber<? super Observable<Void>> subscriber) -> {
                    Logger.trace(TAG, "setDepartureStationObservable - START");
                    if (ObjectUtils.equals(TariffsInfoFragment.this.departureStation, departureStation)) {
                        Logger.trace(TAG, "setDepartureStationObservable - FINISH");
                        subscriber.onNext(refreshDisplayedDepartureAndDestinationStationsObservable());
                    } else {
                        TariffsInfoFragment.this.departureStation = departureStation;
                        Logger.trace(TAG, "setDepartureStationObservable - FINISH");
                        subscriber.onNext(refreshDisplayedDepartureAndDestinationStationsObservable()
                                .flatMap(aVoid -> updateDestinationStationsListObservable())
                                .flatMap(aVoid -> Observable.fromCallable((Callable<Void>) () -> {
                                    Thread.sleep(300);
                                    return null;
                                })));
                    }
                    subscriber.onCompleted();
                })
                .flatMap(voidObservable -> voidObservable)
                .subscribeOn(SchedulersCPPK.background());
    }

    /**
     * Метод для обновления отображаемых станций.
     *
     * @return
     */
    private Observable<Void> refreshDisplayedDepartureAndDestinationStationsObservable() {
        return Observable
                .create((Subscriber<? super Void> subscriber) -> {
                    Logger.trace(TAG, "refreshDisplayedDepartureAndDestinationStationsObservable - START");
                    refreshDisplayedDepartureAndDestinationStationsSync();
                    Logger.trace(TAG, "refreshDisplayedDepartureAndDestinationStationsObservable - FINISH");
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                })
                .observeOn(SchedulersCPPK.background())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Метод для поиска тарифов для станций.
     *
     * @return
     */
    private Observable<Void> findTariffForStationsObservable() {
        return Observable
                .create(new Observable.OnSubscribe<Void>() {
                    @Override
                    public void call(Subscriber<? super Void> subscriber) {
                        Logger.trace(TAG, "findTariffForStationsObservable - START");
                        if (destinationStation != null && departureStation != null) {
                            long depStationCode = (long) departureStation.getCode();
                            long destStationCode = (long) destinationStation.getCode();

                            List<TariffPlan> tariffPlansForPassengerTrainWithTariffs = pdSaleEnv.tariffPlansLoader().loadAllTariffPlans(depStationCode, destStationCode);
                            List<TariffPlan> tariffPlansForPassengerTrain = tariffPlanRepository.getTariffPlans(TRAIN_CATEGORY_CODE_PASSENGER, false, nsiVersion);
                            tariffPlansForPassengerTrain.retainAll(tariffPlansForPassengerTrainWithTariffs);

                            List<TariffsChain> tariffsForPassengerTrainAndFullPd = Collections.emptyList();
                            List<TariffsChain> tariffsForPassengerTrainAndChildPd = Collections.emptyList();
                            if (!tariffPlansForPassengerTrain.isEmpty()) {
                                long tariffPlanCode = tariffPlansForPassengerTrain.get(0).getCode();

                                tariffsForPassengerTrainAndFullPd = pdSaleEnv.tariffsLoader().loadDirectTariffsThere(
                                        depStationCode,
                                        destStationCode,
                                        tariffPlanCode,
                                        PD_TYPE_CODE_FULL
                                );

                                tariffsForPassengerTrainAndChildPd = pdSaleEnv.tariffsLoader().loadDirectTariffsThere(
                                        depStationCode,
                                        destStationCode,
                                        tariffPlanCode,
                                        PD_TYPE_CHILD
                                );
                            }

                            passengerFullPdTariff = tariffsForPassengerTrainAndFullPd.isEmpty() ? null : tariffsForPassengerTrainAndFullPd.get(0).getTariffs().get(0);
                            passengerKidPdTariff = tariffsForPassengerTrainAndChildPd.isEmpty() ? null : tariffsForPassengerTrainAndChildPd.get(0).getTariffs().get(0);


                            List<TariffPlan> tariffPlansForFastTrainTrainWithTariffs = pdSaleEnv.tariffPlansLoader().loadAllTariffPlans(depStationCode, destStationCode);
                            List<TariffPlan> tariffPlansForFastTrain = tariffPlanRepository.getTariffPlans(TRAIN_CATEGORY_CODE_FAST, false, nsiVersion);
                            tariffPlansForFastTrain.retainAll(tariffPlansForFastTrainTrainWithTariffs);

                            List<TariffsChain> tariffsForFastTrainAndFullPd = Collections.emptyList();
                            List<TariffsChain> tariffsForFastTrainAndChildPd = Collections.emptyList();
                            if (!tariffPlansForFastTrain.isEmpty()) {
                                long tariffPlanCode = tariffPlansForFastTrain.get(0).getCode();

                                tariffsForFastTrainAndFullPd = pdSaleEnv.tariffsLoader().loadDirectTariffsThere(
                                        depStationCode,
                                        destStationCode,
                                        tariffPlanCode,
                                        PD_TYPE_CODE_FULL
                                );

                                tariffsForFastTrainAndChildPd = pdSaleEnv.tariffsLoader().loadDirectTariffsThere(
                                        depStationCode,
                                        destStationCode,
                                        tariffPlanCode,
                                        PD_TYPE_CHILD
                                );
                            }

                            fastFullPdTariff = tariffsForFastTrainAndFullPd.isEmpty() ? null : tariffsForFastTrainAndFullPd.get(0).getTariffs().get(0);
                            fastKidPdTariff = tariffsForFastTrainAndChildPd.isEmpty() ? null : tariffsForFastTrainAndChildPd.get(0).getTariffs().get(0);
                        } else {
                            passengerFullPdTariff = null;
                            passengerKidPdTariff = null;
                            fastFullPdTariff = null;
                            fastKidPdTariff = null;
                        }

                        Logger.trace(TAG, "findTariffForStationsObservable - FINISH");
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }
                })
                .flatMap(aVoid -> updateViewObservable())
                .subscribeOn(SchedulersCPPK.background());
    }

    /**
     * Метод для обновления.
     *
     * @return
     */
    private Observable<Void> updateViewObservable() {

        return Observable
                .create((Subscriber<? super Void> subscriber) -> {
                    Logger.trace(TAG, "updateViewObservable - start");
                    updateViewSync();
                    Logger.trace(TAG, "updateViewObservable - end");
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                })
                .observeOn(SchedulersCPPK.background())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Метод для вызова с ProgressBar.
     *
     * @param voidObservable
     */
    private void callObservableWithProgressBar(Observable<Void> voidObservable) {
        if (isProcessing || progressDialog.isShowing()) {
            throw new IllegalStateException("another operation already is run");
        }
        isProcessing = true;
        progressDialog.show();
        Logger.trace(TAG, "Show Progress");
        voidObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Logger.trace(TAG, "Hide Progress");
                        progressDialog.dismiss();
                        // Костыль, чтоб успели за это время сработать асинхронные AdapterView.OnItemSelectedListener
                        getView().postDelayed(() -> isProcessing = false, 64);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.trace(TAG, "Hide Progress");
                        progressDialog.dismiss();
                        Logger.error(TAG, e.getMessage(), e);
                        // Костыль, чтоб успели за это время сработать асинхронные AdapterView.OnItemSelectedListener
                        getView().postDelayed(() -> isProcessing = false, 64);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        Logger.trace(TAG, "onNext Progress");
                    }
                });
    }

    private List<Station> getDepartureStations(@NonNull String filter) {

        final String likeQuery = filter.toUpperCase(Locale.getDefault());
        List<Station> stations = new ArrayList<>();

        if ("".equals(filter) && mDepartureStationsWithoutFilter != null && destinationStation == null) {
            // Используем кешированный список
            return mDepartureStationsWithoutFilter;
        } else {
            stations = pdSaleEnv.depStationsLoader().loadDirectStations(
                    null,
                    destinationStation == null ? null : (long) destinationStation.getCode(),
                    likeQuery
            );
            if ("".equals(filter) && destinationStation == null) {
                // Кешируем список станций
                mDepartureStationsWithoutFilter = stations;
            }
        }
        return stations;
    }

    private List<Station> getDestinationStations(@NonNull String filter) {

        final String likeQuery = filter.toUpperCase(Locale.getDefault());
        List<Station> stations = new ArrayList<>();

        if ("".equals(filter) && mDestinationStationsWithoutFilter != null && departureStation == null) {
            // Используем кешированный список
            return mDestinationStationsWithoutFilter;
        } else {
            stations = pdSaleEnv.destStationsLoader().loadDirectStations(
                    departureStation == null ? null : (long) departureStation.getCode(),
                    null,
                    likeQuery
            );
            if ("".equals(filter) && departureStation == null) {
                // Кешируем список станций
                mDestinationStationsWithoutFilter = stations;
            }
        }
        return stations;
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize() START");
        Completable
                .fromAction(() -> {
                    di().uiThread().post(() -> progressDialog.show());
                    getDepartureStations("");
                    getDestinationStations("");
                    di().uiThread().post(() -> progressDialog.dismiss());
                    Logger.trace(TAG, "onInitialize() FINISH");
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }
}

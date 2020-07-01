package ru.ppr.cppk.ui.activity.nsiQueryTest;

import android.os.Bundle;
import android.util.LongSparseArray;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TariffPlan;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Тестирование запросов к nsi из пакета {@link ru.ppr.nsi.query}
 *
 * @author Dmitry Nevolin
 */
public class NsiQueryTestActivity extends LoggedActivity {
    /**
     * DI
     */
    private NsiQueryTestDi di;
    /**
     * Необходим для формирование запросов
     */
    private PdSaleEnv pdSaleEnv;
    /**
     * Результаты поиска станций отправления
     */
    private LongSparseArray<Long> departureStationsTestResults;
    /**
     * Результаты поиска станций назначения
     */
    private LongSparseArray<Long> destinationStationsTestResults;
    /**
     * Результаты поиска тарифов
     */
    private Map<Pair<Long, Long>, Long> tariffsTestResults;
    /**
     * Адаптер теста станций
     */
    private StationsAdapter stationsAdapter;
    /**
     * Подписка на результат поиска любого из тестов
     */
    private Subscription subscription;
    // Views
    private View runDepartureStationsTestButton;
    private View runDestinationStationsTestButton;
    private View runDirectTariffsTestButton;
    private View runTransitTariffsTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nsi_query_test);

        di = new NsiQueryTestDi(Di.INSTANCE);
        pdSaleEnv = di.pdSaleEnvFactory().pdSaleEnvForSinglePd();
        pdSaleEnv.pdSaleRestrictions().update(Dagger.appComponent().pdSaleRestrictionsParamsBuilder().create(new Date(), di.nsiVersionManager().getCurrentNsiVersionId()));

        departureStationsTestResults = new LongSparseArray<>();
        destinationStationsTestResults = new LongSparseArray<>();
        tariffsTestResults = new HashMap<>();
        stationsAdapter = new StationsAdapter();

        ((ListView) findViewById(R.id.test_out)).setAdapter(stationsAdapter);

        runDepartureStationsTestButton = findViewById(R.id.run_departure_stations_test);
        runDepartureStationsTestButton.setOnClickListener(view -> {
            prepareTestOut();

            List<Pair<Station, Station>> destinationStationsForTest = getDestinationStationsForTest();

            stationsAdapter.setItems(destinationStationsForTest);

            runDepartureStationsTest(destinationStationsForTest);
        });

        runDestinationStationsTestButton = findViewById(R.id.run_destination_stations_test);
        runDestinationStationsTestButton.setOnClickListener(view -> {
            prepareTestOut();

            List<Pair<Station, Station>> departureStationsForTest = getDepartureStationsForTest();

            stationsAdapter.setItems(departureStationsForTest);

            runDestinationStationsTest(departureStationsForTest);
        });

        runDirectTariffsTestButton = findViewById(R.id.run_direct_tariffs_test);
        runDirectTariffsTestButton.setOnClickListener(view -> {
            prepareTestOut();

            List<Pair<Station, Station>> stationsForDirectTariffsTest = getStationsForDirectTariffsTest();

            stationsAdapter.setItems(stationsForDirectTariffsTest);

            runDirectTariffsTest(stationsForDirectTariffsTest, getTestTariffPlan());
        });

        runTransitTariffsTestButton = findViewById(R.id.run_transit_tariffs_test);
        runTransitTariffsTestButton.setOnClickListener(view -> {
            prepareTestOut();

            List<Pair<Station, Station>> stationsForTransitTariffsTest = getStationsForTransitTariffsTest();

            stationsAdapter.setItems(stationsForTransitTariffsTest);

            runTransitTariffsTest(stationsForTransitTariffsTest, getTestTariffPlan());
        });
    }

    @Override
    protected void onPause() {
        if (subscription != null && subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        super.onPause();
    }

    private void prepareTestOut() {
        runDepartureStationsTestButton.setEnabled(false);
        runDestinationStationsTestButton.setEnabled(false);
        runDirectTariffsTestButton.setEnabled(false);
        runTransitTariffsTestButton.setEnabled(false);

        destinationStationsTestResults.clear();
        departureStationsTestResults.clear();
        tariffsTestResults.clear();
    }

    private void onTestCompleted() {
        runDepartureStationsTestButton.setEnabled(true);
        runDestinationStationsTestButton.setEnabled(true);
        runDirectTariffsTestButton.setEnabled(true);
        runTransitTariffsTestButton.setEnabled(true);
    }

    private List<Pair<Station, Station>> getDepartureStationsForTest() {
        int version = di.nsiVersionManager().getCurrentNsiVersionId();

        Station station1 = new Station();
        station1.setCode(2000145);
        station1.setShortName("ДОМОДЕДОВО");
        station1.setVersionId(version);

        Station station2 = new Station();
        station2.setCode(2001138);
        station2.setShortName("АКРИ");
        station2.setVersionId(version);

        Station station3 = new Station();
        station3.setCode(2000005);
        station3.setShortName("МОСКВА ПАВ");
        station3.setVersionId(version);

        Station station4 = new Station();
        station4.setCode(2001750);
        station4.setShortName("НИЖН КОТЛЫ");
        station4.setVersionId(version);

        Station station5 = new Station();
        station5.setCode(2000093);
        station5.setShortName("УЗУНОВО");
        station5.setVersionId(version);

        Station station6 = new Station();
        station6.setCode(-1);
        station6.setShortName("ЛЮБАЯ");
        station6.setVersionId(version);

        return Arrays.asList(
                Pair.create(station1, null),
                Pair.create(station2, null),
                Pair.create(station3, null),
                Pair.create(station4, null),
                Pair.create(station5, null),
                Pair.create(station6, null));
    }

    private List<Pair<Station, Station>> getDestinationStationsForTest() {
        int version = di.nsiVersionManager().getCurrentNsiVersionId();

        Station station1 = new Station();
        station1.setCode(2000505);
        station1.setShortName("АВИАЦИОННАЯ");
        station1.setVersionId(version);

        Station station2 = new Station();
        station2.setCode(2001945);
        station2.setShortName("ЧЕРТАНОВО");
        station2.setVersionId(version);

        Station station3 = new Station();
        station3.setCode(2001156);
        station3.setShortName("БУЛАТНИКОВ");
        station3.setVersionId(version);

        Station station4 = new Station();
        station4.setCode(2001050);
        station4.setShortName("КОЛОМЕНСК");
        station4.setVersionId(version);

        Station station5 = new Station();
        station5.setCode(2000088);
        station5.setShortName("ОЖЕРЕЛЬЕ");
        station5.setVersionId(version);

        Station station6 = new Station();
        station6.setCode(-1);
        station6.setShortName("ЛЮБАЯ");
        station6.setVersionId(version);

        return Arrays.asList(
                Pair.create(null, station1),
                Pair.create(null, station2),
                Pair.create(null, station3),
                Pair.create(null, station4),
                Pair.create(null, station5),
                Pair.create(null, station6));
    }

    // Direct tariffs

    private List<Station> getDepartureStationsForDirectTariffsTest() {
        int version = di.nsiVersionManager().getCurrentNsiVersionId();

        Station station1 = new Station();
        station1.setCode(2000145);
        station1.setShortName("ДОМОДЕДОВО");
        station1.setVersionId(version);

        Station station2 = new Station();
        station2.setCode(2001138);
        station2.setShortName("АКРИ");
        station2.setVersionId(version);

        Station station3 = new Station();
        station3.setCode(2000005);
        station3.setShortName("МОСКВА ПАВ");
        station3.setVersionId(version);

        Station station4 = new Station();
        station4.setCode(2001750);
        station4.setShortName("НИЖН КОТЛЫ");
        station4.setVersionId(version);

        Station station5 = new Station();
        station5.setCode(2000093);
        station5.setShortName("УЗУНОВО");
        station5.setVersionId(version);

        return Arrays.asList(
                station1,
                station2,
                station3,
                station4,
                station5);
    }

    private List<Station> getDestinationStationsForDirectTariffsTest() {
        int version = di.nsiVersionManager().getCurrentNsiVersionId();

        Station station1 = new Station();
        station1.setCode(2000505);
        station1.setShortName("АВИАЦИОННАЯ");
        station1.setVersionId(version);

        Station station2 = new Station();
        station2.setCode(2001945);
        station2.setShortName("ЧЕРТАНОВО");
        station2.setVersionId(version);

        Station station3 = new Station();
        station3.setCode(2001156);
        station3.setShortName("БУЛАТНИКОВ");
        station3.setVersionId(version);

        Station station4 = new Station();
        station4.setCode(2001050);
        station4.setShortName("КОЛОМЕНСК");
        station4.setVersionId(version);

        Station station5 = new Station();
        station5.setCode(2000088);
        station5.setShortName("ОЖЕРЕЛЬЕ");
        station5.setVersionId(version);

        return Arrays.asList(
                station1,
                station2,
                station3,
                station4,
                station5);
    }

    private List<Pair<Station, Station>> getStationsForDirectTariffsTest() {
        List<Station> departureStationsForDirectTariffsTest = getDepartureStationsForDirectTariffsTest();
        List<Station> destinationStationsForDirectTariffsTest = getDestinationStationsForDirectTariffsTest();

        List<Pair<Station, Station>> stationsForTariffsTest = new ArrayList<>(departureStationsForDirectTariffsTest.size());

        for (int i = 0; i < departureStationsForDirectTariffsTest.size(); i++) {
            stationsForTariffsTest.add(Pair.create(
                    departureStationsForDirectTariffsTest.get(i),
                    destinationStationsForDirectTariffsTest.get(i)));
        }

        return stationsForTariffsTest;
    }

    // Transit tariffs

    private List<Station> getDepartureStationsForTransitTariffsTest() {
        int version = di.nsiVersionManager().getCurrentNsiVersionId();

        Station station1 = new Station();
        station1.setCode(2000145);
        station1.setShortName("ДОМОДЕДОВО");
        station1.setVersionId(version);

        Station station2 = new Station();
        station2.setCode(2001138);
        station2.setShortName("АКРИ");
        station2.setVersionId(version);

        Station station3 = new Station();
        station3.setCode(2000005);
        station3.setShortName("МОСКВА ПАВ");
        station3.setVersionId(version);

        Station station4 = new Station();
        station4.setCode(2001750);
        station4.setShortName("НИЖН КОТЛЫ");
        station4.setVersionId(version);

        Station station5 = new Station();
        station5.setCode(2000093);
        station5.setShortName("УЗУНОВО");
        station5.setVersionId(version);

        return Arrays.asList(
                station1,
                station2,
                station3,
                station4,
                station5);
    }

    private List<Station> getDestinationStationsForTransitTariffsTest() {
        int version = di.nsiVersionManager().getCurrentNsiVersionId();

        Station station1 = new Station();
        station1.setCode(2001945);
        station1.setShortName("ЧЕРТАНОВО");
        station1.setVersionId(version);

        Station station2 = new Station();
        station2.setCode(2000505);
        station2.setShortName("АВИАЦИОННАЯ");
        station2.setVersionId(version);

        Station station3 = new Station();
        station3.setCode(2001156);
        station3.setShortName("БУЛАТНИКОВ");
        station3.setVersionId(version);

        Station station4 = new Station();
        station4.setCode(2001050);
        station4.setShortName("КОЛОМЕНСК");
        station4.setVersionId(version);

        Station station5 = new Station();
        station5.setCode(2000088);
        station5.setShortName("ОЖЕРЕЛЬЕ");
        station5.setVersionId(version);

        return Arrays.asList(
                station1,
                station2,
                station3,
                station4,
                station5);
    }

    private List<Pair<Station, Station>> getStationsForTransitTariffsTest() {
        List<Station> departureStationsForTransitTariffsTest = getDepartureStationsForTransitTariffsTest();
        List<Station> destinationStationsForTransitTariffsTest = getDestinationStationsForTransitTariffsTest();

        List<Pair<Station, Station>> stationsForTariffsTest = new ArrayList<>(departureStationsForTransitTariffsTest.size());

        for (int i = 0; i < departureStationsForTransitTariffsTest.size(); i++) {
            stationsForTariffsTest.add(Pair.create(
                    departureStationsForTransitTariffsTest.get(i),
                    destinationStationsForTransitTariffsTest.get(i)));
        }

        return stationsForTariffsTest;
    }

    private TariffPlan getTestTariffPlan() {
        int version = di.nsiVersionManager().getCurrentNsiVersionId();

        TariffPlan tariffPlan = new TariffPlan();
        tariffPlan.setCode(1);
        tariffPlan.setVersionId(version);

        return tariffPlan;
    }

    private Long getTestResult(Pair<Station, Station> pair) {
        if (pair.first != null && pair.second == null) {
            // Берем результат поиска станции назначения по заданной станции отправления
            return destinationStationsTestResults.get(Long.valueOf(pair.first.getCode()));
        } else if (pair.first == null && pair.second != null) {
            // Берем результат поиска станции отправления по заданной станции назначения
            return departureStationsTestResults.get(Long.valueOf(pair.second.getCode()));
        } else if (pair.first != null) {
            return tariffsTestResults.get(Pair.create(Long.valueOf(pair.first.getCode()), Long.valueOf(pair.second.getCode())));
        } else {
            return null;
        }
    }

    // Tests

    private void runDepartureStationsTest(List<Pair<Station, Station>> destinationStationsForTest) {
        subscription = Observable
                .from(destinationStationsForTest)
                .flatMap(pair -> {
                    di.uiThread().post(() -> stationsAdapter.setWaitForItemResult(pair));

                    Station station = pair.second;

                    long startTime = System.currentTimeMillis();
                    int nsiVersion = di.nsiVersionManager().getCurrentNsiVersionId();

                    // Метод удален, как не используемый в реальной работе
                    // Альтернатива - {@code link TariffRepository#loadTariffsForSaleQuery}
//                    di.stationRepository().loadStationsWithTariffs(
//                            pdSaleEnv.pdSaleRestrictions().getAllowedProductionSectionCodes(),
//                            false,
//                            -1,
//                            station.code,
//                            pdSaleEnv.pdSaleRestrictions().getAllowedTariffPlanCodes(),
//                            pdSaleEnv.pdSaleRestrictions().getAllowedTicketTypeCodes(),
//                            pdSaleEnv.pdSaleRestrictions().getAllowedStationCodesBySettings(),
//                            "",
//                            Globals.getStationsStatistics().getRecentDepartureStationsCodes(),
//                            nsiVersion);

                    return Observable.just(Pair.create(station, System.currentTimeMillis() - startTime));
                })
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (pair) -> {
                            departureStationsTestResults.put(Long.valueOf(pair.first.getCode()), pair.second);

                            stationsAdapter.notifyDataSetChanged();
                        },
                        (error) -> {
                        },
                        this::onTestCompleted);
    }

    private void runDestinationStationsTest(List<Pair<Station, Station>> departureStationsForTest) {
        subscription = Observable
                .from(departureStationsForTest)
                .flatMap(pair -> {
                    di.uiThread().post(() -> stationsAdapter.setWaitForItemResult(pair));

                    Station station = pair.first;

                    long startTime = System.currentTimeMillis();
                    int nsiVersion = di.nsiVersionManager().getCurrentNsiVersionId();

                    // Метод удален, как не используемый в реальной работе
                    // Альтернатива - {@code link TariffRepository#loadTariffsForSaleQuery}
//                    di.stationRepository().loadStationsWithTariffs(
//                            pdSaleEnv.pdSaleRestrictions().getAllowedProductionSectionCodes(),
//                            false,
//                            -1,
//                            station.code,
//                            pdSaleEnv.pdSaleRestrictions().getAllowedTariffPlanCodes(),
//                            pdSaleEnv.pdSaleRestrictions().getAllowedTicketTypeCodes(),
//                            pdSaleEnv.pdSaleRestrictions().getAllowedStationCodesBySettings(),
//                            "",
//                            Globals.getStationsStatistics().getRecentDepartureStationsCodes(),
//                            nsiVersion);

                    return Observable.just(Pair.create(station, System.currentTimeMillis() - startTime));
                })
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (pair) -> {
                            destinationStationsTestResults.put(Long.valueOf(pair.first.getCode()), pair.second);

                            stationsAdapter.notifyDataSetChanged();
                        },
                        (error) -> {
                        },
                        this::onTestCompleted);
    }

    private void runDirectTariffsTest(List<Pair<Station, Station>> stationsForTariffsTest, TariffPlan tariffPlan) {
        subscription = Observable
                .from(stationsForTariffsTest)
                .flatMap(pair -> {
                    di.uiThread().post(() -> stationsAdapter.setWaitForItemResult(pair));

                    long startTime = System.currentTimeMillis();
                    int nsiVersion = di.nsiVersionManager().getCurrentNsiVersionId();
                    int ticketTypeCode = 1;

                    di.tariffRepository().loadDirectTariffs(
                            Collections.singletonList((long) pair.first.getCode()),
                            Collections.singletonList((long) pair.second.getCode()),
                            Collections.singletonList((long) ticketTypeCode),
                            Collections.singletonList(tariffPlan),
                            nsiVersion);

                    return Observable.just(Pair.create(pair, System.currentTimeMillis() - startTime));
                })
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (pair) -> {
                            tariffsTestResults.put(Pair.create(Long.valueOf(pair.first.first.getCode()), Long.valueOf(pair.first.second.getCode())), pair.second);

                            stationsAdapter.notifyDataSetChanged();
                        },
                        (error) -> {
                        },
                        this::onTestCompleted);
    }

    private void runTransitTariffsTest(List<Pair<Station, Station>> stationsForTariffsTest, TariffPlan tariffPlan) {
        subscription = Observable
                .from(stationsForTariffsTest)
                .flatMap(pair -> {
                    di.uiThread().post(() -> stationsAdapter.setWaitForItemResult(pair));
                    long startTime = System.currentTimeMillis();
                    // Query was deleted
                    return Observable.just(Pair.create(pair, System.currentTimeMillis() - startTime));
                })
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (pair) -> {
                            tariffsTestResults.put(Pair.create(Long.valueOf(pair.first.first.getCode()), Long.valueOf(pair.first.second.getCode())), pair.second);

                            stationsAdapter.notifyDataSetChanged();
                        },
                        (error) -> {
                        },
                        this::onTestCompleted);
    }

    private int getListPreferredItemHeight() {
        TypedValue value = new TypedValue();

        getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);

        return (int) value.getDimension(getResources().getDisplayMetrics());
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * Адаптер для запросов со станциями отправления/назначения
     */
    private class StationsAdapter extends BaseAdapter<Pair<Station, Station>> {

        private SparseBooleanArray waitForItemResult = new SparseBooleanArray();

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Pair<Station, Station> pair = getItem(i);
            Station departureStation = pair.first;
            Station destinationStation = pair.second;
            Long testResult = getTestResult(pair);

            if (view == null) {
                view = createView();
            }

            LinearLayout layout = (LinearLayout) view;

            ((TextView) layout.getChildAt(0)).setText(departureStation == null ? "" : departureStation.getShortName());
            ((TextView) layout.getChildAt(1)).setText(destinationStation == null ? "" : destinationStation.getShortName());

            FrameLayout timeLayout = (FrameLayout) layout.getChildAt(2);
            TextView time = ((TextView) timeLayout.getChildAt(0));
            ProgressBar progressBar = ((ProgressBar) timeLayout.getChildAt(1));

            if (testResult == null) {
                progressBar.setVisibility(getWaitForItemResult(pair) ? View.VISIBLE : View.GONE);
                time.setText("");
            } else {
                progressBar.setVisibility(View.GONE);
                time.setText(testResult + " ms");
            }

            return view;
        }

        @Override
        public void setItems(List<Pair<Station, Station>> items) {
            waitForItemResult.clear();

            super.setItems(items);
        }

        private View createView() {
            LinearLayout linearLayout = new LinearLayout(NsiQueryTestActivity.this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getListPreferredItemHeight()));

            TextView departureStation = new TextView(NsiQueryTestActivity.this);
            departureStation.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(140), ViewGroup.LayoutParams.MATCH_PARENT));
            departureStation.setGravity(Gravity.CENTER);

            TextView destinationStation = new TextView(NsiQueryTestActivity.this);
            destinationStation.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(140), ViewGroup.LayoutParams.MATCH_PARENT));
            destinationStation.setGravity(Gravity.CENTER);
            destinationStation.setBackgroundColor(getResources().getColor(R.color.gray_2));

            FrameLayout frameLayout = new FrameLayout(NsiQueryTestActivity.this);
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(80), ViewGroup.LayoutParams.MATCH_PARENT));

            TextView time = new TextView(NsiQueryTestActivity.this);
            time.setLayoutParams(new FrameLayout.LayoutParams(dpToPx(80), ViewGroup.LayoutParams.MATCH_PARENT));
            time.setGravity(Gravity.CENTER);
            ((FrameLayout.LayoutParams) time.getLayoutParams()).gravity = Gravity.CENTER;

            ProgressBar progressBar = new ProgressBar(NsiQueryTestActivity.this);
            progressBar.setLayoutParams(new FrameLayout.LayoutParams(getListPreferredItemHeight(), ViewGroup.LayoutParams.MATCH_PARENT));
            progressBar.setIndeterminate(true);
            ((FrameLayout.LayoutParams) progressBar.getLayoutParams()).gravity = Gravity.CENTER;

            frameLayout.addView(time);
            frameLayout.addView(progressBar);

            linearLayout.addView(departureStation);
            linearLayout.addView(destinationStation);
            linearLayout.addView(frameLayout);

            return linearLayout;
        }

        private void setWaitForItemResult(Pair<Station, Station> pair) {
            if (pair.first != null && pair.second == null) {
                waitForItemResult.put(pair.first.getCode(), true);
            } else if (pair.first == null && pair.second != null) {
                waitForItemResult.put(pair.second.getCode(), true);
            } else if (pair.first != null) {
                waitForItemResult.put(getItems().indexOf(pair), true);
            }

            notifyDataSetChanged();
        }

        private boolean getWaitForItemResult(Pair<Station, Station> pair) {
            if (pair.first != null && pair.second == null) {
                return waitForItemResult.get(pair.first.getCode(), false);
            } else if (pair.first == null && pair.second != null) {
                return waitForItemResult.get(pair.second.getCode(), false);
            } else {
                return pair.first != null && waitForItemResult.get(getItems().indexOf(pair), false);
            }
        }

    }

}

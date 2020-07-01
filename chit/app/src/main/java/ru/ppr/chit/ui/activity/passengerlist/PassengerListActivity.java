package ru.ppr.chit.ui.activity.passengerlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.chit.ui.activity.base.delegate.TripServiceInfoDelegate;
import ru.ppr.chit.ui.activity.passengerlist.adapter.PassengerRecyclerAdapter;
import ru.ppr.chit.ui.activity.passengerlist.model.PassengerInfo;
import ru.ppr.core.ui.adapter.RecyclerItemClickListener;


/**
 * Экран со списком пассажиров.
 *
 * @author Aleksandr Brazhkin
 */
public class PassengerListActivity extends MvpActivity implements PassengerListView {

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, PassengerListActivity.class);
    }

    // region Di
    private PassengerListComponent component;
    @Inject
    PassengerRecyclerAdapter passengerRecyclerAdapter;
    @Inject
    ActivityNavigator navigator;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    TripServiceInfoDelegate tripServiceInfoDelegate;
    // endregion
    // region Views
    private RecyclerView recyclerView;
    private EditText searchView;
    //endregion
    //region Other
    private PassengerListPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerPassengerListComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::passengerListPresenter, PassengerListPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_passenger_list);

        statusBarDelegate.init(getMvpDelegate());
        tripServiceInfoDelegate.init(getMvpDelegate());

        searchView = (EditText) findViewById(R.id.searchView);
        searchView.addTextChangedListener(queryTextChangedListener);
        ///////////////////////////////////////////////////////////////////////////////////////
        passengerRecyclerAdapter.setLoader(recordsOffset -> presenter.uploadList(recordsOffset));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(passengerRecyclerAdapter);
        recyclerView.addItemDecoration(new
                DividerItemDecoration(PassengerListActivity.this,
                DividerItemDecoration.VERTICAL));
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, (view, position) -> {
            presenter.onPassengerItemClicked(passengerRecyclerAdapter.getItem(position));
        }));
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(passengerListNavigator);
        presenter.initialize();
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    private TextWatcher queryTextChangedListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            presenter.onQueryTextChanged(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private final PassengerListPresenter.Navigator passengerListNavigator = new PassengerListPresenter.Navigator() {
        @Override
        public void navigateToTicketControl(long ticketId) {
            navigator.navigateToFromListTicketControl(ticketId);
        }
    };

    @Override
    public void reloadPassengers(){
        presenter.reloadList();
    }

    @Override
    public void updatePassengers(List<PassengerInfo> passengers, int positionFrom, boolean hasMoreData){
        passengerRecyclerAdapter.uploadItems(passengers, positionFrom, hasMoreData);
    }
}

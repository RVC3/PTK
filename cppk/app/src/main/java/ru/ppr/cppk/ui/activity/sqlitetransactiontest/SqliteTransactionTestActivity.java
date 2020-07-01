package ru.ppr.cppk.ui.activity.sqlitetransactiontest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.ui.activity.base.MvpActivity;

/**
 * @author Dmitry Nevolin
 */
public class SqliteTransactionTestActivity extends MvpActivity implements SqliteTransactionTestView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SqliteTransactionTestActivity.class);
    }

    private SqliteTransactionTestComponent component;
    private SqliteTransactionTestPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerSqliteTransactionTestComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sqlite_transaction_test);

        findViewById(R.id.test_1_1).setOnClickListener(v -> presenter.test11());
        findViewById(R.id.test_1_2).setOnClickListener(v -> presenter.test12());
        findViewById(R.id.test_1_3).setOnClickListener(v -> presenter.test13());
        findViewById(R.id.test_2_1).setOnClickListener(v -> presenter.test21());
        findViewById(R.id.test_2_2).setOnClickListener(v -> presenter.test22());

        presenter = getMvpDelegate().getPresenter(component::sqliteTransactionTestPresenter, SqliteTransactionTestPresenter.class);
        presenter.initialize();
    }

}

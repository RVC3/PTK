package ru.ppr.cppk.settings.inputs;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ru.ppr.cppk.R;
import ru.ppr.logger.Logger;
import ru.ppr.cppk.systembar.SystemBarActivity;

/**
 * Активити для ввода различных настроек. Например ввод кода дня, количества дней действия стоп-листов.
 *
 * @author Artem Ushakov
 */
public class InputDataActivity extends SystemBarActivity {

    public enum ChangeAction {COUNT_STOP_LIST_ACTION_DAY, ANNUL_PD_TIME, ATTENTION_CLOSE_SHIFT_TIME, PTK_NUMBER}

    private static final String TAG = "InputDataActivity";
    private static final String CHANGE_ACTION = "CHANGE_ACTION";

    public static Intent getNewIntent(Context context, ChangeAction action) {

        Intent intent = new Intent(context, InputDataActivity.class);
        intent.putExtra(CHANGE_ACTION, action);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_activity);

        ChangeAction action = (ChangeAction) getIntent().getSerializableExtra(CHANGE_ACTION);

        Fragment fragment;
        switch (action) {
            case COUNT_STOP_LIST_ACTION_DAY:
                fragment = SetStopListDayActionFragment.newInstance();
                break;

            case ANNUL_PD_TIME:
                fragment = SetTimeForCancellation.newInstance();
                break;

            case ATTENTION_CLOSE_SHIFT_TIME:
                fragment = SetCloseShiftAttentionPeriodFragment.newInstance();
                break;

            case PTK_NUMBER:
                fragment = SetPtkNumberFragment.newInstance();
                break;

            default:
                fragment = null;
                break;
        }

        if (fragment != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_activity_container, fragment).commit();
        } else {
            Logger.trace(TAG, "Fragment is null, finish activity");
            finish();
        }
    }
}

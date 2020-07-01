package ru.ppr.cppk.ui.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.fragment.EnterDayCodeFragment;

/**
 * Created by Александр on 29.12.2015.
 */
public class EnterDayCodeActivity extends SystemBarActivity implements
        EnterDayCodeFragment.OnFragmentInteractionListener {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, EnterDayCodeActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_day_code_activity);

        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = EnterDayCodeFragment.newInstance();
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer,
                fragment, EnterDayCodeFragment.FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void onDayCodeChanged() {
        finish();
    }
}

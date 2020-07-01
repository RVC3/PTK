package ru.ppr.cppk.ui.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ru.ppr.cppk.R;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.fragment.OpenShiftSettingsFragment;
import ru.ppr.cppk.ui.fragment.OpenShiftStartFragment;

public class OpenShiftActivity extends SystemBarActivity implements
        OpenShiftStartFragment.OnFragmentInteractionListener,
        OpenShiftSettingsFragment.OnFragmentInteractionListener {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, OpenShiftActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    OpenShiftStartFragment openShiftStartFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_shift_activity);

        showOpenShift();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof OpenShiftStartFragment) {
            openShiftStartFragment = (OpenShiftStartFragment) fragment;
        }
    }

    private void showOpenShift() {
        denyScreenLock();
        if (ShiftManager.getInstance().isShiftOpened()) {
            onShiftOpened();
        } else {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainerOpenShiftActivity,
                    OpenShiftStartFragment.newInstance());
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            return;
        }

        if (openShiftStartFragment != null && openShiftStartFragment.onBackPressed()) {
            return;
        }

        Navigator.navigateToWelcomeActivity(this, false);
    }

    @Override
    /** Переопределяем обработчик чтобы запретить переход на главное меню по нажатию на кнопку Settings*/
    public void onClickSettings() {

    }

    @Override
    public void onShiftOpened() {
        allowScreenLock();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerOpenShiftActivity, OpenShiftSettingsFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public void onChangeDayCode() {
        Navigator.navigateToEnterDayCodeActivity(this);
    }
}

package ru.ppr.cppk.statistics;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import ru.ppr.cppk.R;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogClickListener;
import ru.ppr.logger.Logger;
import ru.ppr.cppk.statistics.StatisticsFragment.OnFragmentInteraction;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;

public class StatisticsActivity extends SystemBarActivity implements
        OnFragmentInteraction, CppkDialogClickListener,
        TariffsInfoFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_activity);

        Fragment statFragment = StatisticsFragment.newInstance();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.statistics_fragment_container, statFragment).commit();
    }


    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.statistics_fragment_container, fragment).addToBackStack(null).commit();
    }


    @Override
    public void showSell() {
        replaceFragment(StatisticSellForLastShiftFragment.newInstance());
    }


    @Override
    public void showCheckEtt() {
        replaceFragment(StatisticsCheckETTFragment.newInstance());
    }


    @Override
    public void showTariffInfo() {
        replaceFragment(TariffsInfoFragment.newInstance());
    }


    @Override
    public void showCheckPd() {
        replaceFragment(StatisticsCheckTicketFragment.newInstance());
    }


    @Override
    public void showUpdates() {
        replaceFragment(UpdateStatisticsFragment.newInstance());
    }


    @Override
    public void onPositiveClick(DialogFragment dialog, int idDialog) {
        getFragmentManager().popBackStack();
    }


    @Override
    public void onNegativeClick(DialogFragment dialog, int idDialog) {
        /* NOP */
    }

    @Override
    public void onBackPressed() {

        FragmentManager manager = getFragmentManager();
        Fragment currentFragment = manager.findFragmentById(R.id.statistics_fragment_container);

        if (currentFragment instanceof FragmentOnBackPressed) {
            FragmentOnBackPressed fragmentOnBackPressed = (FragmentOnBackPressed) currentFragment;

            if (fragmentOnBackPressed.onBackPress()) {
                Logger.info("TEST", "onBackPress return true");
                return;
            }
        }

        if (manager.getBackStackEntryCount() == 0) {
            finish();
        } else {
            manager.popBackStack();
        }
    }
}

package ru.ppr.cppk.ui.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.math.BigDecimal;

import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.fragment.CalculateDeliveryFixedCostFragment;

/**
 * Created by Александр on 12.09.2016.
 */
public class CalculateDeliveryFixedCostActivity extends SystemBarActivity {

    // EXTRAS
    private static final String EXTRA_COST_PD = "EXTRA_COST_PD";

    public static Intent getCallingIntent(Context context, BigDecimal costPD) {
        Intent intent = new Intent(context, CalculateDeliveryFixedCostActivity.class);
        intent.putExtra(EXTRA_COST_PD, costPD);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        BigDecimal costPD = BigDecimal.ZERO;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            costPD = (BigDecimal) extras.getSerializable(EXTRA_COST_PD);
        }

        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = CalculateDeliveryFixedCostFragment.newInstance(costPD);
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}

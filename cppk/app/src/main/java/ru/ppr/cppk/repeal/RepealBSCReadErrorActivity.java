package ru.ppr.cppk.repeal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.fragment.pd.invalid.ErrorFragment;
import ru.ppr.cppk.pd.utils.ValidityPdVariants;
import ru.ppr.cppk.systembar.SystemBarActivity;

/**
 * Created by Dmitry Nevolin on 01.03.2016.
 */
public class RepealBSCReadErrorActivity extends SystemBarActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, RepealBSCReadErrorActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.repeal_bsc_read_error_activity);

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, ErrorFragment.newInstance(ValidityPdVariants.ONE_PD_VALID, ErrorFragment.Errors.NO_TICKET_FOR_CANCEL, false)).commit();

        findViewById(R.id.ok).setOnClickListener(v -> finish());
    }

}

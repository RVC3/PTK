package ru.ppr.cppk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;

/**
 * Created by Dmitry Nevolin on 30.03.2016.
 */
public class ActivityTicketTapeIsNotSet extends SystemBarActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, ActivityTicketTapeIsNotSet.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ticket_tape_is_not_set);

        findViewById(R.id.cancel).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.set_ticket_tape).setOnClickListener(v -> {
            Navigator.navigateToAccountingTicketTapeStartActivity(ActivityTicketTapeIsNotSet.this, true);

            finish();
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}

package ru.ppr.cppk.settings;

import android.os.Bundle;
import android.widget.TextView;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.systembar.SystemBarActivity;

public class UserInfoActivity extends SystemBarActivity {

    private Globals g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_user_info);
        g = (Globals) getApplication();

        TextView textView = (TextView) findViewById(R.id.tvS3_6_UserName);
        textView.setText(Di.INSTANCE.getUserSessionInfo().getCurrentUser().getName());
        textView = (TextView) findViewById(R.id.tvS3_6_UserRole);
        textView.setText(Di.INSTANCE.getUserSessionInfo().getCurrentUser().getRole().getName());

    }
}

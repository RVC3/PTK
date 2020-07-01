package ru.ppr.cppk.settings;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.core.ui.widget.SimpleLseView;

public class ReportsActivity extends SystemBarActivity implements
        ReportsShiftFragment.OnFragmentInteractionListener,
        ReportsMonthFragment.OnFragmentInteractionListener {

    public static final String SHIFT = "shift";
    public static final String MONTH = "month";

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, ReportsActivity.class);
        return intent;
    }

    private SimpleLseView simpleLseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reports);

        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        createTabHost();
    }

    private void createTabHost() {
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec spec;
        spec = tabHost.newTabSpec(SHIFT);

        View tabHostHeader = getLayoutInflater().inflate(R.layout.tab_host_header, null);
        final TextView title1 = (TextView) tabHostHeader.findViewById(R.id.tvTabHostTitle);
        title1.setText(R.string.reports_shift);
        title1.setTextColor(getResources().getColor(R.color.white));
        title1.setBackground(getResources().getDrawable(R.drawable.top_blue_corners));

        spec.setIndicator(tabHostHeader);
        spec.setContent(R.id.shift);
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec(MONTH);
        View tabHostHeader2 = getLayoutInflater().inflate(R.layout.tab_host_header, null);
        final TextView title2 = (TextView) tabHostHeader2.findViewById(R.id.tvTabHostTitle);
        title2.setText(R.string.reports_month);
        title2.setBackgroundColor(getResources().getColor(R.color.black));
        title2.setTextColor(getResources().getColor(R.color.white));
        spec.setIndicator(tabHostHeader2);
        spec.setContent(R.id.month);
        tabHost.addTab(spec);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals(SHIFT)) {
                    title1.setBackground(getResources().getDrawable(R.drawable.top_blue_corners));
                    title2.setBackgroundColor(getResources().getColor(R.color.black));
                } else {
                    title2.setBackground(getResources().getDrawable(R.drawable.top_blue_corners));
                    title1.setBackgroundColor(getResources().getColor(R.color.black));
                }
            }
        });
        tabHost.setCurrentTabByTag(SHIFT);

        Fragment shiftFragment = new ReportsShiftFragment();
        Fragment monthFragment = new ReportsMonthFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.month, monthFragment).add(R.id.shift, shiftFragment).commit();
    }

    @Override
    public void hideErrorView() {
        simpleLseView.hide();
    }

    @Override
    public void showErrorView(SimpleLseView.State errorViewState) {
        simpleLseView.setState(errorViewState);
        simpleLseView.show();
    }
}

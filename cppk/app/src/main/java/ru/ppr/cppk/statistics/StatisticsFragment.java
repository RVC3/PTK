package ru.ppr.cppk.statistics;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;

public class StatisticsFragment extends FragmentParent implements OnClickListener {

    public static Fragment newInstance() {
        return new StatisticsFragment();
    }

    public interface OnFragmentInteraction {
        void showSell();

        void showCheckEtt();

        void showTariffInfo();

        void showCheckPd();

        void showUpdates();
    }

    private OnFragmentInteraction listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteraction) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Activity " + activity.getClass().getName() + " must implement StatisticsFragment#OnFragmentInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.statistics_fragment, null);

        if (view == null)
            return super.onCreateView(inflater, container, savedInstanceState);

        Button ettButton = (Button) view.findViewById(R.id.statistics_check_ett_for_last_shift);
        ettButton.setOnClickListener(this);

        Button sellPdButton = (Button) view.findViewById(R.id.statistics_sell_pd_for_last_shift);
        sellPdButton.setOnClickListener(this);

        Button tarriffButton = (Button) view.findViewById(R.id.statistics_tariffs);
        tarriffButton.setOnClickListener(this);

        Button checkPdButton = (Button) view.findViewById(R.id.statistics_check_pd_for_last_shit);
        checkPdButton.setOnClickListener(this);

        Button updatesButton = (Button) view.findViewById(R.id.statistics_updates_ptk);
        updatesButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.statistics_check_ett_for_last_shift:

                if (listener != null) {
                    listener.showCheckEtt();
                }
                break;

            case R.id.statistics_check_pd_for_last_shit:
                if (listener != null) {
                    listener.showCheckPd();
                }
                break;

            case R.id.statistics_sell_pd_for_last_shift:
                if (listener != null) {
                    listener.showSell();
                }
                break;

            case R.id.statistics_tariffs:
                if (listener != null) {
                    listener.showTariffInfo();
                }
                break;

            case R.id.statistics_updates_ptk:
                if (listener != null) {
                    listener.showUpdates();
                }
                break;

            default:
                break;
        }

    }

}

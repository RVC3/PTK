package ru.ppr.cppk.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.activity.base.Navigator;

public class CalculateDeliveryFixedCostFragment extends FragmentParent {

    // ARGS
    private static final String ARG_PD_COST = "ARG_PD_COST";

    private EditText cash;
    private TextView cost;
    private TextView delivery;

    private BigDecimal costPd = BigDecimal.ZERO;

    public static CalculateDeliveryFixedCostFragment newInstance(BigDecimal costPD) {
        CalculateDeliveryFixedCostFragment fragment = new CalculateDeliveryFixedCostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PD_COST, costPD);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            costPd = (BigDecimal) args.getSerializable(ARG_PD_COST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calculate_delivery_fixed_cost, container, false);

        cash = (EditText) view.findViewById(R.id.cash);
        cash.addTextChangedListener(textWatcher);
        cash.setOnKeyListener((v, keyCode, event) -> {
            boolean result = false;

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                Navigator.navigateToMenuActivity(Globals.getInstance());
                result = true;
            }

            return result;
        });

        cost = (TextView) view.findViewById(R.id.calculate_delivery_cost_pd_value);
        cost.setText(String.format(getString(R.string.rub_cent_as_single), costPd));

        delivery = (TextView) view.findViewById(R.id.calculate_delivery_value);

        return view;
    }

    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String cashString = s.toString().trim();

            if (!TextUtils.isEmpty(cashString)) {
                if (cashString.equals(".")) {
                    cashString = "0.";
                    cash.setText(cashString);
                    cash.setSelection(cashString.length());
                } else {
                    BigDecimal cash = new BigDecimal(cashString);

                    if (cash.compareTo(costPd) == 1) {
                        delivery.setText(String.format(getString(R.string.rub_cent_as_single), cash.subtract(costPd)));
                    } else {
                        delivery.setText("");
                    }
                }
            } else {
                delivery.setText("");
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            /* NOP */
        }

        @Override
        public void afterTextChanged(Editable s) {
            /* NOP */
        }
    };

}

package ru.ppr.cppk.ui.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import ru.ppr.cppk.R;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogClickListener;
import ru.ppr.cppk.listeners.SellNewPdOnClickListener;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.nsi.entity.TicketCategory;

/**
 * Экран расчета сдачи.
 *
 * @author Artem Ushakov
 */
public class CalculateDeliveryActivity extends SystemBarActivity implements TextWatcher, InputFilter, CppkDialogClickListener {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, CalculateDeliveryActivity.class);
        return intent;
    }

    private NumberDetectBaseViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_delivery);
        viewHolder = new NumberDetectBaseViewHolder();
        viewHolder.change = (TextView) findViewById(R.id.change);
        viewHolder.price = (EditText) findViewById(R.id.price);
        viewHolder.sum = (EditText) findViewById(R.id.sum);

        viewHolder.price.addTextChangedListener(this);
        viewHolder.sum.addTextChangedListener(this);
        viewHolder.price.setFilters(new InputFilter[]{this});
        viewHolder.sum.setFilters(new InputFilter[]{this});

        findViewById(R.id.sell_new_pd).setOnClickListener(new SellNewPdOnClickListener(this));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        double sum = 0;
        try {
            sum = Double.parseDouble(viewHolder.sum.getText().toString());
        } catch (NumberFormatException e) {

        }

        double price = 0;
        try {
            price = Double.parseDouble(viewHolder.price.getText().toString());
        } catch (NumberFormatException e) {

        }

        double change = sum - price;

        change = change < 0 ? 0 : change;

        viewHolder.change.setText(String.format(getString(R.string.rub_cent_as_single), change));
    }

    class NumberDetectBaseViewHolder {
        TextView change;
        EditText sum;
        EditText price;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        String dst1 = dest.subSequence(0, dstart).toString();
        String dst2 = dest.subSequence(dend, dest.length()).toString();
        String src = source.subSequence(start, end).toString();

        String res = dst1 + src + dst2;
        if (res.matches("\\d{0,6}(?:(?:\\.\\d{0,2})|(?:\\.))?")) {
            return src;
        } else if (dstart != dend) {
            return dest.subSequence(dstart, dend);
        }
        return "";
    }

    @Override
    public void onPositiveClick(DialogFragment dialog, int dialogId) {
        PdSaleParams pdSaleParams = new PdSaleParams();
        pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
        pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
        Navigator.navigateToPdSaleActivity(this, pdSaleParams);
        finish();
    }

    @Override
    public void onNegativeClick(DialogFragment dialog, int dialogId) {
        /* NOP */
    }

}

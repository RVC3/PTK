package ru.ppr.cppk.listeners;

import android.app.Activity;
import android.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;

import ru.ppr.cppk.R;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogButtonStyle;

/**
 * @author Brazhkin A.V.
 *         <p>
 *         Открытие экрана оформления нового ПД с предварительным подтверждением
 */
public class SellNewPdOnClickListener implements OnClickListener {

    private Activity activity;

    /**
     * Передаваемая активити должна реализовавать интерфейс
     * CppkDialogFragment.CppkDialogClickListener
     *
     * @param activity
     */
    public SellNewPdOnClickListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {

        String message = activity.getString(R.string.question_sell_new_pd);
        String positiveButtonText = activity.getString(R.string.Yes);
        String negativeButtonText = activity.getString(R.string.No);

        DialogFragment dialogFragment = CppkDialogFragment.getInstance(null, message, positiveButtonText, negativeButtonText,
                CppkDialogButtonStyle.HORIZONTAL);

        dialogFragment.setCancelable(false);
        dialogFragment.show(activity.getFragmentManager(), "Dialog");

    }

}

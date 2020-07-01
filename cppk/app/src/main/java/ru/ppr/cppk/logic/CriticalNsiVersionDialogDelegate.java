package ru.ppr.cppk.logic;

import android.app.FragmentManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.LinearLayout;

import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.dialog.SimpleDialog;

/**
 * @author Dmitry Nevolin
 */
public class CriticalNsiVersionDialogDelegate {

    private final CriticalNsiChecker criticalNsiChecker;
    private final FragmentManager fragmentManager;
    private final Resources resources;
    private final String criticalNsiCloseDialogTag;

    public CriticalNsiVersionDialogDelegate(@NonNull CriticalNsiChecker criticalNsiChecker,
                                            @NonNull FragmentManager fragmentManager,
                                            @NonNull Resources resources,
                                            @NonNull String criticalNsiCloseDialogTag) {
        this.criticalNsiChecker = criticalNsiChecker;
        this.fragmentManager = fragmentManager;
        this.resources = resources;
        this.criticalNsiCloseDialogTag = criticalNsiCloseDialogTag;
    }

    public boolean showCriticalNsiCloseDialogIfNeeded(@Nullable SimpleDialog.DialogBtnClickListener okClickListener) {
        if (criticalNsiChecker.checkCriticalNsiCloseDialogShouldBeShown()) {
            String okButtonText = criticalNsiChecker.checkCriticalNsiCloseShiftPermissions() ?
                    resources.getString(R.string.critical_nsi_close) :
                    resources.getString(R.string.critical_nsi_close_ok);

            SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                    resources.getString(R.string.critical_nsi_close_message),
                    okButtonText,
                    null,
                    LinearLayout.HORIZONTAL,
                    0);

            simpleDialog.setCancelable(false);

            if (okClickListener != null) {
                simpleDialog.setDialogPositiveBtnClickListener(okClickListener);
            }

            simpleDialog.show(fragmentManager, criticalNsiCloseDialogTag);

            return true;
        }

        return false;
    }

}

package ru.ppr.chit.helpers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import ru.ppr.chit.R;

/**
 * Класс-помощник для отображения диалоговых окон.
 *
 * @author m.sidorov.
 */
public class AppDialogHelper {

    private static final int TITLE_ERROR_RES_ID = R.string.dialog_error_title;
    private static final int IC_ERROR_RES_ID = R.drawable.ic_error;

    public static void showError(@NonNull Context context, String message) {
        createSimpleDialog(context, context.getString(TITLE_ERROR_RES_ID), message, IC_ERROR_RES_ID).show();
    }

    public static void showError(@NonNull Context context, String title, String message) {
        createSimpleDialog(context, title, message, IC_ERROR_RES_ID).show();
    }

    public static void showInfo(@NonNull Context context, String title, String message) {
        createSimpleDialog(context, title, message, 0).show();
    }

    public static ProgressDialog createProgress(@NonNull Context context, String title, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;
    }

    public static AlertDialog createSimpleDialog(@NonNull Context context, String title, String message, @DrawableRes int iconResId) {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIcon(iconResId);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.dialog_ok_button), (senderDialog, which) -> senderDialog.cancel());
        dialog.setCancelable(false);
        return dialog;
    }

}

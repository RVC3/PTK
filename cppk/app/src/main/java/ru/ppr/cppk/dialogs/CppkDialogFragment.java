package ru.ppr.cppk.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.androidquery.AQuery;

import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.FeedbackDialog;

/**
 * @deprecated Use {@link ru.ppr.cppk.ui.dialog.SimpleDialog} instead
 */
@Deprecated
public class CppkDialogFragment extends DialogFragment implements OnClickListener {

    private static final String DIALOG_TITLE = "DialogTitle";
    private static final String DIALOG_MESSAGE = "DialogMessage";
    private static final String DIALOG_POSITIVE_BUTTON_TEXT = "PositiveText";
    private static final String DIALOG_NEGATIVE_BUTTON_TEXT = "NegativeText";
    private static final String DIALOG_ID = "DialogId";
    private static final String DIALOG_BUTTON_STYLE = "DialogButtonStyle";

    public enum CppkDialogButtonStyle {VERTICAL, HORIZONTAL}

    public interface CppkDialogClickListener {
        /**
         * Вызывается при нажатии на положительную кнопку
         *
         * @param dialog
         * @param idDialog
         */
        void onPositiveClick(DialogFragment dialog, int idDialog);

        /**
         * Вызывается при нажатии на отрицательную кнопку
         *
         * @param dialog
         * @param idDialog
         */
        void onNegativeClick(DialogFragment dialog, int idDialog);
    }

    private CppkDialogClickListener listener;

    private String title = null;
    private String message = null;
    private String positiveText = null;
    private String negativeText = null;
    private int dialogId = -1;
    private CppkDialogButtonStyle buttonStyle;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof CppkDialogClickListener) {
            listener = (CppkDialogClickListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Создает фрагмент диалога, если необходимо не выводить какие либо поля, то необходимо передавать в качестве параметра null
     *
     * @param title              заголовок диалога
     * @param message            сообщение
     * @param positiveButtonText текст кнопки
     * @param negativeButtonText текст кнопки
     * @param buttonStyle        стиль расположения кнопок в диалоге
     * @return
     */
    public static CppkDialogFragment getInstance(String title,
                                                 String message,
                                                 String positiveButtonText,
                                                 String negativeButtonText,
                                                 CppkDialogButtonStyle buttonStyle) {

        return getInstance(title, message, positiveButtonText, negativeButtonText, -1, buttonStyle);
    }

    /**
     * Создает фрагмент диалога, если необходимо не выводить какие либо поля, то необходимо передавать в качестве параметра null/
     * Используется в случае если диалогу надо присворить ИД, с целью идентификации,
     * в функциях обработки результата, какой диалог вызвал это события
     *
     * @param title              заголовок диалога
     * @param message            сообщение
     * @param positiveButtonText текст кнопки
     * @param negativeButtonText текст кнопки
     * @param dialogId           ид диалога
     * @param buttonStyle        стиль расположения кнопок в диалоге
     * @return
     */
    public static CppkDialogFragment getInstance(String title,
                                                 String message,
                                                 String positiveButtonText,
                                                 String negativeButtonText,
                                                 int dialogId,
                                                 CppkDialogButtonStyle buttonStyle) {

        CppkDialogFragment dialogFragment = new CppkDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_TITLE, title);
        bundle.putString(DIALOG_MESSAGE, message);
        bundle.putString(DIALOG_POSITIVE_BUTTON_TEXT, positiveButtonText);
        bundle.putString(DIALOG_NEGATIVE_BUTTON_TEXT, negativeButtonText);
        bundle.putInt(DIALOG_ID, dialogId);
        bundle.putSerializable(DIALOG_BUTTON_STYLE, buttonStyle);
        dialogFragment.setArguments(bundle);

        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        title = bundle.getString(DIALOG_TITLE);
        message = bundle.getString(DIALOG_MESSAGE);
        positiveText = bundle.getString(DIALOG_POSITIVE_BUTTON_TEXT);
        negativeText = bundle.getString(DIALOG_NEGATIVE_BUTTON_TEXT);
        dialogId = bundle.getInt(DIALOG_ID, -1);
        buttonStyle = (CppkDialogButtonStyle) bundle.getSerializable(DIALOG_BUTTON_STYLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new FeedbackDialog(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view;
        if (buttonStyle == CppkDialogButtonStyle.HORIZONTAL)
            view = inflater.inflate(ru.ppr.cppk.R.layout.cppk_alert_dialog, null);
        else
            view = inflater.inflate(ru.ppr.cppk.R.layout.cppk_alert_dialog_vertical, null);

        AQuery aQuery = new AQuery(view);
        if (title != null)
            aQuery.id(R.id.cppk_dialog_title).text(title).visible();

        if (message != null)
            aQuery.id(R.id.cppk_dialog_message).text(message);

        if (positiveText != null) {
            aQuery.id(R.id.cppk_dialog_positive_button).text(positiveText).clicked(this);
        } else {
            aQuery.id(R.id.cppk_dialog_positive_button).gone();
        }

        if (negativeText != null) {
            aQuery.id(R.id.cppk_dilog_negative_butoon).text(negativeText).clicked(this);
        } else {
            aQuery.id(R.id.cppk_dilog_negative_butoon).gone();
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LayoutParams params = dialogWindow.getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.cppk_dialog_width);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(params);

        return dialog;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            if (v.getId() == R.id.cppk_dialog_positive_button) {
                listener.onPositiveClick(this, dialogId);
            } else if (v.getId() == R.id.cppk_dilog_negative_butoon) {
                listener.onNegativeClick(this, dialogId);
            }
        }
        dismiss();
    }
}

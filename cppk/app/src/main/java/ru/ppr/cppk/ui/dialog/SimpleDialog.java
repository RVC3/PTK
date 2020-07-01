package ru.ppr.cppk.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.FeedbackDialog;

/**
 * Стандартный AlertDialog для приложения.
 * Альтернатива CppkDialogFragment, т.к. тот завязан на Activity
 *
 * @author Aleksandr Brazhkin
 */
public class SimpleDialog extends DialogFragment {


    public static final String FRAGMENT_TAG = SimpleDialog.class.getSimpleName();

    // ARGS
    private static final String ARG_DIALOG_TITLE = "ARG_DIALOG_TITLE";
    private static final String ARG_DIALOG_MESSAGE = "ARG_DIALOG_MESSAGE";
    private static final String ARG_DIALOG_POSITIVE_BUTTON_TEXT = "ARG_DIALOG_POSITIVE_BUTTON_TEXT";
    private static final String ARG_DIALOG_NEGATIVE_BUTTON_TEXT = "ARG_DIALOG_NEGATIVE_BUTTON_TEXT";
    private static final String ARG_DIALOG_ORIENTATION = "ARG_DIALOG_ORIENTATION";
    private static final String ARG_DIALOG_ID = "ARG_DIALOG_ID";

    public static SimpleDialog newInstance(String title,
                                           String message,
                                           String positiveButtonText,
                                           String negativeButtonText,
                                           int orientation,
                                           int dialogId) {
        SimpleDialog fragment = new SimpleDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_DIALOG_TITLE, title);
        bundle.putString(ARG_DIALOG_MESSAGE, message);
        bundle.putString(ARG_DIALOG_POSITIVE_BUTTON_TEXT, positiveButtonText);
        bundle.putString(ARG_DIALOG_NEGATIVE_BUTTON_TEXT, negativeButtonText);
        bundle.putInt(ARG_DIALOG_ORIENTATION, orientation);
        bundle.putInt(ARG_DIALOG_ID, dialogId);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Слушатель нажатий на кнопки диалога.
     */
    public interface DialogBtnClickListener {
        void onBtnClick(DialogFragment dialog, int dialogId);
    }

    private String title = null;
    private String message = null;
    private String positiveBtnText = null;
    private String negativeBtnText = null;
    private int dialogId;
    private int orientation;
    private DialogBtnClickListener dialogPositiveBtnClickListener;
    private DialogBtnClickListener dialogNegativeBtnClickListener;
    private DialogInterface.OnCancelListener onCancelListener;
    private DialogInterface.OnDismissListener onDismissListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        title = args.getString(ARG_DIALOG_TITLE);
        message = args.getString(ARG_DIALOG_MESSAGE);
        positiveBtnText = args.getString(ARG_DIALOG_POSITIVE_BUTTON_TEXT);
        negativeBtnText = args.getString(ARG_DIALOG_NEGATIVE_BUTTON_TEXT);
        dialogId = args.getInt(ARG_DIALOG_ID, -1);
        orientation = args.getInt(ARG_DIALOG_ORIENTATION);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new FeedbackDialog(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_simple, null);

        LinearLayout buttonsContainer = (LinearLayout) view.findViewById(R.id.buttonsContainer);
        buttonsContainer.setOrientation(orientation);

        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(title);
        titleView.setVisibility(title == null ? View.GONE : View.VISIBLE);

        TextView messageView = (TextView) view.findViewById(R.id.message);
        messageView.setText(message);
        messageView.setVisibility(message == null ? View.GONE : View.VISIBLE);

        TextView positiveBtn = (TextView) view.findViewById(R.id.positiveBtn);
        positiveBtn.setText(positiveBtnText);
        positiveBtn.setVisibility(positiveBtnText == null ? View.GONE : View.VISIBLE);
        positiveBtn.setOnClickListener(v -> {
            if (dialogPositiveBtnClickListener != null) {
                dialogPositiveBtnClickListener.onBtnClick(SimpleDialog.this, dialogId);
            }
            dismiss();
        });

        TextView negativeBtn = (TextView) view.findViewById(R.id.negativeBtn);
        negativeBtn.setText(negativeBtnText);
        negativeBtn.setVisibility(negativeBtnText == null ? View.GONE : View.VISIBLE);
        negativeBtn.setOnClickListener(v -> {
            if (dialogNegativeBtnClickListener != null) {
                dialogNegativeBtnClickListener.onBtnClick(SimpleDialog.this, dialogId);
            }
            dismiss();
        });

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.cppk_dialog_width);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(params);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    /**
     * Set a listener to be invoked when the dialog is canceled.
     *
     * @param onCancelListener
     */
    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    /**
     * Set a listener to be invoked when the dialog is dismissed.
     *
     * @param onDismissListener
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    /**
     * Set a listener to be invoked when the positive button of the dialog is pressed.
     *
     * @param dialogPositiveBtnClickListener
     */
    public void setDialogPositiveBtnClickListener(DialogBtnClickListener dialogPositiveBtnClickListener) {
        this.dialogPositiveBtnClickListener = dialogPositiveBtnClickListener;
    }

    /**
     * Set a listener to be invoked when the negative button of the dialog is pressed.
     *
     * @param dialogNegativeBtnClickListener
     */
    public void setDialogNegativeBtnClickListener(DialogBtnClickListener dialogNegativeBtnClickListener) {
        this.dialogNegativeBtnClickListener = dialogNegativeBtnClickListener;
    }
}

package ru.ppr.cppk.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import ru.ppr.cppk.R;

/**
 * Диалог обратной связи
 * Created by Григорий on 15.03.2017.
 */
public class SimpleFeedbackDialog extends Dialog {

    private Context context;

    //UI
    private Button sendBtn;
    private Button cancelBtn;

    private EditText inputEditText;

    private SimpleFeedbackDialog.DialogBtnClickListener dialogPositiveBtnClickListener;
    private SimpleFeedbackDialog.DialogBtnClickListener dialogNegativeBtnClickListener;

    public SimpleFeedbackDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public static SimpleFeedbackDialog newInstance(Context context) {
        SimpleFeedbackDialog instance = new SimpleFeedbackDialog(context);
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window dialogWindow = getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        params.width = context.getResources().getDimensionPixelSize(R.dimen.cppk_dialog_width);
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(params);
        setContentView(R.layout.dialog_feedback);

        sendBtn = (Button) findViewById(R.id.positiveBtn);
        cancelBtn = (Button) findViewById(R.id.negativeBtn);
        inputEditText = (EditText) findViewById(R.id.inputEditText);

        sendBtn.setOnClickListener(v -> {
            if (dialogPositiveBtnClickListener != null) {
                dialogPositiveBtnClickListener.onBtnClick(SimpleFeedbackDialog.this, inputEditText.getText().toString());
            }
            dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            if (dialogNegativeBtnClickListener != null) {
                dialogNegativeBtnClickListener.onBtnClick(SimpleFeedbackDialog.this, inputEditText.getText().toString());
            }
            dismiss();
        });
    }

    /**
     * Слушатель нажатий на кнопки диалога.
     */
    public interface DialogBtnClickListener {
        void onBtnClick(Dialog dialog, String inputText);
    }

    /**
     * Set a listener to be invoked when the positive button of the dialog is pressed.
     *
     * @param dialogPositiveBtnClickListener
     */
    public void setDialogPositiveBtnClickListener(SimpleFeedbackDialog.DialogBtnClickListener dialogPositiveBtnClickListener) {
        this.dialogPositiveBtnClickListener = dialogPositiveBtnClickListener;
    }

    /**
     * Set a listener to be invoked when the negative button of the dialog is pressed.
     *
     * @param dialogNegativeBtnClickListener
     */
    public void setDialogNegativeBtnClickListener(SimpleFeedbackDialog.DialogBtnClickListener dialogNegativeBtnClickListener) {
        this.dialogNegativeBtnClickListener = dialogNegativeBtnClickListener;
    }

    @Override
    public void show() {
        super.show();
        showKeyboard(inputEditText);
    }

    /**
     * Открывает клавиатуру с фокусом на поле ввода
     */
    private void showKeyboard(EditText editText) {
        editText.post(() -> {
            editText.requestFocus();
            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        });
    }
}

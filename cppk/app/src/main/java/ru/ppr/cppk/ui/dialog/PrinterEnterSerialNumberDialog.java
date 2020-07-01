package ru.ppr.cppk.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import ru.ppr.cppk.R;
import ru.ppr.cppk.systembar.FeedbackDialog;

/**
 * Диалог ввода заводского номера принтера, валидирует только на пустую строку,
 * или строку содержащую только пробелы, возвращает trimmed заводской номер в листенер.
 *
 * @author Dmitry Nevolin
 */
public class PrinterEnterSerialNumberDialog extends DialogFragment {

    public static final String FRAGMENT_TAG = PrinterEnterSerialNumberDialog.class.getSimpleName();

    public static PrinterEnterSerialNumberDialog newInstance() {
        return new PrinterEnterSerialNumberDialog();
    }

    private EditText serialNumber;
    private View error;
    private SerialNumberEnterListener serialNumberEnterListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_printer_enter_serial_number, null);

        serialNumber = (EditText) view.findViewById(R.id.serial_number);
        error = view.findViewById(R.id.error);

        serialNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                error.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        view.findViewById(R.id.ok).setOnClickListener(v -> {
            String tmp = serialNumber.getText().toString();

            if (isSerialNumberValid(tmp)) {
                if (serialNumberEnterListener != null) {
                    serialNumberEnterListener.onSerialNumberEntered(this, tmp);
                }

                dismiss();
            } else {
                error.setVisibility(View.VISIBLE);
            }
        });

        Dialog dialog = new FeedbackDialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(view);

        return dialog;
    }

    public void setSerialNumberEnterListener(SerialNumberEnterListener serialNumberEnterListener) {
        this.serialNumberEnterListener = serialNumberEnterListener;
    }

    private boolean isSerialNumberValid(@NonNull String serialNumber) {
        return serialNumber.matches("^[0-9A-Za-z]+$");
    }

    public interface SerialNumberEnterListener {
        void onSerialNumberEntered(DialogFragment dialogFragment, @NonNull String serialNumber);
    }

}

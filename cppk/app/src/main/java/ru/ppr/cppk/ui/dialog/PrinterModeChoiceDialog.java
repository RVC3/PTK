package ru.ppr.cppk.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;

import ru.ppr.cppk.R;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.systembar.FeedbackDialog;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;

/**
 * Диалог выбора драйвера принтера, поддерживает только
 * {@link PrinterManager}.PRINTER_MODE_MOEBIUS_REAL и {@link PrinterManager}.PRINTER_MODE_SHTRIH
 *
 * @author Dmitry Nevolin
 */
public class PrinterModeChoiceDialog extends DialogFragment {

    public static final String FRAGMENT_TAG = PrinterModeChoiceDialog.class.getSimpleName();

    public static PrinterModeChoiceDialog newInstance() {
        return new PrinterModeChoiceDialog();
    }

    private PrinterModeAdapter adapter;
    private ItemClickListener itemClickListener;
    private BackClickListener backClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_printer_mode_choice, null);

        ListView printerModeList = (ListView) view.findViewById(R.id.printer_mode_list);
        printerModeList.setAdapter(adapter = new PrinterModeAdapter(getActivity()));
        printerModeList.setOnItemClickListener((adapterView, view1, position, id) -> {
            Integer printerMode = adapter.getItem(position);

            if (itemClickListener != null && printerMode != null) {
                itemClickListener.onItemClick(this, printerMode);
            }

            dismiss();
        });

        adapter.setItems(Arrays.asList(PrinterManager.PRINTER_MODE_MOEBIUS_REAL, PrinterManager.PRINTER_MODE_SHTRIH));

        Dialog dialog = new FeedbackDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.setTitle(R.string.printer_mode_choice_dialog_title);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (backClickListener != null) {
                    backClickListener.onBackClick();
                }
            }
            return false;
        });

        return dialog;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setBackClickListener(BackClickListener backClickListener) {
        this.backClickListener = backClickListener;
    }

    /**
     * Слушатель нажатий на элементы
     */
    public interface ItemClickListener {
        void onItemClick(DialogFragment dialogFragment, int printerMode);
    }

    /**
     * Слушатель нажатия на back
     */
    public interface BackClickListener {
        void onBackClick();
    }

    private class PrinterModeAdapter extends BaseAdapter<Integer> {

        private LayoutInflater layoutInflater;

        PrinterModeAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Integer printerMode = getItem(i);

            if (view == null) {
                view = layoutInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            }

            ((TextView) view).setGravity(Gravity.CENTER);
            ((TextView) view).setText(getPrinterModeText(printerMode));

            return view;
        }

        private String getPrinterModeText(Integer printerMode) {
            if (printerMode == PrinterManager.PRINTER_MODE_SHTRIH) {
                return getString(R.string.printer_mode_choice_dialog_printer_shtrih);
            } else {
                return getString(R.string.printer_mode_choice_dialog_printer_zebra);
            }
        }
    }


}

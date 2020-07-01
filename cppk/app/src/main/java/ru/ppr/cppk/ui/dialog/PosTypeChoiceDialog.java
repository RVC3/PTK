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
import ru.ppr.cppk.pos.PosType;
import ru.ppr.cppk.systembar.FeedbackDialog;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;

/**
 * Диалог выбора драйвера POS-терминала, поддерживает только
 * {@link PosType#INGENICO} и {@link PosType#INPAS}
 *
 * @author Dmitry Nevolin
 */
public class PosTypeChoiceDialog extends DialogFragment {

    public static final String FRAGMENT_TAG = PosTypeChoiceDialog.class.getSimpleName();

    public static PosTypeChoiceDialog newInstance() {
        return new PosTypeChoiceDialog();
    }

    private PosTypeAdapter adapter;
    private ItemClickListener itemClickListener;
    private BackClickListener backClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_pos_type_choice, null);

        ListView posTypeList = (ListView) view.findViewById(R.id.pos_type_list);
        posTypeList.setAdapter(adapter = new PosTypeAdapter(getActivity()));
        posTypeList.setOnItemClickListener((adapterView, view1, position, id) -> {
            PosType posType = adapter.getItem(position);

            if (itemClickListener != null && posType != null) {
                itemClickListener.onItemClick(this, posType);
            }

            dismiss();
        });

        adapter.setItems(Arrays.asList(PosType.INGENICO, PosType.INPAS));

        Dialog dialog = new FeedbackDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.setTitle(R.string.pos_type_choice_dialog_title);
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
        void onItemClick(DialogFragment dialogFragment, PosType posType);
    }

    /**
     * Слушатель нажатия на back
     */
    public interface BackClickListener {
        void onBackClick();
    }

    private class PosTypeAdapter extends BaseAdapter<PosType> {

        private LayoutInflater layoutInflater;

        PosTypeAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            PosType posType = getItem(i);

            if (view == null) {
                view = layoutInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            }

            ((TextView) view).setGravity(Gravity.CENTER);
            ((TextView) view).setText(getPosTypeText(posType));

            return view;
        }

        private String getPosTypeText(PosType posType) {
            if (posType == PosType.INGENICO) {
                return getString(R.string.pos_type_choice_dialog_terminal_ingenico);
            } else {
                return getString(R.string.pos_type_choice_dialog_terminal_inpas);
            }
        }
    }

}

package ru.ppr.cppk.ui.activity.selectionActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.androidquery.AQuery;

import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.widget.FilterEditText.OnBackListener;
import ru.ppr.cppk.ui.widget.SmartAutoCompleteTextView;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;

public abstract class BaseSelectionActivity<T> extends SystemBarActivity implements OnBackListener {

    protected Globals globals;
    private SmartAutoCompleteTextView autoCompleateView;
    private BaseListAdapter<T> adapter;

    private boolean isShowDropdown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globals = (Globals) getApplication();

        setContentView(R.layout.selection_activity);
        AQuery aq = new AQuery(this);
        autoCompleateView = (SmartAutoCompleteTextView) aq.id(R.id.selection_activity_edit_text_field).getView();
//		adapter = new ArrayAdapter<T>(this, android.R.layout.simple_list_item_1, getData());
        adapter = getAdapter();
        autoCompleateView.setAdapter(adapter);
        autoCompleateView.setOnClickListener(onClickListener);
        autoCompleateView.setOnKeyListener(onKeyListener);
        autoCompleateView.setOnHamburgerClickListener(onHamburgerClickListener);
        autoCompleateView.setOnItemClickListener(itemClickListener);
        autoCompleateView.setHint(getHintId());
        autoCompleateView.setThreshold(1);

        aq.id(R.id.selection_activity_title).text(getTitleId());
    }

    @Override
    public boolean onBackAtEditPressed() {

        boolean result = true;
        if (isShowDropdown) {
            hideDropDown();
            result = false;
        } else {
            finish();
        }
        return result;
    }

    protected abstract BaseListAdapter<T> getAdapter();

    protected abstract List<T> getData();

    protected abstract Intent getIntent(T selectedItem);

    protected abstract int getTitleId();

    protected abstract int getHintId();

    protected abstract
    @Nullable
    T getItemForName(String name);

    private void showDropdown() {
        isShowDropdown = true;
        autoCompleateView.showDropDown();
    }

    private void hideDropDown() {
        isShowDropdown = false;
        autoCompleateView.hideDropDown();
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            showDropdown();
        }
    };

    private OnClickListener onHamburgerClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            showDropdown();
        }
    };

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            T selected = adapter.getItem(position);
            getIntent(selected);
            hideKeyboard(view);
            finish();
        }
    };

    private OnKeyListener onKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                if (v instanceof SmartAutoCompleteTextView) {
                    SmartAutoCompleteTextView textView = (SmartAutoCompleteTextView) v;
                    String inputText = textView.getText().toString();
                    T item = getItemForName(inputText);
                    if (item != null) {
                        getIntent(item);
                        finish();
                    } else {
                        Globals.getInstance().getToaster().showToast(R.string.incorrect_input);
                    }
                }

                return true;
            }
            return false;
        }
    };
}

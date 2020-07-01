package ru.ppr.cppk.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import java.util.Calendar;

import ru.ppr.cppk.R;

/**
 * Диалог выбора дата + время
 *
 * @author Brazhkin A.V.
 */
public class DateTimePickerDialog extends AlertDialog implements OnClickListener, OnDateChangedListener, OnTimeChangedListener {

    private Context mContext;

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    private final TimePicker mTimePicker;

    private final DatePicker mDatePicker;
    private final OnDateTimeSetListener mCallBack;
    private final Calendar mCalendar;

    private boolean mTitleNeedsUpdate = true;

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateTimeSetListener {

        void onDateTimeSet(DatePicker datePicker, TimePicker timePicker, int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute);
    }

    public DateTimePickerDialog(Context context, OnDateTimeSetListener callBack, int year, int monthOfYear, int dayOfMonth, int hourOfDay,
                                int minute, boolean is24HourView) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth, hourOfDay, minute, is24HourView);
    }

    @SuppressLint("InflateParams")
    public DateTimePickerDialog(Context context, int theme, OnDateTimeSetListener callBack, int year, int monthOfYear, int dayOfMonth, int hourOfDay,
                                int minute, boolean is24HourView) {
        super(context, theme);

        mCallBack = callBack;

        mCalendar = Calendar.getInstance();

        Context themeContext = getContext();
        setButton(BUTTON_POSITIVE, themeContext.getText(R.string.dialog_complete), this);
        setIcon(0);

        LayoutInflater inflater = (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_date_time_picker, null);
        setView(view);

        mDatePicker = (DatePicker) view.findViewById(R.id.datePicker);
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);

        mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(is24HourView);
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minute);
        mTimePicker.setOnTimeChangedListener(this);

        mContext = context;
    }

    public void onClick(DialogInterface dialog, int which) {
        tryNotifyDateTimeSet();
    }

    public void onDateChanged(DatePicker view, int year, int month, int day) {
        mDatePicker.init(year, month, day, this);
        updateTitle(year, month, day, -1, -1);
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        updateTitle(-1, -1, -1, hourOfDay, minute);
    }

    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    public TimePicker getTimePicker() {
        return mTimePicker;
    }

    public void updateDateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minutOfHour) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minutOfHour);
    }

    private void tryNotifyDateTimeSet() {
        if (mCallBack != null) {
            mDatePicker.clearFocus();
            mTimePicker.clearFocus();
            mCallBack.onDateTimeSet(mDatePicker, mTimePicker, mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),
                    mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
        }
    }

    @Override
    protected void onStop() {
        // tryNotifyDateTimeSet();
        super.onStop();
    }

    private void updateTitle(int year, int month, int day, int hour, int minute) {
        if (!mDatePicker.getCalendarViewShown()) {
            if (year != -1) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, day);
            } else {
                mCalendar.set(Calendar.HOUR_OF_DAY, hour);
                mCalendar.set(Calendar.MINUTE, minute);
            }
            String title = DateUtils.formatDateTime(mContext, mCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
                    | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY);
            setTitle(title);
            mTitleNeedsUpdate = true;
        } else {
            if (mTitleNeedsUpdate) {
                mTitleNeedsUpdate = false;
                setTitle(R.string.about_ptk);
            }
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        state.putInt(HOUR, mTimePicker.getCurrentHour());
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, this);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
    }
}
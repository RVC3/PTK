package ru.ppr.cppk.settings;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PermissionDvc;

public class SetTimeActivity extends SystemBarActivity implements OnClickListener {

    private Globals g;

    private int timeChangesPeriod = 0;

    private boolean isCTO = false;

    private int newHour;
    private int newMinute;
    private int newDay;
    private int newMonth;
    private int newYear;
    private long oldTimestamp;

    private TextView dateTextView;
    private TextView timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_and_date_activity);

        g = (Globals) getApplication();

        dateTextView = (TextView) findViewById(R.id.date);
        dateTextView.setText(DateFormatOperations.getDate(new Date()));
        dateTextView.setOnClickListener(this);

        timeTextView = (TextView) findViewById(R.id.time);
        timeTextView.setText(DateFormatOperations.getTime(new Date()));
        timeTextView.setOnClickListener(this);

        Button button = (Button) findViewById(R.id.set_new_time);
        button.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        newHour = calendar.get(Calendar.HOUR_OF_DAY);
        newMinute = calendar.get(Calendar.MINUTE);
        newYear = calendar.get(Calendar.YEAR);
        newDay = calendar.get(Calendar.DAY_OF_MONTH);
        newMonth = calendar.get(Calendar.MONTH);

        oldTimestamp = calendar.getTimeInMillis() / 1000;

        timeChangesPeriod = Dagger.appComponent().commonSettingsStorage().get().getTimeChangesPeriod() * 60;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.time:
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timepicker");
                break;

            case R.id.date:
                if (isCTO) {
                    DialogFragment newFragment2 = new DatePickerFragment();
                    newFragment2.show(getFragmentManager(), "datepicker");
                } else {
                    Globals.getInstance().getToaster().showToast(R.string.eror_set_date);
                }
                break;

            case R.id.set_new_time:
                setTime();
                break;

            default:
                break;
        }

    }

    @SuppressLint("ValidFragment")
    public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            newHour = hourOfDay;
            newMinute = minute;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("%02d", hourOfDay)).append(":").append(String.format("%02d", minute));
            timeTextView.setText(stringBuilder.toString());
        }
    }

    @SuppressLint("ValidFragment")
    private class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            newDay = day;
            newMonth = month;
            newYear = year;
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            dateTextView.setText(DateFormatOperations.getDate(calendar.getTime()));
        }
    }

    private void setTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(newYear, newMonth, newDay, newHour, newMinute);

        if (!getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.ChangeDateTimeMoreThen5Minutes)) {

            long newTime = calendar.getTimeInMillis() / 1000;

            if (Math.abs(oldTimestamp - newTime) > timeChangesPeriod) {
                LogEvent logEvent = Dagger.appComponent().logEventBuilder()
                        .setLogActionType(LogActionType.ATTEMPT_TO_CHANGE_TIME)
                        .setMessage("" + new Date(oldTimestamp * 1000) + " => " + new Date(calendar.getTimeInMillis()))
                        .build();
                Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEvent);
                Globals.getInstance().getToaster().showToast(getString(R.string.set_time_5_minute_error));
                return;
            }

        }

        ShiftEvent lastShift = getLocalDaoSession().getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        Check lastCheck = getLocalDaoSession().getCheckDao().getLastCheckForPeriod(null, null);

        long lastCheckTime = (lastCheck != null ? lastCheck.getPrintDatetime().getTime() : 0);
        long lastShiftOpenTime = (lastShift != null ? lastShift.getStartTime().getTime() : 0);
        long lastShiftCloseTime = (lastShift != null && lastShift.getCloseTime() != null ? lastShift.getCloseTime().getTime() : 0);
        long lastEvent = getLocalDaoSession().getEventDao().getLastEventTimeStamp();

        if (lastCheckTime >= calendar.getTimeInMillis()
                || lastShiftOpenTime >= calendar.getTimeInMillis()
                || lastShiftCloseTime >= calendar.getTimeInMillis()
                || lastEvent >= calendar.getTimeInMillis()) {
            LogEvent logEvent = Dagger.appComponent().logEventBuilder()
                    .setLogActionType(LogActionType.ATTEMPT_TO_CHANGE_TIME)
                    .setMessage("" + new Date(oldTimestamp * 1000) + " => " + new Date(calendar.getTimeInMillis()))
                    .build();
            Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEvent);
            Globals.getInstance().getToaster().showToast(getString(R.string.set_time_last_check_after_date_error));
            return;
        }

        Logger.info(SetTimeActivity.class,
                "time is changed: " +
                        DateFormatOperations.getDateddMMyyyyHHmmss(new Date(oldTimestamp * 1000)) +
                        " => " +
                        DateFormatOperations.getDateddMMyyyyHHmmss(new Date(calendar.getTimeInMillis())));

        LogEvent logEvent = Dagger.appComponent().logEventBuilder()
                .setLogActionType(LogActionType.TIME_CHANGE)
                .setMessage("" + new Date(oldTimestamp * 1000) + " => " + new Date(calendar.getTimeInMillis()))
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEvent);
        SystemClock.setCurrentTimeMillis(calendar.getTimeInMillis());
        finish();
    }

}

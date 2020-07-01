package ru.ppr.cppk.ui.fragment.readPd;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidquery.AQuery;

import ru.ppr.core.ui.helper.TimerResourceHelper;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.DataCarrierReadSettings;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.ui.activity.base.readBarcode.BaseReadBarcodeActivity;
import ru.ppr.logger.Logger;

@Deprecated
public class ReadInProgressFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(ReadInProgressFragment.class);

    private static final String READER_TYPE = "READER_TYPE";

    private ImageView counterView;
    private MyCounter timerCounter;
    private ReaderType readerType;

    public static Fragment newInstance(ReaderType readerType) {
        Fragment fragment = new ReadInProgressFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(READER_TYPE, readerType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readerType = (ReaderType) getArguments().getSerializable(READER_TYPE);
        timerCounter = new MyCounter(readerType == ReaderType.TYPE_BSC ? DataCarrierReadSettings.RFID_FIND_TIME :
                DataCarrierReadSettings.BARCODE_FIND_TIME, 1000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.read_pd_in_progress_fragment, container, false);

        AQuery aQuery = new AQuery(view);
        HolderId holderId = getResIds(readerType);

        counterView = aQuery.id(R.id.read_pd_timer_img).getImageView();
        aQuery.id(R.id.title_where_read_pd).text(holderId.messageId);
        aQuery.id(R.id.read_pd_tape_img).image(holderId.imageId);

        timerCounter.start();

        return view;
    }

    private class MyCounter extends CountDownTimer {

        private int prevTickValue = -1;

        public MyCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int second = (int) (millisUntilFinished / 1000);
            if (prevTickValue != second) {
                prevTickValue = second;
                counterView.setImageResource(TimerResourceHelper.getTimerImageResource(second));
            }
        }

        @Override
        public void onFinish() {
            Logger.trace(TAG, "MyCounter.onFinish()");
            BaseReadBarcodeActivity activity = ((BaseReadBarcodeActivity) getActivity());
            if (activity != null && activity.isActivityResumed()) {
                Logger.trace(TAG, "MyCounter.onFinish() stopRead");
                activity.stopRead();
            }
        }

    }

    private HolderId getResIds(ReaderType readerType) {

        HolderId holder = new HolderId();

        switch (readerType) {
            case TYPE_BARCODE:
                holder.messageId = R.string.read_barcode;
                holder.imageId = R.drawable.img_read_barcode;
                break;

            case TYPE_BSC:
                holder.messageId = R.string.pull_card_please;
                holder.imageId = R.drawable.img_read_bsc;
                break;

            default:
                break;
        }
        return holder;
    }

    private class HolderId {
        public int imageId = -1;
        public int messageId = -1;
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.trace(TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerCounter.cancel();
    }
}

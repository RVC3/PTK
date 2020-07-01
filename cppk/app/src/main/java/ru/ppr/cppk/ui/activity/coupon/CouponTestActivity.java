package ru.ppr.cppk.ui.activity.coupon;

import android.os.Bundle;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.logic.coupon.CouponChecker;
import ru.ppr.cppk.logic.coupon.PtsKeysProvider;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;

/**
 * @author Dmitry Nevolin
 */
public class CouponTestActivity extends LoggedActivity {
    /**
     * Класс проверки талонов
     */
    private CouponChecker couponChecker;
    /**
     * Номера талонов для проверки
     */
    private LongSparseArray<Boolean> testCoupons;
    /**
     * Результат теста по талону
     */
    private LongSparseArray<Boolean> testResults;
    /**
     * Адаптер для вывода результатов теста
     */
    private TestCouponsAdapter testCouponsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.debug_test_tppd_coupon);

        CouponTestDi di = new CouponTestDi(Di.INSTANCE);
        PtsKeysProvider ptsKeysProvider = di.ptsKeysProvider();

        couponChecker = new CouponChecker(ptsKeysProvider, di.kuznyechik());
        testCoupons = di.testCoupons();
        testResults = new LongSparseArray<>();
        testCouponsAdapter = new TestCouponsAdapter();

        List<Long> keys = new ArrayList<>();
        for (int i = 0; i < testCoupons.size(); i++) {
            keys.add(testCoupons.keyAt(i));
        }

        testCouponsAdapter.setItems(keys);

        ((ListView) findViewById(R.id.test_out)).setAdapter(testCouponsAdapter);

        findViewById(R.id.run_test).setOnClickListener(view -> {
            for (int i = 0; i < testCoupons.size(); i++) {
                long couponNumber = testCoupons.keyAt(i);
                CouponChecker.Result checkResult = couponChecker.check(couponNumber);

                testResults.put(couponNumber, checkResult.isValid());
            }

            testCouponsAdapter.notifyDataSetChanged();
        });
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private class TestCouponsAdapter extends BaseAdapter<Long> {

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Long couponNumber = getItem(i);

            if (view == null) {
                view = createView();
            }

            LinearLayout layout = (LinearLayout) view;

            ((TextView) layout.getChildAt(0)).setText(String.format(Locale.getDefault(), "%016d", couponNumber));
            ((TextView) layout.getChildAt(1)).setText(testCoupons.get(couponNumber) == null ? "" : String.valueOf(testCoupons.get(couponNumber)));
            ((TextView) layout.getChildAt(2)).setText(testResults.get(couponNumber) == null ? "" : String.valueOf(testResults.get(couponNumber)));

            return view;
        }

        private View createView() {
            LinearLayout linearLayout = new LinearLayout(CouponTestActivity.this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(40)));

            TextView couponNumber = new TextView(CouponTestActivity.this);
            couponNumber.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(140), ViewGroup.LayoutParams.MATCH_PARENT));
            couponNumber.setGravity(Gravity.CENTER);

            TextView expected = new TextView(CouponTestActivity.this);
            expected.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(110), ViewGroup.LayoutParams.MATCH_PARENT));
            expected.setGravity(Gravity.CENTER);
            expected.setBackgroundColor(getResources().getColor(R.color.gray_2));

            TextView actual = new TextView(CouponTestActivity.this);
            actual.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(110), ViewGroup.LayoutParams.MATCH_PARENT));
            actual.setGravity(Gravity.CENTER);

            linearLayout.addView(couponNumber);
            linearLayout.addView(expected);
            linearLayout.addView(actual);

            return linearLayout;
        }
    }

}

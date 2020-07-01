package ru.ppr.cppk.ui.activity.commonSettingsManagement;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.ui.activity.base.settings.SettingsManagementActivity;
import ru.ppr.cppk.utils.CommonSettingsUtils;
import ru.ppr.logger.Logger;

/**
 * Экран общих настроек.
 *
 * @author Dmitry Nevolin
 */
public class CommonSettingsManagementActivity extends SettingsManagementActivity {

    private static final String TAG = Logger.makeLogTag(CommonSettingsManagementActivity.class);

    private CommonSettingsManagementDi di;

    private CommonSettings commonSettings;

    //region Views
    private EditText printerSendToOfdCountTriggerView;
    private EditText printerSendToOfdPeriodTriggerView;
    //endregion

    @NonNull
    @Override
    protected Map<String, Boolean> providedInitialSettingsMap() {
        Map<String, Boolean> initialSettingsMap = new HashMap<>();

        initialSettingsMap.put(CommonSettings.Entities.TEST_PD_PRINT_REQ, commonSettings.isTestPdPrintReq());
        initialSettingsMap.put(CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_OPENING_SHIFT, commonSettings.isDiscountShiftSheetOpeningShift());
        initialSettingsMap.put(CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ, commonSettings.isDiscountShiftSheetClosingShiftReq());
        initialSettingsMap.put(CommonSettings.Entities.SHEET_SHIFT_CLOSE_SHIFT_REQ, commonSettings.isSheetShiftCloseShiftReq());
        initialSettingsMap.put(CommonSettings.Entities.SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ, commonSettings.isSheetBlankingShiftClosingShiftReq());
        initialSettingsMap.put(CommonSettings.Entities.DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ, commonSettings.isDiscountMonthShiftSheetClosingMonthReq());
        initialSettingsMap.put(CommonSettings.Entities.MONTH_SHEET_CLOSING_MONTH_REQ, commonSettings.isMonthSheetClosingMonthReq());
        initialSettingsMap.put(CommonSettings.Entities.SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ, commonSettings.isSheetBlankingMonthClosingMonthReq());
        initialSettingsMap.put(CommonSettings.Entities.BT_MONTHLY_SHEET_CLOSING_MONTH_REQ, commonSettings.isBtMonthlySheetClosingMonthReq());
        initialSettingsMap.put(CommonSettings.Entities.ENABLE_ANNULATE_AFTER_TIME_OVER, commonSettings.isEnableAnnulateAfterTimeOver());
        initialSettingsMap.put(CommonSettings.Entities.SELECT_DRAFT_NSI, commonSettings.isSelectDraftNsi());
        initialSettingsMap.put(CommonSettings.Entities.LOG_FULL_SQL, commonSettings.isLogFullSQL());
        initialSettingsMap.put(CommonSettings.Entities.AUTO_BLOCKING_ENABLED, commonSettings.isAutoBlockingEnabled());
        initialSettingsMap.put(CommonSettings.Entities.IGNORE_CARD_VALIDITY_PERIOD, commonSettings.isIgnoreCardValidityPeriod());
        initialSettingsMap.put(CommonSettings.Entities.EXTRA_PAYMENT_WITH_CARD_ALLOWED, commonSettings.isExtraPaymentWithCardAllowed());
        initialSettingsMap.put(CommonSettings.Entities.DECREMENT_TRIP_ALLOWED, commonSettings.isDecrementTripAllowed());
        initialSettingsMap.put(CommonSettings.Entities.EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED, commonSettings.isExtraSaleForPdWithExemptionAllowed());

        return initialSettingsMap;
    }

    @NonNull
    @Override
    protected Map<String, String> providedSettingsNameMap() {
        Map<String, String> settingsNameMap = new HashMap<>();

        settingsNameMap.put(CommonSettings.Entities.TEST_PD_PRINT_REQ,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pTestPdPrintReq);
        settingsNameMap.put(CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_OPENING_SHIFT,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pDiscountShiftSheetOpeningShift);
        settingsNameMap.put(CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pDiscountShiftSheetClosingShiftReq);
        settingsNameMap.put(CommonSettings.Entities.SHEET_SHIFT_CLOSE_SHIFT_REQ,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pSheetShiftCloseShiftReq);
        settingsNameMap.put(CommonSettings.Entities.SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pSheetBlankingShiftClosingShiftReq);
        settingsNameMap.put(CommonSettings.Entities.DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pDiscountMonthShiftSheetClosingMonthReq);
        settingsNameMap.put(CommonSettings.Entities.MONTH_SHEET_CLOSING_MONTH_REQ,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pMonthSheetClosingMonthReq);
        settingsNameMap.put(CommonSettings.Entities.SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pSheetBlankingMonthClosingMonthReq);
        settingsNameMap.put(CommonSettings.Entities.BT_MONTHLY_SHEET_CLOSING_MONTH_REQ,
                CommonSettingsUtils.CommonSettingsEntities.Comments.pBTMonthlySheetClosingMonthReq);
        settingsNameMap.put(CommonSettings.Entities.ENABLE_ANNULATE_AFTER_TIME_OVER,
                CommonSettingsUtils.CommonSettingsEntities.Comments.enableAnnulateAfterTimeOver);
        settingsNameMap.put(CommonSettings.Entities.SELECT_DRAFT_NSI,
                CommonSettingsUtils.CommonSettingsEntities.Comments.selectDraftNsi);
        settingsNameMap.put(CommonSettings.Entities.LOG_FULL_SQL,
                CommonSettingsUtils.CommonSettingsEntities.Comments.logFullSQL);
        settingsNameMap.put(CommonSettings.Entities.AUTO_BLOCKING_ENABLED,
                CommonSettingsUtils.CommonSettingsEntities.Comments.autoBlockingEnabled);
        settingsNameMap.put(CommonSettings.Entities.IGNORE_CARD_VALIDITY_PERIOD,
                CommonSettingsUtils.CommonSettingsEntities.Comments.ignoreCardValidityPeriod);
        settingsNameMap.put(CommonSettings.Entities.EXTRA_PAYMENT_WITH_CARD_ALLOWED,
                CommonSettingsUtils.CommonSettingsEntities.Comments.extraPaymentWithCardAllowed);
        settingsNameMap.put(CommonSettings.Entities.DECREMENT_TRIP_ALLOWED,
                CommonSettingsUtils.CommonSettingsEntities.Comments.decrementTripAllowed);
        settingsNameMap.put(CommonSettings.Entities.EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED,
                CommonSettingsUtils.CommonSettingsEntities.Comments.extraSaleForPdWithExemptionAllowed);

        return settingsNameMap;
    }

    @Override
    protected void onSettingChanged(@NonNull String name, @NonNull Boolean value) {
        commonSettings.getSettings().put(name, String.valueOf(value));
    }

    @NonNull
    @Override
    protected String providedSettingsTitle() {
        return getString(R.string.common_settings_management_title);
    }

    @Override
    protected void applySettingsAndExit() {
        showApplySettingsProgress();

        int printerSendToOfdCountTrigger = commonSettings.getPrinterSendToOfdCountTrigger();
        try {
            printerSendToOfdCountTrigger = Integer.valueOf(printerSendToOfdCountTriggerView.getText().toString());
        } catch (NumberFormatException e) {
            Logger.error(TAG, e);
        }
        commonSettings.setPrinterSendToOfdCountTrigger(printerSendToOfdCountTrigger);

        int printerSendToOfdPeriodTrigger = commonSettings.getPrinterSendToOfdPeriodTrigger();
        try {
            printerSendToOfdPeriodTrigger = Integer.valueOf(printerSendToOfdPeriodTriggerView.getText().toString());
        } catch (NumberFormatException e) {
            Logger.error(TAG, e);
        }
        commonSettings.setPrinterSendToOfdPeriodTrigger(printerSendToOfdPeriodTrigger);

        Dagger.appComponent().commonSettingsStorage().update(commonSettings);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_settings_management);

        ListView listView = (ListView) findViewById(R.id.settings_list_view);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View header = layoutInflater.inflate(R.layout.view_common_settings_management_header, listView, false);
        listView.addHeaderView(header);

        di = new CommonSettingsManagementDi(Di.INSTANCE);

        commonSettings = di.commonSettings();

        TextView printerSendToOfdCountTriggerLabel = (TextView) header.findViewById(R.id.printerSendToOfdCountTriggerLabel);
        printerSendToOfdCountTriggerLabel.setText(CommonSettingsUtils.CommonSettingsEntities.Comments.printerSendToOfdCountTrigger);
        printerSendToOfdCountTriggerView = (EditText) header.findViewById(R.id.printerSendToOfdCountTriggerView);
        printerSendToOfdCountTriggerView.setText(String.valueOf(commonSettings.getPrinterSendToOfdCountTrigger()));
        TextView printerSendToOfdPeriodTriggerLabel = (TextView) header.findViewById(R.id.printerSendToOfdPeriodTriggerLabel);
        printerSendToOfdPeriodTriggerLabel.setText(CommonSettingsUtils.CommonSettingsEntities.Comments.printerSendToOfdPeriodTrigger);
        printerSendToOfdPeriodTriggerView = (EditText) header.findViewById(R.id.printerSendToOfdPeriodTriggerView);
        printerSendToOfdPeriodTriggerView.setText(String.valueOf(commonSettings.getPrinterSendToOfdPeriodTrigger()));

        initialize();
    }

}

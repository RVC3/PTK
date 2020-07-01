package ru.ppr.cppk.ui.fragment.pd.simple;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.ppr.barcodereal.MobileBarcodeReader;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SeasonForDaysPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SeasonForPeriodPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SimplePdViewParams;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SinglePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SurchargeSinglePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.TicketPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.zoom.ZoomPdFragment;
import ru.ppr.cppk.ui.helper.DateListStringifier;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.dao.StationDao;
import ru.ppr.nsi.entity.Station;

/**
 * Фрагмент со считанным ПД.
 */
public class SimplePdFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(SimplePdFragment.class);

    // region Views
    private Button saleExtraChargeBtn;
    private Button saleTransferBtn;
    private Button validPdBtn;
    private Button notValidPdBtn;
    private TextView depStationName;
    private TextView destStationName;
    private ImageView directionImage;
    private TextView pdNumber;
    private TextView pdTitle;
    private TextView pdError;
    private TextView pdDateTimeLabel;
    private TextView pdDateTimeValue;
    private TextView trainCategoryName;
    private ViewGroup exemptionLayout;
    private TextView exemptionLabel;
    private TextView exemptionValue;
    private ViewGroup validityPeriodLayout;
    private TextView validityPeriodLabel;
    private TextView validityPeriodFromLabel;
    private TextView validityPeriodFromValue;
    private TextView validityPeriodToLabel;
    private TextView validityPeriodToValue;
    private TextView validityPeriodDash;
    private TextView validityLabel;
    // endregion
    //region Other
    private boolean validityFromError;
    private boolean validityToError;
    private SimplePdViewParams simplePdViewParams;
    private Callback callback;
    //endregion

    public static SimplePdFragment newInstance() {
        return new SimplePdFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        //add mobile QR code
        MobileBarcodeReader mobileBarcodeReader = MobileBarcodeReader.getInstance();
        final boolean mobileTicket = mobileBarcodeReader.getIfLastCodeMobile();
        //если продажа и длинное название, тогда используем режим 2х ПД
        if (simplePdViewParams != null && simplePdViewParams.isSmallSize()  && !mobileTicket) {
            //все мелкое
            view = inflater.inflate(R.layout.one_off_pd_fragment, container, false);
            if (simplePdViewParams.isZoomEnabled()) {
                ((SystemBarActivity) getActivity()).addLockedView(view);
                view.setOnClickListener(v -> onFullViewClicked());
            }
        } else {
            //все крупное
            view = inflater.inflate(R.layout.one_off_pd_fragment_for_one_pd, container, false);

            if (mobileTicket) {

                view.findViewById(R.id.mobile_barcode_layout).setVisibility(View.VISIBLE);

                ((TextView) view.findViewById(R.id.mobile_station_activation_time)).setText(mobileBarcodeReader.getLastTime());

                StationDao stationDao = getNsiDaoSession().getStationDao();

                int nsiVer = Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId();
                long stationCode = mobileBarcodeReader.getLastStationCode();
                Station station = stationDao.load(stationCode, nsiVer);

                String stationName = "---";
                if (station != null)
                    stationName = station.getShortName();

                TextView tv = ((TextView) view.findViewById(R.id.mobile_staion_activation_name));
                tv.setText(stationName);

                List<Long> barcodeList = mobileBarcodeReader.used_keys.get(mobileBarcodeReader.getLastCode());
                if (barcodeList != null && barcodeList.size() > 1) {
                    view.findViewById(R.id.mobile_barcode_layout_invalid_caption).setVisibility(View.VISIBLE);

                    StringBuilder activations = new StringBuilder();
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss dd.MM\n");
                    for (Long l : mobileBarcodeReader.used_keys.get(mobileBarcodeReader.getLastCode())) {
                        activations.append(format.format(new Date(l)));
                    }

                    SimpleDialog listActivationDialog = SimpleDialog.newInstance(
                            null,
                            activations.toString(),
                            getString(R.string.Yes),
                            null,
                            LinearLayout.HORIZONTAL,
                            0);

                    //simpleDialog.setCancelable(false);
                    listActivationDialog.setDialogNegativeBtnClickListener(
                            (dialog, dialogId) -> dialog.dismiss());

                    listActivationDialog.setOnCancelListener(
                            DialogInterface::dismiss
                    );

                    Button listActivationButton = (Button) view.findViewById(R.id.mobile_ticket_register_history);
                    listActivationButton.setVisibility(View.VISIBLE);
                    listActivationButton.setOnClickListener(v ->
                            listActivationDialog.show(getFragmentManager(), SystemBarActivity.DIALOG_ATTENTION_CLOSE_SHIFT));
                    listActivationButton.setVisibility(View.VISIBLE);


                   // Logger.info(TAG, "============" + argPD.numberPD);
                } else {
                    view.findViewById(R.id.mobile_barcode_layout_valid_caption).setVisibility(View.VISIBLE);//setBackgroundColor(0xff0000);
                    //Logger.info(TAG, "============ Номера билета ещё не было  " + argPD.numberPD);
                }

                for (Map.Entry<String, List<Long>> e : mobileBarcodeReader.used_keys.entrySet()) {
                    Logger.info(TAG, "===== entry " + e.getKey() + "  " + Arrays.toString(e.getValue().toArray()));
                }

                //   mobile_barcode_layout_valid_caption
            }


        }

        ////////////////////////////////
        saleExtraChargeBtn = (Button) view.findViewById(R.id.saleExtraChargeBtn);
        saleExtraChargeBtn.setOnClickListener(v -> onSellSurchargeBtnClicked());
        saleTransferBtn = (Button) view.findViewById(R.id.saleTransferBtn);
        saleTransferBtn.setOnClickListener(v -> onSaleTransferBtnClicked());
        validPdBtn = (Button) view.findViewById(R.id.isValidPd);
        if (validPdBtn != null) {
            validPdBtn.setOnClickListener(v -> onPdValidBtnClicked());
        }
        notValidPdBtn = (Button) view.findViewById(R.id.isNotValidPd);
        notValidPdBtn.setOnClickListener(v -> onPdNotValidBtnClicked());
        depStationName = (TextView) view.findViewById(R.id.depStationName);
        destStationName = (TextView) view.findViewById(R.id.destStationName);
        directionImage = (ImageView) view.findViewById(R.id.directionImage);
        pdNumber = (TextView) view.findViewById(R.id.pdNumber);
        pdTitle = (TextView) view.findViewById(R.id.pdTitle);
        pdError = (TextView) view.findViewById(R.id.pdError);
        pdDateTimeLabel = (TextView) view.findViewById(R.id.pdDateTimeLabel);
        pdDateTimeValue = (TextView) view.findViewById(R.id.pdDateTimeValue);
        trainCategoryName = (TextView) view.findViewById(R.id.trainCategoryName);
        exemptionLayout = (ViewGroup) view.findViewById(R.id.exemptionLayout);
        exemptionLabel = (TextView) view.findViewById(R.id.exemptionLabel);
        exemptionValue = (TextView) view.findViewById(R.id.exemptionValue);
        validityPeriodLayout = (ViewGroup) view.findViewById(R.id.validityPeriodLayout);
        validityPeriodLabel = (TextView) view.findViewById(R.id.validityPeriodLabel);
        validityPeriodFromLabel = (TextView) view.findViewById(R.id.validityPeriodFromLabel);
        validityPeriodFromValue = (TextView) view.findViewById(R.id.validityPeriodFromValue);
        validityPeriodToLabel = (TextView) view.findViewById(R.id.validityPeriodToLabel);
        validityPeriodToValue = (TextView) view.findViewById(R.id.validityPeriodToValue);
        validityPeriodDash = (TextView) view.findViewById(R.id.validityPeriodDash);
        validityLabel = (TextView) view.findViewById(R.id.validityLabel);
        ////////////////////////////////

        // Отображаем модель
        setPdViewModel(simplePdViewParams.getPdViewModel());
        // Настраиваем видимость кнопок
        setButtonsVisibility(simplePdViewParams);

        return view;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setSimplePdViewParams(SimplePdViewParams simplePdViewParams) {
        this.simplePdViewParams = simplePdViewParams;
    }

    private void setButtonsVisibility(SimplePdViewParams params) {
        if (validPdBtn != null) {
            validPdBtn.setVisibility(params.isTicketValidBtnVisible() ? View.VISIBLE : View.GONE);
        }
        notValidPdBtn.setVisibility(params.isTicketNotValidBtnVisible() ? View.VISIBLE : View.GONE);
        saleExtraChargeBtn.setVisibility(params.isSellSurchargeBtnVisible() ? View.VISIBLE : View.GONE);
        saleTransferBtn.setVisibility(params.isSellTransferBtnVisible() ? View.VISIBLE : View.GONE);
    }

    private void setPdViewModel(TicketPdViewModel pdViewModel) {
        setTicketPdViewModel(pdViewModel);
        if (pdViewModel instanceof SinglePdViewModel) {
            setSinglePdViewModel((SinglePdViewModel) pdViewModel);
            if (pdViewModel instanceof SurchargeSinglePdViewModel) {
                setSurchargeSinglePdViewModel((SurchargeSinglePdViewModel) pdViewModel);
            }
        } else if (pdViewModel instanceof SeasonForPeriodPdViewModel) {
            setSeasonForPeriodPdViewModel((SeasonForPeriodPdViewModel) pdViewModel);
        } else if (pdViewModel instanceof SeasonForDaysPdViewModel) {
            setSeasonForDaysPdViewModel((SeasonForDaysPdViewModel) pdViewModel);
        } else {
            throw new IllegalArgumentException("Unsupported pdViewModel");
        }
    }

    private void setTicketPdViewModel(TicketPdViewModel pdViewModel) {
        // Устанавливаем номер ПД
        pdNumber.setText(String.format(getString(R.string.number_for_pd), pdViewModel.getNumber()));
        // Устанавливаем станцию отправления
        if (pdViewModel.getDepStationName() == null) {
            depStationName.setText(getString(R.string.not_found));
        } else {
            depStationName.setText(pdViewModel.getDepStationName());
        }
        // Устанавливаем станцию назначения
        if (pdViewModel.getDestStationName() == null) {
            destStationName.setText(getString(R.string.not_found));
        } else {
            destStationName.setText(pdViewModel.getDestStationName());
        }
        // Устанавливаем тип ПД
        pdTitle.setText(pdViewModel.getTitle());
        // Устанавливаем льготу
        if (pdViewModel.getExemptionExpressCode() == 0) {
            exemptionValue.setText(R.string.simple_pd_no_exemption_for_any_pd);
        } else {
            exemptionValue.setText(String.valueOf(pdViewModel.getExemptionExpressCode()));
            exemptionValue.setTextColor(getResources().getColor((R.color.green)));
        }
        // Устанавливаем категорию поезда
        trainCategoryName.setText(pdViewModel.getTrainCategoryName());
        // Для трансфера скрываем информацию о льготе
        exemptionLayout.setVisibility(pdViewModel.isTransfer() ? View.GONE : View.VISIBLE);
        // Для трансфера скрываем информацию о категории поезда
        trainCategoryName.setVisibility(pdViewModel.isTransfer() ? View.GONE : View.VISIBLE);
        // Отображаем валидность ПД
        setValid(pdViewModel.isValid());
        // Отображаем валидность маршрута
        setRouteError(pdViewModel.isRouteError());
        // Отображаем валидность категории поезда
        setTrainCategoryError(pdViewModel.isTrainCategoryError());

        // Отображаем сообщение об ошибке, если нужно
        if (pdViewModel.isInvalidEdsKeyError()) {
            pdError.setVisibility(View.VISIBLE);
            pdError.setText(R.string.read_bsc_error_ecp_error);
        } else if (pdViewModel.isRevokedEdsKeyError()) {
            pdError.setVisibility(View.VISIBLE);
            pdError.setText(R.string.read_bsc_error_ecp_revoked);
        } else if (pdViewModel.isTicketInStopListError()) {
            pdError.setVisibility(View.VISIBLE);
            pdError.setText(R.string.read_bsc_error_pd_in_stop_list);
        } else if (pdViewModel.isTicketAnnulledError()) {
            pdError.setVisibility(View.VISIBLE);
            pdError.setText(R.string.read_bsc_error_is_annulled);
        }
    }

    private void setSinglePdViewModel(SinglePdViewModel pdViewModel) {
        // Обновляем сообщение о валидности
        if (pdViewModel.isValid() && pdViewModel.isSoldNow()) {
            validityLabel.setBackgroundResource(R.color.simple_pd_written);
            validityLabel.setText(R.string.simple_pd_written_pd);
        }
        // Устанавливаем заголовок для даты действия ПД
        pdDateTimeLabel.setText(pdViewModel.isControlMode() ? R.string.date_title : R.string.sale_date_title);
        // Устанавливаем направление
        if (pdViewModel.isTwoWay()) {
            directionImage.setImageResource(R.drawable.ic_direction_there_back);
        } else {
            directionImage.setImageResource(R.drawable.ic_direction_there);
        }
        // Устанавливаем дату действия ПД
        if (pdViewModel.getValidityDate() == null) {
            pdDateTimeValue.setText(getString(R.string.not_found));
        } else {
            if (pdViewModel.isControlMode()) {
                pdDateTimeValue.setText(DateFormatOperations.getDateddMMyyyy(pdViewModel.getValidityDate()));
            } else {
                pdDateTimeValue.setText(DateFormatOperations.getDateddMMyyyyHHmm(pdViewModel.getValidityDate()));
            }
        }
        // Отображаем валидность даты действия ПД
        setValidityDateError(pdViewModel.isValidityDateError());
    }

    private void setSurchargeSinglePdViewModel(SurchargeSinglePdViewModel pdViewModel) {
        // Устанавливаем льготу (Переписываем значение после заполнения базовых полей)
        if (pdViewModel.getExemptionExpressCode() == 0) {
            // https://aj.srvdev.ru/browse/CPPKPP-28473
            // при чтении доплаты в поле "Льгота" выводить прочерк вместо слова "нет"
            exemptionValue.setText(R.string.simple_pd_no_exemption_for_fare_pd);
        }
        // Затираем категорию поезда и пишем туда номер родительского ПД
        trainCategoryName.setText(String.format(getString(R.string.fare_category), pdViewModel.getParentPdNumber()));
    }

    private void setSeasonForPeriodPdViewModel(SeasonForPeriodPdViewModel pdViewModel) {
        // Устанавливаем направление
        directionImage.setImageResource(R.drawable.ic_direction_there_back);
        // Скрываем поля развого ПД
        pdDateTimeLabel.setVisibility(View.GONE);
        pdDateTimeValue.setVisibility(View.GONE);
        // Устанавливаем период действия ПД
        validityPeriodLayout.setVisibility(View.VISIBLE);
        validityPeriodFromValue.setText(DateFormatOperations.getOutDate(pdViewModel.getValidityFromDate()));
        validityPeriodToValue.setText(DateFormatOperations.getOutDate(pdViewModel.getValidityToDate()));
        // Отображаем валидность даты действия ПД
        if(pdViewModel.isWeekendOnlyError()) {
            // Выделяем цветом заголовок, если ошибка "абонемент только на выходные"
            setDefaultTextViewError(pdTitle, true, false);
        } else if(pdViewModel.isWorkingDayOnlyError()) {
            // Выделяем цветом заголовок, если ошибка "абонемент только по рабочим дням"
            setDefaultTextViewError(pdTitle, true, false);
        } else {
            setDefaultTextViewError(pdTitle, false, false);
        }
        setValidityFromError(pdViewModel.isValidityFromDateError());
        setValidityToError(pdViewModel.isValidityToDateError());
    }

    private void setSeasonForDaysPdViewModel(SeasonForDaysPdViewModel pdViewModel) {
        // Устанавливаем направление
        directionImage.setImageResource(R.drawable.ic_direction_there_back);
        // Устанавливаем заголовок для даты действия ПД
        pdDateTimeLabel.setText(R.string.date_actions_title);
        // Устанавливаем даты действия ПД
        String daysString = new DateListStringifier().stringify(pdViewModel.getValidityDates());
        pdDateTimeValue.setText(daysString);
        // Отображаем валидность даты действия ПД
        setValidityDateError(pdViewModel.isValidityDatesError());
    }

    private void setValid(boolean valid) {
        if (valid) {
            validityLabel.setBackgroundResource(R.color.simple_pd_success);
            validityLabel.setText(R.string.simple_pd_valid_pd);
        } else {
            validityLabel.setBackgroundResource(R.color.simple_pd_error);
            validityLabel.setText(R.string.simple_pd_invalid_pd);
        }

        pdTitle.setEnabled(valid);
        pdNumber.setEnabled(valid);
        depStationName.setEnabled(valid);
        destStationName.setEnabled(valid);
        directionImage.setEnabled(valid);
        trainCategoryName.setEnabled(valid);
        pdDateTimeLabel.setEnabled(valid);
        pdDateTimeValue.setEnabled(valid);
        validityLabel.setEnabled(valid);
        validityPeriodLabel.setEnabled(valid);
        if (validityPeriodFromLabel != null) {
            validityPeriodFromLabel.setEnabled(valid);
        }
        validityPeriodFromValue.setEnabled(valid);
        if (validityPeriodDash != null) {
            validityPeriodDash.setEnabled(valid);
        }
        if (validityPeriodToLabel != null) {
            validityPeriodToLabel.setEnabled(valid);
        }
        validityPeriodToValue.setEnabled(valid);
        exemptionLabel.setEnabled(valid);
        exemptionValue.setEnabled(valid);
    }

    private void setDefaultTextViewError(TextView textView, boolean error, boolean changeStyle) {
        if (error) {
            textView.setTextColor(getResources().getColor(R.color.simple_pd_error));
        } else {
            textView.setTextColor(getResources().getColorStateList(R.color.count_trips_normal));
        }
        if (changeStyle) {
            textView.setTypeface(textView.getTypeface(), error ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void setRouteError(boolean error) {
        setDefaultTextViewError(depStationName, error, false);
        setDefaultTextViewError(destStationName, error, false);
    }

    private void setValidityDateError(boolean error) {
        setDefaultTextViewError(pdDateTimeLabel, error, true);
        setDefaultTextViewError(pdDateTimeValue, error, true);
    }

    private void setValidityFromError(boolean error) {
        this.validityFromError = error;
        setDefaultTextViewError(validityPeriodFromValue, error, true);
        updateValidityLabel();
    }

    private void setValidityToError(boolean error) {
        this.validityToError = error;
        setDefaultTextViewError(validityPeriodToValue, error, true);
        updateValidityLabel();
    }

    private void updateValidityLabel() {
        setDefaultTextViewError(validityPeriodLabel, validityFromError || validityToError, true);
    }

    private void setTrainCategoryError(boolean error) {
        setDefaultTextViewError(trainCategoryName, error, true);
    }

    private void onFullViewClicked() {
        callback.onZoomDialogShown();
        ZoomPdFragment zoomPdFragment = ZoomPdFragment.newInstance();
        zoomPdFragment.setCancelable(true);
        zoomPdFragment.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        zoomPdFragment.setPdViewModel(simplePdViewParams.getPdViewModel());
        zoomPdFragment.setInteractionListener(() -> callback.onZoomDialogHidden());
    }

    private void onPdValidBtnClicked() {
        Logger.info(TAG, "onPdValidBtnClicked");
        callback.onPdValidBtnClicked();
    }

    private void onSaleTransferBtnClicked() {
        Logger.trace(TAG, "onSaleTransferBtnClicked");
        callback.onSaleTransferBtnClicked();
    }

    private void onPdNotValidBtnClicked() {
        Logger.info(TAG, "onPdNotValidBtnClicked");
        callback.onPdNotValidBtnClicked();
    }

    private void onSellSurchargeBtnClicked() {
        Logger.trace(TAG, "onSellSurchargeBtnClicked");
        callback.onSellSurchargeBtnClicked();
    }

    interface Callback {
        void onPdValidBtnClicked();

        void onSaleTransferBtnClicked();

        void onPdNotValidBtnClicked();

        void onSellSurchargeBtnClicked();

        void onZoomDialogShown();

        void onZoomDialogHidden();
    }

}
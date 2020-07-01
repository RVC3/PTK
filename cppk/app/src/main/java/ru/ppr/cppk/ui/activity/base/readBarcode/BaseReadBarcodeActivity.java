package ru.ppr.cppk.ui.activity.base.readBarcode;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.pd.utils.reader.OnRepeatRead;
import ru.ppr.cppk.pd.utils.reader.ReadBarcodeData;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.fragment.readPd.ReadInProgressFragment;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketCategory;

/**
 * Запускает активити с обратным отсчетом и начинает считывание ПД с носителя.
 * Если считывание не удалось, т.е. в качестве результата вернулся null, то
 * показывает фрагмент с ошибкой. Если считывание удалось, т.е. list != null, то
 * возвращает данные в вызывающую активити. В случае если при чтении ПД
 * произошла ошибка и пользователь нажимает кнопку назад, то вызывающей активити
 * возвращается null. Этот null обрабатывать никак не надо, т.к. сообщение об
 * ошибке были показаны в данной активити
 * <p>
 * UPD: базовый класс делающий описанное выше с некоторыми изменениями: являющийся обобщением для ситуаций
 * когда надо отрпавить результат выполнения в кастомное место, теперь активити не
 * возвращает результат в onActivityResult вызывающей активити, а оставляет этот функционал наследникам
 *
 * @author A.Ushakov *
 */
public abstract class BaseReadBarcodeActivity extends SystemBarActivity implements OnRepeatRead, CppkDialogFragment.CppkDialogClickListener {

    private static final String TAG = Logger.makeLogTag(BaseReadBarcodeActivity.class);

    private ReadBarcodeData readBarcodeData;
    private BaseReadBarcodeDi di;
    private boolean isAlreadyRead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.read_pd_activity);

        di = new BaseReadBarcodeDi(di());

        resetRegisterReceiver();
        canUserHardwareButton();

        readBarcodeData = new ReadBarcodeData(
                di.readBarcodeTaskFactory(),
                di.pdHandler(),
                di.readCouponFromBarcodeHandler(),
                new ReadBarcodeData.Listener() {

                    @Override
                    public void onPdListRead(@NonNull List<PD> pdList) {
                        di.uiThread().post(() -> {
                            Logger.info(TAG, "onPdListRead() pdList.size()=" + pdList.size());

                            returnResult(pdList, null);
                        });
                    }

                    @Override
                    public void onCouponRead(long couponReadEventId) {
                        di.uiThread().post(() -> {
                            Logger.info(TAG, "onCouponRead() coupon: " + couponReadEventId);

                            returnResult(null, couponReadEventId);
                        });
                    }

                    @Override
                    public void onError() {
                        di.uiThread().post(() -> {
                            errorReadPd();
                            isAlreadyRead = false;
                        });
                    }

                    @Override
                    public void onCancelled() {
                        finish();
                    }
                },
                Dagger.appComponent().pdControlBarcodeDataStorage());

        startRead();
    }

    /**
     * Устанавливает фрагмент с таймером обратного отсчета и запускает
     * считывание ПД
     */
    private void startRead() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = ReadInProgressFragment.newInstance(ReaderType.TYPE_BARCODE);
        fragmentTransaction.replace(R.id.read_pd_fragment_container, fragment).commit();

        isAlreadyRead = true;
        readBarcodeData.runPdRead();
    }

    /**
     * Заменяет фрагмент с таймером на фрагмент с информацией об ошибке
     */
    private void errorReadPd() {
        onReadError();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            Logger.error(TAG, "Нажата кнопка Power");
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Отдаёт результат в точку возврата результата
     */
    private void returnResult(@Nullable List<PD> pdList, @Nullable Long couponReadEventId) {
        ReadBarcodeResult readBarcodeResult = new ReadBarcodeResult();
        readBarcodeResult.setPdList(pdList);
        readBarcodeResult.setCouponReadEventId(couponReadEventId);

        onBarcodeResultRead(readBarcodeResult);
    }

    @Override
    protected void onPause() {
        cancelRead();
        super.onPause();
    }

    @Override
    public void repeatRead() {
        startRead();
    }

    @Override
    public void onClickRfrid() {
        // запускался ридер из текущей активити, теперь так делать нельзя
    }

    @Override
    public void onClickBarcode() {
        if (!isAlreadyRead) {
            this.startRead();
        }
    }

    public void cancelRead() {
        Logger.trace(TAG, "cancelRead()");
        if (readBarcodeData != null) {
            readBarcodeData.cancel();
        }
    }

    public void stopRead() {
        Logger.trace(TAG, "stopRead()");
        if (readBarcodeData != null) {
            readBarcodeData.stop();
        }
    }

    @Override
    public void onClickSettings() {
        // Переопределяем метод чтобы нельзя было выйти в меню с этого окна
    }

    @Override
    public void onPositiveClick(DialogFragment dialog, int dialogId) {
        PdSaleParams pdSaleParams = new PdSaleParams();
        pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
        pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
        Navigator.navigateToPdSaleActivity(this, pdSaleParams);
        finish();
    }

    @Override
    public void onNegativeClick(DialogFragment dialog, int dialogId) {
        finish();
    }

    protected abstract void onBarcodeResultRead(@NonNull ReadBarcodeResult readBarcodeResult);

    protected abstract void onReadError();

}

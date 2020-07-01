package ru.ppr.cppk.ui.activity.readpdfortransfer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.logger.Logger;


/**
 * Экран чтения БСК.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadPdForTransferActivity extends MvpActivity implements ReadPdForTransferView {

    private static final String TAG = Logger.makeLogTag(ReadPdForTransferActivity.class);

    //region Extras
    private static final String PARAMS = "PARAMS";
    //endregion

    public static Intent getCallingIntent(Context context, ReadForTransferParams params) {
        Intent intent = new Intent(context, ReadPdForTransferActivity.class);
        intent.putExtra(PARAMS, params);
        return intent;
    }

    // region Di
    private ReadPdForTransferComponent component;
    // endregion
    // region Views
    private Button readBscBtn;
    private Button readBarcodeBtn;
    //endregion
    //region Other
    private ReadPdForTransferPresenter presenter;
    /**
     * Данные для оформления трансфера по считанному ПД
     */
    private ReadForTransferParams readForTransferParams;
    //endregion


    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerReadPdForTransferComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::repealReadBscPresenter, ReadPdForTransferPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        readForTransferParams = getIntent().getParcelableExtra(PARAMS);
        Logger.trace(TAG, "readForTransferParams: " + readForTransferParams);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_read_pd_for_transfer);
        readBscBtn = (Button) findViewById(R.id.readBscBtn);
        readBscBtn.setOnClickListener(v -> presenter.onReadBscBtnClicked());
        readBarcodeBtn = (Button) findViewById(R.id.readBarcodeBtn);
        readBarcodeBtn.setOnClickListener(v -> presenter.onReadBarcodeBtnClicked());
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(navigator);
        presenter.initialize();
    }

    @Override
    public void onClickRfrid() {
        presenter.onReadBscBtnClicked();
    }

    @Override
    public void onClickBarcode() {
        presenter.onReadBarcodeBtnClicked();
    }

    @Override
    public void onClickSettings() {
        // Переопределяем метод чтобы нельзя было выйти в меню с этого окна
    }

    private final ReadPdForTransferPresenter.Navigator navigator = new ReadPdForTransferPresenter.Navigator() {
        @Override
        public void navigateToReadBsc() {
            if (isActivityResumed())
                startReadPd(ReaderType.TYPE_BSC, readForTransferParams);
        }

        @Override
        public void navigateToReadBarcode() {
            if (isActivityResumed())
                startReadPd(ReaderType.TYPE_BARCODE, readForTransferParams);
        }
    };
}

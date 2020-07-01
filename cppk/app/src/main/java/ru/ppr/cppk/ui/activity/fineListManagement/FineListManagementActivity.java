package ru.ppr.cppk.ui.activity.fineListManagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.activity.base.SimpleMvpActivity;
import ru.ppr.cppk.ui.adapter.spinner.FineListManagementAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.nsi.entity.Fine;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Данная активити позволяет управлять списком штрафов,
 * под управлением подразумевается как редактирования списка,
 * так и просто просмотр (зависит от прав попавшего на экран)
 * <p>
 * см. http://agile.srvdev.ru/browse/CPPKPP-34711
 *
 * @author Dmitry Nevolin
 */
public class FineListManagementActivity extends SimpleMvpActivity implements FineListManagementView {

    private static final String FINE_LIST_IS_EMPTY_DIALOG_TAG = "FINE_LIST_IS_EMPTY_DIALOG_TAG";

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, FineListManagementActivity.class);
    }

    /**
     * DI
     */
    private FineListManagementDi di;
    /**
     * Adapter for listview
     */
    private FineListManagementAdapter adapter;
    //region Other
    private FineListManagementPresenter presenter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fine_list_management);

        di = new FineListManagementDi(di());
        adapter = new FineListManagementAdapter(this, di.permissionChecker().checkPermission(PermissionDvc.FineListEdit));

        ListView fineListView = (ListView) findViewById(R.id.fine_list_view);
        fineListView.setAdapter(adapter);
        fineListView.setOnItemClickListener((parent, view, position, id) -> {
            Fine fine = adapter.getItem(position);
            boolean isChecked = adapter.inverseAndGetChecked(fine);

            presenter.onFineChecked(fine, isChecked);
        });

        presenter = getMvpDelegate().getPresenter(FineListManagementPresenter::new, FineListManagementPresenter.class);
        getMvpDelegate().bindView();
        presenter.bindInteractionListener(fineListManagementPresenterInteractionListener);
        presenter.initialize(di.localDaoSession(), di.nsiDaoSession(), di.privateSettings(), di.nsiVersionManager(), di.fineRepository());
    }

    @Override
    public void setAllowedFineCodeList(@NonNull List<Long> allowedFineCodeList) {
        adapter.setPreDefinedCheckedCodeList(allowedFineCodeList);
    }

    @Override
    public void setFineList(@NonNull List<Fine> fineList) {
        adapter.setItems(fineList);
    }

    @Override
    public void setFineListIsEmptyDialogVisible(boolean visible) {
        SimpleDialog simpleDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(FINE_LIST_IS_EMPTY_DIALOG_TAG);

        if (visible) {
            if (simpleDialog == null) {
                simpleDialog = SimpleDialog.newInstance(null,
                        getString(R.string.fine_list_management_fine_list_is_empty_dialog_message),
                        getString(R.string.fine_list_management_fine_list_is_empty_dialog_close),
                        null,
                        LinearLayout.VERTICAL,
                        0);
                simpleDialog.setOnDismissListener(dialog -> presenter.onFineListIsEmptyDialogDismiss());
                simpleDialog.show(getFragmentManager(), FINE_LIST_IS_EMPTY_DIALOG_TAG);
            } else {
                simpleDialog.setOnDismissListener(dialog -> presenter.onFineListIsEmptyDialogDismiss());
            }
        } else {
            if (simpleDialog != null) {
                simpleDialog.dismiss();
            }
        }
    }

    private FineListManagementPresenter.InteractionListener fineListManagementPresenterInteractionListener = this::finish;

}

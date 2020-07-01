package ru.ppr.cppk.ui.activity.fineListManagement;

import android.support.annotation.NonNull;

import com.google.common.primitives.Longs;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.core.ui.mvp.presenter.BaseMvpPresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Fine;
import ru.ppr.nsi.repository.FineRepository;

/**
 * @author Dmitry Nevolin
 */
public class FineListManagementPresenter extends BaseMvpPresenter<FineListManagementView> {

    private InteractionListener interactionListener;

    private boolean initialized = false;
    private LocalDaoSession localDaoSession;
    private NsiDaoSession nsiDaoSession;
    private PrivateSettings privateSettings;
    private NsiVersionManager nsiVersionManager;
    private FineRepository fineRepository;
    /**
     * Список кодов разрешенных штрафов
     */
    private List<Long> allowedFineCodeList;

    void initialize(@NonNull LocalDaoSession localDaoSession,
                    @NonNull NsiDaoSession nsiDaoSession,
                    @NonNull PrivateSettings privateSettings,
                    @NonNull NsiVersionManager nsiVersionManager,
                    @NonNull FineRepository fineRepository) {
        if (!initialized) {
            this.initialized = true;
            this.localDaoSession = localDaoSession;
            this.nsiDaoSession = nsiDaoSession;
            this.privateSettings = privateSettings;
            this.nsiVersionManager = nsiVersionManager;
            this.allowedFineCodeList = new ArrayList<>();
            this.fineRepository = fineRepository;

            onInitialize();
        }
    }

    void bindInteractionListener(@NonNull InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void onFineChecked(@NonNull Fine fine, boolean isChecked) {
        if (isChecked && !allowedFineCodeList.contains(fine.getCode())) {
            allowedFineCodeList.add(fine.getCode());
        } else if (!isChecked && allowedFineCodeList.contains(fine.getCode())) {
            allowedFineCodeList.remove(fine.getCode());
        }

        privateSettings.setAllowedFineCodes(Longs.toArray(allowedFineCodeList));

        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
    }

    void onFineListIsEmptyDialogDismiss() {
        interactionListener.returnToPreviousScreen();
    }

    private void onInitialize() {
        long[] allowedFineCodes = privateSettings.getAllowedFineCodes();
        int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();
        List<Fine> fineList = fineRepository.loadAll(nsiVersion);

        if (allowedFineCodes != null) {
            allowedFineCodeList.addAll(Longs.asList(allowedFineCodes));
        }

        view.setAllowedFineCodeList(allowedFineCodeList);
        view.setFineList(fineList);
        view.setFineListIsEmptyDialogVisible(fineList.isEmpty());
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void returnToPreviousScreen();
    }

}

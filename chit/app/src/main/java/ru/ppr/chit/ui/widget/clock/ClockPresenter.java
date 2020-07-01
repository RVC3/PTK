package ru.ppr.chit.ui.widget.clock;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;

/**
 * @author Dmitry Nevolin
 */
class ClockPresenter extends BaseMvpViewStatePresenter<ClockView, ClockViewState> {

    private static final long SECONDS_IN_MINUTE = 60;

    private boolean initialized;

    private Disposable initializedDisposable = Disposables.disposed();

    @Inject
    ClockPresenter(ClockViewState clockViewState) {
        super(clockViewState);
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        long delay = SECONDS_IN_MINUTE - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) % SECONDS_IN_MINUTE;
        initializedDisposable = Observable
                .interval(delay, SECONDS_IN_MINUTE, TimeUnit.SECONDS, AppSchedulers.clock())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> view.setDate(new Date()))
                .subscribe(sequentialNumber -> view.setDate(new Date()));
    }

    @Override
    public void destroy() {
        initializedDisposable.dispose();
        super.destroy();
    }

}

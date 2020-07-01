package ru.ppr.chit.data.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.data.repository.local.base.BaseLocalDbRepository;
import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.localdb.entity.AppPropertyEntity;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
@Singleton
public class AppPropertiesRepositoryImpl extends BaseLocalDbRepository<AppProperties, AppPropertyEntity, String> implements AppPropertiesRepository {

    private static final String TAG = Logger.makeLogTag(AppPropertiesRepositoryImpl.class);

    private final Object lock = new Object();
    private final PublishSubject<Boolean> subject = PublishSubject.create();
    private final AtomicBoolean pendingChange = new AtomicBoolean();
    private AppProperties cache = null;

    @Inject
    AppPropertiesRepositoryImpl(LocalDbManager localDbManager) {
        super(localDbManager);
        // Подписываемся на переподключение к БД
        connectionState()
                .doOnNext(connected -> Logger.trace(TAG, "connectionState = " + connected))
                .filter(Boolean.TRUE::equals)
                // Подписываемся на завершение транзакции БД
                .mergeWith(endsOfTransactions()
                        .doOnNext(aBoolean -> Logger.trace(TAG, "endsOfTransactions"))
                        .filter(aBoolean -> pendingChange.getAndSet(false)))
                .subscribe(aBoolean -> {
                    // Оповещаем всех заинтересованных об изменениях
                    synchronized (lock) {
                        Logger.trace(TAG, "clear cache");
                        cache = null;
                    }
                    subject.onNext(Boolean.TRUE);
                }, throwable -> Logger.error(TAG, throwable));
    }

    @Override
    protected AbstractDao<AppPropertyEntity, String> dao() {
        return daoSession().getAppPropertyEntityDao();
    }

    @Override
    protected LocalDbMapper<AppProperties, AppPropertyEntity> mapper() {
        // Не используется
        return null;
    }

    @NonNull
    @Override
    public AppProperties load() {
        AppProperties local = cache;
        Logger.trace(TAG, "cached?=" + local);
        if (local == null) {
            synchronized (lock) {
                Logger.trace(TAG, "lockCached?=" + cache);
                if (cache == null) {
                    cache = new AppProperties();
                    List<AppPropertyEntity> entities = dao().loadAll();
                    for (AppPropertyEntity entity : entities) {
                        cache.setValue(entity.getKey(), entity.getValue());
                    }
                }
                local = cache;
            }
        }
        // Возвращаем новый инстанс для избежания несанкционированных изменений объекта
        AppProperties appProperties = new AppProperties();
        appProperties.setValues(local.getValues());
        return appProperties;
    }

    @Override
    @Nullable
    public String readKeyValue(@NonNull String key) {
        return load().getValue(key);
    }

    @Override
    public synchronized void writeKeyValue(@NonNull String key, @NonNull String value) {
        cache.setString(key, value);
        dao().insertOrReplace(new AppPropertyEntity(key, value));
    }

    @Override
    public synchronized void deleteKeyValue(@NonNull String key) {
        cache.clearValue(key);
        dao().deleteByKey(key);
    }

    @Override
    public Observable<AppProperties> rxLoad() {
        return subject
                .startWith(Boolean.TRUE)
                .flatMap(aBoolean -> Observable.fromCallable(this::load));
    }

    @Override
    public void merge(@NonNull AppProperties appProperties) {
        beginTransaction();
        try {
            for (Map.Entry<String, String> entry : appProperties.getValues().entrySet()) {
                AppPropertyEntity appPropertyEntity = dao().load(entry.getKey());
                if (appPropertyEntity == null) {
                    appPropertyEntity = new AppPropertyEntity();
                    appPropertyEntity.setKey(entry.getKey());
                    appPropertyEntity.setValue(entry.getValue());
                    dao().insert(appPropertyEntity);
                } else {
                    appPropertyEntity.setValue(entry.getValue());
                    dao().update(appPropertyEntity);
                }
            }
            setTransactionSuccessful();
            pendingChange.set(true);
        } finally {
            endTransaction();
        }
    }
}

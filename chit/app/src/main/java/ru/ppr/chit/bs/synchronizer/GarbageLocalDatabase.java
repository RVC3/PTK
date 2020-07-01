package ru.ppr.chit.bs.synchronizer;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Completable;
import ru.ppr.chit.bs.synchronizer.base.Synchronizer;
import ru.ppr.chit.bs.synchronizer.event.SyncInfoEvent;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.localdb.daosession.LocalDaoEntities;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCListener;
import ru.ppr.logger.Logger;

/**
 * Класс, выполняющий сборку мусора в локальной базе данных
 * Выполняется не чаще 1 раза в месяц
 *
 * @author m.sidorov
 */
public class GarbageLocalDatabase {

    private static final String TAG = Logger.makeLogTag(GarbageLocalDatabase.class);

    private static final int OLD_DATA_MONTH_OFFSET = 14;

    private static final String LAST_GARBAGE_DATE_KEY = "LAST_GARBAGE_DATE";
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private static final long GARBAGE_PERIOD = 30 * ONE_DAY;

    private final Context context;
    private final LocalDbManager localDbManager;
    private final AppPropertiesRepository appPropertiesRepository;
    private final SynchronizerInformer synchronizerInformer;

    @Inject
    GarbageLocalDatabase(Context context,
                         LocalDbManager localDbManager,
                         AppPropertiesRepository appPropertiesRepository,
                         SynchronizerInformer synchronizerInformer) {
        this.context = context;
        this.localDbManager = localDbManager;
        this.appPropertiesRepository = appPropertiesRepository;
        this.synchronizerInformer = synchronizerInformer;
    }

    private void notify(String message) {
        synchronizerInformer.notify(new SyncInfoEvent(Synchronizer.SynchronizeType.GARBAGE, message));
    }

    public Completable execute(){
        return Completable.fromAction(() -> {
                    Date lastDate = readLastGarbageDate();
                    Date now = new Date();
                    // Если последней очистки мусора не было или со времени очистки прошло больше 30 дней, то выполняем очистку
                    if (lastDate == null || now.getTime() - lastDate.getTime() > GARBAGE_PERIOD){
                        Calendar dateBefore = Calendar.getInstance();
                        dateBefore.set(Calendar.DAY_OF_MONTH, 1);
                        dateBefore.add(Calendar.MONTH, OLD_DATA_MONTH_OFFSET * -1);

                        startGarbage(dateBefore.getTime());
                        writeLastGarbageDate(new Date());
                    }
                });
    }

    // Возвращает дату последней сборки мусора
    @Nullable
    private Date readLastGarbageDate() {
        String value = appPropertiesRepository.readKeyValue(LAST_GARBAGE_DATE_KEY);
        try {
            return new Date(Long.parseLong(value));
        } catch (Exception e){
            return null;
        }
    }

    private void writeLastGarbageDate(Date lastDate) {
        appPropertiesRepository.writeKeyValue(LAST_GARBAGE_DATE_KEY, String.valueOf(lastDate.getTime()));
    }

    // Запускает сборку мусора в локальной базе данных
    private void startGarbage(Date dateBefore) {
        GCListener listener = message -> notify(message);
        new DBGarbageCollector(context, localDbManager.daoSession().getDatabase(), LocalDaoEntities.entities, LocalDaoEntities.references, listener).execute(dateBefore);
    }

}

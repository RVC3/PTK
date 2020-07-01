package ru.ppr.chit.domain.model.local;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.core.domain.model.HashMapProperties;
import ru.ppr.core.domain.model.TypedProperties;

/**
 * Параметры приложения времени выполнения
 * Живут в рамках сессии приложения
 * Никуда не сохраняются и не восстанавливаются
 *
 * Created by m.sidorov.
 */

@Singleton
public class AppRuntimeProperty extends TypedProperties {

    private class Names {
        static final String REGISTRATION_BROKEN_ERROR_SHOWN = "REGISTRATION_BROKEN_ERROR_SHOWN";
    }

    @Inject
    public AppRuntimeProperty() {
        super(new HashMapProperties());
    }

    // Признак, что ошибка разрыва соединения с базовой станции была показана пользователю
    public boolean isRegistrationBrokenShown(){
        return getBoolean(Names.REGISTRATION_BROKEN_ERROR_SHOWN, false);
    }

    public void setRegistrationBrokenShown(boolean value){
        setBoolean(Names.REGISTRATION_BROKEN_ERROR_SHOWN, value);
    }

}

package ru.ppr.cppk.pd.check.write;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.di.Dagger;

/**
 * Выполняет проверки билета, необходимые для определения действительности билите, при поиске места
 * на БСК.
 * См. п 4.2.26 Проверка ПД, записанных на БСК ТСОППД ЧТЗ ПТК от 21.08.2014
 * Created by Артем on 16.02.2016.
 */
public class WriteChecker {

    private final PD pd;

    public WriteChecker(@NonNull PD pd) {
        this.pd = pd;
    }

    /**
     * Возвращает результат проверки билета
     *
     * @return true - билет валиден, иначе false
     */
    public boolean isValid() {
        Checker checker = createChecker();
        return checker.performCheck(pd, new Date());
    }

    @NonNull
    private Checker createChecker() {

        PdVersion pdVersion = PdVersion.getByCode(pd.versionPD);

        if (pdVersion == null) {
            return new StubChecker();
        }

        Checker checker;
        switch (pdVersion) {
            case V3:
            case V4:
            case V5:
            case V11:
            case V12:
            case V13:
            case V14:
            case V15:
            case V16:
            case V17:
                checker = Dagger.appComponent().oneOffAndSeasonForPeriodChecker();
                break;

            case V6:
            case V25:
                checker = Dagger.appComponent().seasonForDaysTicketChecker();
                break;

            case V64:
                checker = new TicketCapChecker();
                break;

            default:
                checker = new StubChecker();
        }
        return checker;
    }
}

package ru.ppr.edssft;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;

import ru.ppr.edssft.model.GetStateResult;

/**
 * Контроллер ЭЦП, основанный на SFT.
 *
 * @author Aleksandr Brazhkin
 */
public interface SftEdsChecker extends EdsChecker {


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SFT_STATE_NO_LICENSES,
            SFT_STATE_ONLY_CHECK_LICENSE,
            SFT_STATE_ONLY_SELL_LICENSE,
            SFT_STATE_ALL_LICENSES
    })
    @interface State {
    }

    /**
     * Состояние SFT. Нет лицензий
     */
    int SFT_STATE_NO_LICENSES = 0;
    /**
     * Состояние SFT. Есть только лицензия на проверку подписи
     */
    int SFT_STATE_ONLY_CHECK_LICENSE = 2;
    /**
     * Состояние SFT. Есть только лицензия на создание подписи
     */
    int SFT_STATE_ONLY_SELL_LICENSE = 4;
    /**
     * Состояние SFT. Есть обе лицензии
     */
    int SFT_STATE_ALL_LICENSES = 6;

    /**
     * Возвращает состояние SFT.
     *
     * @return Результат запроса состония SFT.
     */
    GetStateResult getState();

    /**
     * Выполняет загрузку лицензий.
     */
    boolean takeLicenses();

    /**
     * Создает запросы лицензий для указанных типов.
     *
     * @param licTypes Типы лицензий
     */
    void createLicRequest(EnumSet<LicType> licTypes);
}

package ru.ppr.cppk.ui.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.cppk.localdb.model.ShiftEvent;

/**
 * Преобразователь статуса смены в строкое представление.
 *
 * @author Aleksandr Brazhkin
 * @deprecated Заводить строковые android-ресурсы и использовать их в местах отображения.
 */
@Deprecated
public class ShiftEventStatusStringifier {

    public ShiftEventStatusStringifier() {

    }

    /**
     * Преобразует статус смены в строковое представление.
     *
     * @param status Статус смены
     * @return Строковое представление
     */
    @NonNull
    public String stringify(@Nullable ShiftEvent.Status status) {
        if (status == null) {
            return "Неизвестое состояние";
        }
        switch (status) {
            case STARTED:
            case TRANSFERRED:
                return "Открыта";
            case ENDED:
                return "Закрыта";
            default:
                return "Неизвестое состояние";
        }
    }
}

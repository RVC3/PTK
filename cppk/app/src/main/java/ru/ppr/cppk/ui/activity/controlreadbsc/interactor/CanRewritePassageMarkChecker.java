package ru.ppr.cppk.ui.activity.controlreadbsc.interactor;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CppkNumberOfTripsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.AuthCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.IpkReader;

/**
 * Чекер, проверяющий, имеет ли ПТК право зписать на карту метку прохода в случае её отсутствия.
 *
 * @author Aleksandr Brazhkin
 */
public class CanRewritePassageMarkChecker {

    @Inject
    CanRewritePassageMarkChecker() {

    }

    /**
     * Проверяет, имеет ли ПТК право зписать на карту метку прохода в случае её отсутствия.
     *
     * @param cardReader Ридер
     * @return {@code true} если имеет право, {@code false} иначе
     */
    public boolean canPassageMarkBeRewritten(CardReader cardReader) {
        // http://agile.srvdev.ru/browse/CPPKPP-42779
        // Пишем метку на любые карты, где она может быть
        return cardReader instanceof CppkNumberOfTripsReader
                || cardReader instanceof CppkReader
                || cardReader instanceof IpkReader
                || cardReader instanceof AuthCardReader;
    }

}

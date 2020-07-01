package ru.ppr.cppk.ui.activity.controlreadbsc.interactor;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CppkNumberOfTripsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.AuthCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.IpkReader;

/**
 * Чекер, проверяющий, должна ли на карте быть метка прохода с точки зрения логики.
 *
 * @author Aleksandr Brazhkin
 */
public class ShouldHavePassageMarkChecker {

    @Inject
    ShouldHavePassageMarkChecker() {

    }

    /**
     * Проверяет, должна ли на карте быть метка прохода с точки зрения логики.
     *
     * @param cardReader Ридер
     * @return {@code true} если должна, {@code false} иначе
     */
    public boolean shouldHavePassageMark(CardReader cardReader) {
        return cardReader instanceof CppkNumberOfTripsReader
                || cardReader instanceof CppkReader
                || cardReader instanceof IpkReader
                || cardReader instanceof AuthCardReader;
    }

}

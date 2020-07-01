package ru.ppr.cppk.ui.activity.fineSale;

import javax.inject.Inject;

import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.model.FineSaleData;

/**
 * Хранилище {@link FineSaleData} для шаринга между актитити и фрагментами.
 *
 * @author Aleksandr Brazhkin
 */
@ActivityScope
public class FineSaleDataStorage {

    private final FineSaleData fineSaleData = new FineSaleData();

    @Inject
    FineSaleDataStorage(){

    }

    public FineSaleData getFineSaleData() {
        return fineSaleData;
    }
}

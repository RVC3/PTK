package ru.ppr.cppk.model;

import java.util.Collections;
import java.util.List;

import ru.ppr.nsi.entity.Tariff;

/**
 * Цепочка тарифных планов.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffsChain {

    private final List<Tariff> tariffs;

    public TariffsChain(List<Tariff> tariffs) {
        this.tariffs = tariffs;
    }

    public TariffsChain(Tariff tariff) {
        this.tariffs = Collections.singletonList(tariff);
    }

    public List<Tariff> getTariffs() {
        return tariffs;
    }
}

package ru.ppr.core.manager.factory;

import android.support.annotation.NonNull;

import ru.ppr.core.manager.eds.EdsConfig;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.edssft.EdsChecker;
import ru.ppr.edssft.SftEdsChecker;

/**
 * Интерфейс для фабрики контроллера ЭЦП.
 */
public interface IEdsCheckerFactory {

    /**
     * С помощью данного метода можно получить конкретный объект нужного типа для взаимодействия с ЭПЦ.
     *
     * @param edsConfig Конфигурация sft.
     * @return объект нужного типа для взаимодействия с ЭЦП.
     * @see EdsType
     * @see EdsChecker
     */
    SftEdsChecker getEdsChecker(@NonNull final EdsConfig edsConfig);

}

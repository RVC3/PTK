package ru.ppr.cppk.sync.kpp.baseEntities;

import ru.ppr.cppk.sync.kpp.model.Station;
import ru.ppr.cppk.sync.kpp.model.StationDevice;

/**
 * @author Grigoriy Kashka
 */
public abstract class Event {

    /**
     * (заполняется автоматически, если пустой)
     */
    public String Id;

    /**
     * (заполняется автоматически)
     */
    public long CreationTimestamp;

    /**
     * Версия НСИ при использовании которой было создано событие
     */
    public int VersionId;

    /**
     * Версия софта установленного на ПТК
     */
    public String SoftwareVersion;

    /**
     * КО на котором произошло событие
     */
    public StationDevice Device;

    /**
     * Если ПТК работает в режиме Кассы (или хотя бы у него выставлена станция), то это поле будет заполнено.
     */
    public Station Station;

}

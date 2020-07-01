package ru.ppr.cppk.localdb.repository;

import java.util.List;

import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.base.CrudLocalDbRepository;

/**
 * @author Aleksandr Brazhkin
 */
public interface UpdateEventRepository extends CrudLocalDbRepository<UpdateEvent, Long> {
    /**
     * Возвращает список событий обновления для смены/месяца.
     *
     * @param shiftId         Id смены для фильтра
     * @param monthId         Id месяца для фильтра
     * @param updateEventType Тип события для фильтра
     */
    List<UpdateEvent> getUpdateEventsForShiftOrMonth(String shiftId, String monthId, UpdateEventType updateEventType);

    /**
     * Возвращает текущую для указанного объекта
     */
    UpdateEvent getLastUpdateEvent(UpdateEventType updateEventType, boolean includeSubjectAll);

    /**
     * Вернет флаг актуальности базы безопасности Security
     *
     * @param actualDaysCount
     */
    boolean isStopListVersionValid(int actualDaysCount);
}

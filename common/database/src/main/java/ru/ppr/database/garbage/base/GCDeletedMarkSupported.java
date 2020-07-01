package ru.ppr.database.garbage.base;

/**
 * Интерфейс, который должны реализовывать таблицы,
 * которые поддерживают пометку на удаление (есть поле "deletedMark")
 * обязательно к реализации для таблиц, на которые есть каскадные ссылки
 *
 * @author m.sidorov
 */
public interface GCDeletedMarkSupported {

    // Должен вернуть имя поля для отметки удаления
    String getDeletedMarkField();

}

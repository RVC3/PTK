package ru.ppr.core.dataCarrier.findcardtask;

/**
 * Фабрика для {@link FindCardTaskFactory}.
 *
 * @author Aleksandr Brazhkin
 */
public interface FindCardTaskFactory {
    /**
     * Возвращает новый инстанс {@link FindCardTask}.
     */
    FindCardTask create();
}

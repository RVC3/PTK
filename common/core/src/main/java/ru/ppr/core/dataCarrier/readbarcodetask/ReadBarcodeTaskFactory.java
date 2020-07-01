package ru.ppr.core.dataCarrier.readbarcodetask;

/**
 * Фабрика для {@link ReadBarcodeTaskFactory}.
 *
 * @author Aleksandr Brazhkin
 */
public interface ReadBarcodeTaskFactory {
    /**
     * Возвращает новый инстанс {@link ReadBarcodeTask}.
     */
    ReadBarcodeTask create();
}

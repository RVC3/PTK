package ru.ppr.cppk.sync.writer.base;

import java.io.IOException;

/**
 * Базовый Writer
 *
 * @param <T> - тип Объекта с которым работает экземплят Writer-а
 * @author Grigoriy Kashka
 */
public abstract class BaseWriter<T> {

    /**
     * Допишет поля в текущий объект
     *
     * @param field
     * @param writer
     * @throws IOException
     */
    abstract public void writeProperties(T field, ExportJsonWriter writer) throws IOException;


    /**
     * Запишет новую сущность или null
     *
     * @param field
     * @param writer
     * @throws IOException
     */
    public void writeField(String fieldName, T field, ExportJsonWriter writer) throws IOException {
        writer.name(fieldName);
        if (field == null) {
            writer.nullValue();
        } else {
            write(field, writer);
        }
    }

    /**
     * Запишет сущность
     *
     * @param event
     * @param writer
     * @throws IOException
     */
    public void write(T event, ExportJsonWriter writer) throws IOException {
        writer.beginObject();
        writeProperties(event, writer);
        writer.endObject();
    }
}

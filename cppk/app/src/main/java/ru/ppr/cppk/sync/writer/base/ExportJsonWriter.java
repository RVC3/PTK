package ru.ppr.cppk.sync.writer.base;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Aleksandr Brazhkin
 */
public interface ExportJsonWriter extends Closeable {

    ExportJsonWriter beginArray() throws IOException;

    ExportJsonWriter endArray() throws IOException;

    ExportJsonWriter beginObject() throws IOException;

    ExportJsonWriter endObject() throws IOException;

    ExportJsonWriter name(String name) throws IOException;

    ExportJsonWriter nullValue() throws IOException;

    ExportJsonWriter value(String value) throws IOException;

    ExportJsonWriter value(String value, boolean escape) throws IOException;

    ExportJsonWriter value(int value) throws IOException;

    ExportJsonWriter value(Integer value) throws IOException;

    ExportJsonWriter value(long value) throws IOException;

    ExportJsonWriter value(Long value) throws IOException;

    ExportJsonWriter value(boolean value) throws IOException;

    ExportJsonWriter value(Boolean value) throws IOException;

    ExportJsonWriter value(BigDecimal value) throws IOException;
}

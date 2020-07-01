package ru.ppr.cppk.sync.writer.base;

import android.util.JsonWriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;

/**
 * @author Aleksandr Brazhkin
 */
public class AndroidExportJsonWriter implements ExportJsonWriter {

    private final JsonWriter writer;

    public AndroidExportJsonWriter(File outputFile) throws FileNotFoundException {
        OutputStream outputStream = new FileOutputStream(outputFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream);
        writer = new JsonWriter(outputStreamWriter);
        writer.setIndent("");
    }

    @Override
    public AndroidExportJsonWriter beginArray() throws IOException {
        writer.beginArray();
        return this;
    }

    @Override
    public AndroidExportJsonWriter endArray() throws IOException {
        writer.endArray();
        return this;
    }

    @Override
    public AndroidExportJsonWriter beginObject() throws IOException {
        writer.beginObject();
        return this;
    }

    @Override
    public AndroidExportJsonWriter endObject() throws IOException {
        writer.endObject();
        return this;
    }

    @Override
    public AndroidExportJsonWriter name(String name) throws IOException {
        writer.name(name);
        return this;
    }

    @Override
    public AndroidExportJsonWriter value(int value) throws IOException {
        writer.value(value);
        return this;
    }

    @Override
    public AndroidExportJsonWriter value(Integer value) throws IOException {
        writer.value(value);
        return this;
    }

    @Override
    public AndroidExportJsonWriter value(String value) throws IOException {
        return value(value, false);
    }

    @Override
    public AndroidExportJsonWriter value(String value, boolean escape) throws IOException {
        writer.value(value);
        return this;
    }

    @Override
    public AndroidExportJsonWriter nullValue() throws IOException {
        writer.nullValue();
        return this;
    }

    @Override
    public AndroidExportJsonWriter value(long value) throws IOException {
        writer.value(value);
        return this;
    }

    @Override
    public AndroidExportJsonWriter value(Long value) throws IOException {
        writer.value(value);
        return this;
    }

    @Override
    public AndroidExportJsonWriter value(boolean value) throws IOException {
        writer.value(value);
        return this;
    }

    @Override
    public AndroidExportJsonWriter value(Boolean value) throws IOException {
        writer.value(value);
        return this;
    }

    @Override
    public AndroidExportJsonWriter value(BigDecimal value) throws IOException {
        writer.value(value);
        return this;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}

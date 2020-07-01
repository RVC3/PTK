package ru.ppr.cppk.sync.writer.base;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;

/**
 * @author Aleksandr Brazhkin
 */
public class CustomExportJsonWriter implements ExportJsonWriter {

    private static final byte[] COMMA = ",".getBytes();
    private static final byte[] COLON = ":".getBytes();
    private static final byte[] QUOTE = "\"".getBytes();
    private static final byte[] SQUARE_BRACKET_OPEN = "[".getBytes();
    private static final byte[] SQUARE_BRACKET_CLOSE = "]".getBytes();
    private static final byte[] FIGURE_BRACKET_OPEN = "{".getBytes();
    private static final byte[] FIGURE_BRACKET_CLOSE = "}".getBytes();
    private static final byte[] NULL_VALUE = "null".getBytes();

    private final OutputStream out;

    private boolean shouldAddColon = false;
    private boolean shouldAddComma = false;

    public CustomExportJsonWriter(File outputFile) throws FileNotFoundException {
        OutputStream outputStream = new FileOutputStream(outputFile);
        out = new BufferedOutputStream(outputStream);
    }

    @Override
    public CustomExportJsonWriter beginArray() throws IOException {
        beforeValue();
        out.write(SQUARE_BRACKET_OPEN);
        shouldAddComma = false;
        return this;
    }

    @Override
    public CustomExportJsonWriter endArray() throws IOException {
        out.write(SQUARE_BRACKET_CLOSE);
        shouldAddComma = true;
        return this;
    }

    @Override
    public CustomExportJsonWriter beginObject() throws IOException {
        beforeValue();
        out.write(FIGURE_BRACKET_OPEN);
        shouldAddComma = false;
        return this;
    }

    @Override
    public CustomExportJsonWriter endObject() throws IOException {
        out.write(FIGURE_BRACKET_CLOSE);
        shouldAddComma = true;
        return this;
    }

    @Override
    public CustomExportJsonWriter name(String name) throws IOException {
        beforeName();
        out.write(QUOTE);
        out.write(name.getBytes());
        out.write(QUOTE);
        shouldAddColon = true;
        return this;
    }

    @Override
    public CustomExportJsonWriter nullValue() throws IOException {
        beforeValue();
        out.write(NULL_VALUE);
        shouldAddComma = true;
        return this;
    }

    @Override
    public CustomExportJsonWriter value(String value) throws IOException {
        return value(value, false);
    }

    @Override
    public CustomExportJsonWriter value(String value, boolean escape) throws IOException {
        if (value == null) {
            return nullValue();
        }
        beforeValue();
        out.write(QUOTE);
        out.write(escape ? escape(value).getBytes() : value.getBytes());
        out.write(QUOTE);
        shouldAddComma = true;
        return this;
    }

    @Override
    public CustomExportJsonWriter value(int value) throws IOException {
        beforeValue();
        out.write(String.valueOf(value).getBytes());
        shouldAddComma = true;
        return this;
    }

    @Override
    public CustomExportJsonWriter value(Integer value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value((int) value);
    }

    @Override
    public CustomExportJsonWriter value(long value) throws IOException {
        beforeValue();
        out.write(String.valueOf(value).getBytes());
        shouldAddComma = true;
        return this;
    }

    @Override
    public ExportJsonWriter value(Long value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value((long) value);
    }

    @Override
    public CustomExportJsonWriter value(boolean value) throws IOException {
        beforeValue();
        out.write(String.valueOf(value).getBytes());
        shouldAddComma = true;
        return this;
    }

    @Override
    public ExportJsonWriter value(Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value((boolean) value);
    }

    @Override
    public ExportJsonWriter value(BigDecimal value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        beforeValue();
        out.write(QUOTE);
        out.write(value.toString().getBytes());
        out.write(QUOTE);
        shouldAddComma = true;
        return this;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private void beforeValue() throws IOException {
        if (shouldAddColon) {
            out.write(COLON);
            shouldAddColon = false;
        }
        if (shouldAddComma) {
            out.write(COMMA);
            shouldAddComma = false;
        }
    }

    private void beforeName() throws IOException {
        if (shouldAddComma) {
            out.write(COMMA);
            shouldAddComma = false;
        }
    }

    private String escape(String value) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);

            /*
             * From RFC 4627, "All Unicode characters may be placed within the
             * quotation marks except for the characters that must be escaped:
             * quotation mark, reverse solidus, and the control characters
             * (U+0000 through U+001F)."
             *
             * We also escape '\u2028' and '\u2029', which JavaScript interprets
             * as newline characters. This prevents eval() from failing with a
             * syntax error.
             * http://code.google.com/p/google-gson/issues/detail?id=341
             */
            switch (c) {
                case '"':
                case '\\':
                    sb.append('\\');
                    sb.append(c);
                    break;

                case '\t':
                    sb.append("\\t");
                    break;

                case '\b':
                    sb.append("\\b");
                    break;

                case '\n':
                    sb.append("\\n");
                    break;

                case '\r':
                    sb.append("\\r");
                    break;

                case '\f':
                    sb.append("\\f");
                    break;

                default:
                    sb.append(c);
                    break;
            }

        }
        return sb.toString();
    }

}

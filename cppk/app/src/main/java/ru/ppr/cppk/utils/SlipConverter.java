package ru.ppr.cppk.utils;

import android.support.annotation.NonNull;
import android.util.Base64;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Dmitry Nevolin on 18.02.2016.
 */
public class SlipConverter {

    private static final String separator = "\n";

    public static String toImage(@NonNull List<String> slip) {
        String report = "";

        for(int i = 0; i < slip.size(); i++)
            report += slip.get(i) + (i == slip.size() - 1 ? "" : separator);

        return Base64.encodeToString(report.getBytes(), Base64.DEFAULT);
    }

    public static List<String> fromImage(@NonNull String slip) {
        return Arrays.asList(new String(Base64.decode(slip, Base64.DEFAULT)).split(separator));
    }

}

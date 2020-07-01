package ru.ppr.cppk.logic;

import android.support.annotation.Nullable;

public class StubFnSerialChecker implements FnSerialChecker {
    @Override
    public boolean check(@Nullable String fnSerialFromPrinter) {
        return true;
    }
}

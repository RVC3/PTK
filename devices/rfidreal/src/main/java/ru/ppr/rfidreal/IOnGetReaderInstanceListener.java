package ru.ppr.rfidreal;

import java.util.EventListener;


public interface IOnGetReaderInstanceListener extends EventListener {
    void OnGetReaderInstance(BReader var1);
}

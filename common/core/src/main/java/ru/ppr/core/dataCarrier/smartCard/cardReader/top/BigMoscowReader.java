package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import java.util.List;

public interface BigMoscowReader {
    boolean checkForPD(int sector_one, int sector_two);
    List<byte []> getPds();
}

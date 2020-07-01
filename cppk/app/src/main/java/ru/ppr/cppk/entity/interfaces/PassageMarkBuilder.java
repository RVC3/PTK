package ru.ppr.cppk.entity.interfaces;

import ru.ppr.cppk.dataCarrier.entity.PassageMark;

public interface PassageMarkBuilder {

    byte[] build(PassageMark passageMark);

}

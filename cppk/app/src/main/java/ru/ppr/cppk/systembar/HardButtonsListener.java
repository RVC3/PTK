package ru.ppr.cppk.systembar;

public interface HardButtonsListener {
    /**
     * Вызывается когда обнаружено нажатие на боковую кнопку, которая запускает считывание ПД
     * с карты
     */
    void onClickRfrid();

    /**
     * Вызывается когда обнаружено нажатие на боковую кнопку, которая запускает считывание ПД
     * со штрихкода
     */
    void onClickBarcode();

    /**
     * Вызывается когда обнаружено нажатие на кнопку настроект
     */
    void onClickSettings();
}

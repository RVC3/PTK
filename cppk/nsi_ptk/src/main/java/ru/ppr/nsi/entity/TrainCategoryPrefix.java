package ru.ppr.nsi.entity;

/**
 * Префикс категории поезда.
 */
public enum TrainCategoryPrefix {
    /**
     * Пассажирский
     */
    PASSENGER(6000),
    /**
     * Скорый
     */
    EXPRESS(7000);

    private final int code;

    TrainCategoryPrefix(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Метод для получения префикса категории поезда.
     * Если значение не найдено, то будет возвращен префикс {@link #PASSENGER}.
     *
     * @param code значение префикса категории поезда.
     * @return префикс категории поезда.
     */
    public static TrainCategoryPrefix valueOf(final int code) {
        for (TrainCategoryPrefix prefix : TrainCategoryPrefix.values()) {
            if (prefix.getCode() == code) {
                return prefix;
            }
        }
        return TrainCategoryPrefix.PASSENGER;
    }
}

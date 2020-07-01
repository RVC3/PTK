package ru.ppr.core.dataCarrier.smartCard.coverageArea;

/**
 * Тип зоны действия.
 */
public enum CoverageAreaType {
    /**
     * 1 - весь полигон
     */
    ALL_AREA(1),
    /**
     * 2 - одна станция
     */
    SINGLE_STATION(2),
    /**
     * 3 - оно направление
     */
    SINGLE_DIRECTION(3),
    /**
     * 4 - один производственный участок
     */
    SINGLE_PRODUCTION_SECTION(4);

    private final int code;

    CoverageAreaType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CoverageAreaType getByCode(int code) {
        for (CoverageAreaType coverageAreaType : CoverageAreaType.values()) {
            if (coverageAreaType.getCode() == code) {
                return coverageAreaType;
            }
        }
        return null;
    }
}

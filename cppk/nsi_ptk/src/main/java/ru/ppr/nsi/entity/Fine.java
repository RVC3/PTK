package ru.ppr.nsi.entity;

import java.math.BigDecimal;

import ru.ppr.nsi.NsiDaoSession;

/**
 * Штраф.
 *
 * @author Aleksandr Brazhkin
 */
public class Fine {

    /**
     * Код
     */
    private long code;
    /**
     * Наименование
     */
    private String name;

    /**
     * НДС (проценты)
     */
    private int ndsPercent;

    /**
     * Короткое Наименование.
     */
    private String shortName;
    /**
     * Сумма штрафа (в рублях с точностью до копеек)
     */
    private BigDecimal value;
    /**
     * Код региона
     */
    private String regionCode;
    /**
     * Регион
     */
    private Region region = null;
    public Fine() {

    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
        if (region != null && !region.getCode().equals(regionCode)) {
            region = null;
        }
    }

    public Region getRegion(NsiDaoSession nsiDaoSession, int versionId) {
        Region local = region;
        if (local == null && regionCode != null) {
            synchronized (this) {
                if (region == null) {
                    region = nsiDaoSession.getRegionDao().load(Integer.valueOf(regionCode), versionId);
                }
            }
            return region;
        }
        return local;
    }

    public void setRegion(Region region) {
        this.region = region;
        regionCode = region == null ? null : regionCode;
    }

    public int getNdsPercent() {
        return ndsPercent;
    }

    public void setNdsPercent(int ndsPercent) {
        this.ndsPercent = ndsPercent;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}

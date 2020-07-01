package ru.ppr.nsi.entity;

/**
 * Станция.
 */
public class Station {
    public Station() {
    }

    private Integer ercCode = null;
    private String name = null;
    private String shortName = null;
    private Integer regionCode = null;
    private boolean transitStation = false;
    private Integer code = null;
    private boolean forTransfer = false;
    private boolean canSaleTickets = true;
    private Integer versionId = null;

    public Integer getErcCode() {
        return ercCode;
    }

    public void setErcCode(Integer ercCode) {
        this.ercCode = ercCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(Integer regionCode) {
        this.regionCode = regionCode;
    }

    public boolean isTransitStation() {
        return transitStation;
    }

    public void setTransitStation(boolean transitStation) {
        this.transitStation = transitStation;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public boolean isForTransfer() {
        return forTransfer;
    }

    public void setForTransfer(boolean forTransfer) {
        this.forTransfer = forTransfer;
    }

    public boolean isCanSaleTickets() {
        return canSaleTickets;
    }

    public void setCanSaleTickets(boolean canSaleTickets) {
        this.canSaleTickets = canSaleTickets;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // В будущем возможно может быть ситуация что у станции может быть одинаковый код, имя, но разный участок
        //тогда надо набавить еще сравнение по участку. Это надо уточнить
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Station other = (Station) obj;
        if (code == null) {
            if (other.getCode() != null)
                return false;
        } else if (!code.equals(other.getCode()))
            return false;
        if (name == null) {
            if (other.getName() != null)
                return false;
        } else if (!name.equals(other.getName()))
            return false;
        return true;
    }

}

package ru.ppr.nsi.entity;

import ru.ppr.nsi.NsiDaoSession;

/**
 * Тарифнвй план.
 */
public class TariffPlan {
    public TariffPlan() {
    }

    private Integer trainCategoryCode = null;
    private Integer code = null; // id
    private Integer versionId = null;
    private String carrierCode = null;
    private String shortName = null;
    private boolean isSurcharge = false;
    // private Integer deleteInVersionId = null;

    private Carrier carrier;
    private TrainCategory trainCategory = null;

    public Integer getTrainCategoryCode() {
        return trainCategoryCode;
    }

    public void setTrainCategoryCode(Integer trainCategoryCode) {
        this.trainCategoryCode = trainCategoryCode;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isSurcharge() {
        return isSurcharge;
    }

    public void setSurcharge(boolean surcharge) {
        isSurcharge = surcharge;
    }

    public TrainCategory getTrainCategory(NsiDaoSession nsiDaoSession) {
        if (trainCategory == null)
            trainCategory = nsiDaoSession.getTrainCategoryDao().load(trainCategoryCode, versionId);
        return trainCategory;
    }

    public Carrier getCarrier(NsiDaoSession nsiDaoSession) {
        if (carrier == null)
            carrier = nsiDaoSession.getCarrierDao().load(carrierCode, versionId);
        return carrier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TariffPlan that = (TariffPlan) o;

        if (!code.equals(that.code)) return false;
        return versionId.equals(that.versionId);

    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + versionId.hashCode();
        return result;
    }
}

package ru.ppr.nsi.entity;

import android.support.annotation.NonNull;

import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class RouteToTariffPlan extends BaseNSIObject<Integer> {
    /**
     * Код тарифного плана
     */
    private int tariffPlanCode = -1;
    /**
     * Тарифный план
     */
    private TariffPlan tariffPlan = null;
    /**
     * Номер маршрута
     */
    private String routeCode;

    public RouteToTariffPlan() {

    }

    public int getTariffPlanCode() {
        return tariffPlanCode;
    }

    public void setTariffPlanCode(int tariffPlanCode) {
        this.tariffPlanCode = tariffPlanCode;
        if (tariffPlan != null && !tariffPlan.getCode().equals(tariffPlanCode)) {
            tariffPlan = null;
        }
    }

    /**
     * Возвращает тарифный план
     *
     * @param nsiDaoSession
     * @return
     */
    public TariffPlan getTariffPlan(@NonNull NsiDaoSession nsiDaoSession) {
        TariffPlan local = tariffPlan;
        if (local == null && tariffPlanCode >= 0) {
            synchronized (this) {
                if (tariffPlan == null) {
                    tariffPlan = nsiDaoSession.getTariffPlanDao().load(tariffPlanCode, getVersionId());
                }
            }
            return tariffPlan;
        }
        return local;
    }

    public void setTariffPlan(TariffPlan tariffPlan) {
        this.tariffPlan = tariffPlan;
        this.tariffPlanCode = tariffPlan == null ? -1 : tariffPlan.getCode();
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }
}

package ru.ppr.cppk.logic.fiscalDocStateSync.base;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Dmitry Nevolin
 */
public abstract class FiscalDocInfoBuilder<T> {

    private final Provider<T> targetProvider;
    private final Mapper<T> targetMapper;

    public FiscalDocInfoBuilder(Provider<T> targetProvider, Mapper<T> targetMapper) {
        this.targetProvider = targetProvider;
        this.targetMapper = targetMapper;
    }

    public Info build() {
        BigDecimal total = BigDecimal.ZERO;
        List<T> targetList = targetProvider.provideTargetList();

        for (T target : targetList) {
            total = total.add(targetMapper.toItem(target).getValue());
        }

        return new Info(total);
    }

    protected interface Provider<T> {
        List<T> provideTargetList();
    }

    protected interface Mapper<T> {
        Item toItem(T target);
    }

    public static class Item {

        private BigDecimal value;

        public Item(BigDecimal value) {
            this.value = value;
        }

        public BigDecimal getValue() {
            return value;
        }

    }

    public static class Info {

        private BigDecimal total;

        private Info(BigDecimal total) {
            this.total = total;
        }

        public BigDecimal getTotal() {
            return total;
        }

    }

}

package ru.ppr.chit.domain.model.nsi;

import android.support.annotation.NonNull;

import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCVD;
import ru.ppr.chit.domain.repository.nsi.TicketCategoryRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Тип ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketType implements NsiModelWithCVD<Long> {
    /**
     * Код категории ПД
     */
    private Long ticketCategoryCode;
    /**
     * Категория ПД
     */
    private TicketCategory ticketCategory;
    /**
     * Короткое название типа ПД
     */
    private String shortName;
    /**
     * Код станции
     */
    private Long code;
    /**
     * Версия НСИ, в которой была добавлена запись
     */
    private int versionId;
    /**
     * Версия НСИ, в которой была удалена запись
     */
    private Integer deleteInVersionId;

    //region TicketCategory getters and setters
    public Long getTicketCategoryCode() {
        return ticketCategoryCode;
    }

    public void setTicketCategoryCode(Long ticketCategoryCode) {
        this.ticketCategoryCode = ticketCategoryCode;
        if (ticketCategory != null && !ObjectUtils.equals(ticketCategory.getCode(), ticketCategoryCode)) {
            ticketCategory = null;
        }
    }

    public TicketCategory getTicketCategory(@NonNull TicketCategoryRepository ticketCategoryRepository, int versionId) {
        TicketCategory local = ticketCategory;
        if (local == null && ticketCategoryCode != null) {
            synchronized (this) {
                if (ticketCategory == null) {
                    ticketCategory = ticketCategoryRepository.load(ticketCategoryCode, versionId);
                }
            }
            return ticketCategory;
        }
        return local;
    }

    public void setTicketCategory(TicketCategory ticketCategory) {
        this.ticketCategory = ticketCategory;
        ticketCategoryCode = ticketCategory != null ? ticketCategory.getCode() : null;
    }
    //endregion

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public Long getCode() {
        return code;
    }

    @Override
    public void setCode(Long code) {
        this.code = code;
    }

    @Override
    public int getVersionId() {
        return versionId;
    }

    @Override
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    @Override
    public Integer getDeleteInVersionId() {
        return deleteInVersionId;
    }

    @Override
    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }
}

package ru.ppr.cppk.helpers.controlbscstorage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardPdData;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;

/**
 * Данные, считанные с карты при контроле БСК с ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdControlCardData {

    private CardInformation cardInformation;
    // Легаси поле, требуется в дальнейшем, конвертора нет, убрать при первой возможности
    private BscInformation legacyBscInformation;
    private List<Integer> hwCounters;
    private PassageMark passageMark;
    private MetroWallet troykaWallet;
    private CardPdData cardPdData;
    private List<Pd> pdList;
    // Легаси поле, требуется в дальнейшем, т.к. handler следует вызывать 1 раз, хотя и есть конвертер, убрать при первой возможности
    private List<PD> legacyPdList;
    private byte[] eds;

    public PdControlCardData() {
    }

    @NonNull
    public CardInformation getCardInformation() {
        return cardInformation;
    }

    public void setCardInformation(CardInformation cardInformation) {
        this.cardInformation = cardInformation;
    }

    @NonNull
    public BscInformation getLegacyBscInformation() {
        return legacyBscInformation;
    }

    public void setLegacyBscInformation(BscInformation legacyBscInformation) {
        this.legacyBscInformation = legacyBscInformation;
    }

    @Nullable
    public List<Integer> getHwCounters() {
        return hwCounters;
    }

    public void setHwCounters(List<Integer> hwCounters) {
        this.hwCounters = hwCounters;
    }

    public MetroWallet getTroykaWallet() {
        return troykaWallet;
    }

    public void setTroykaWallet(MetroWallet troykaWallet) {
        this.troykaWallet = troykaWallet;
    }

    @Nullable
    public PassageMark getPassageMark() {
        return passageMark;
    }

    public void setPassageMark(PassageMark passageMark) {
        this.passageMark = passageMark;
    }

    @NonNull
    public List<Pd> getPdList() {
        return pdList;
    }

    public void setPdList(List<Pd> pdList) {
        this.pdList = pdList;
    }

    public List<PD> getLegacyPdList() {
        return legacyPdList;
    }

    public void setLegacyPdList(List<PD> legacyPdList) {
        this.legacyPdList = legacyPdList;
    }

    @Nullable
    public byte[] getEds() {
        return eds;
    }

    public void setEds(byte[] eds) {
        this.eds = eds;
    }

    public void setCardPdData(CardPdData cardPdData) {
        this.cardPdData = cardPdData;
    }

    public CardPdData getCardPdData() {
        return cardPdData;
    }

    @Override
    public String toString() {
        return "PdControlCardData{" +
                "cardInformation=" + cardInformation +
                ", legacyBscInformation=" + legacyBscInformation +
                ", hwCounters=" + hwCounters +
                ", passageMark=" + passageMark +
                ", troykaWallet=" + troykaWallet +
                ", cardPdData=" + cardPdData +
                ", pdList=" + pdList +
                ", legacyPdList=" + legacyPdList +
                ", eds=" + Arrays.toString(eds) +
                '}';
    }
}

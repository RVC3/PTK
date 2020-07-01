package ru.ppr.cppk.dataCarrier;

import ru.ppr.core.dataCarrier.smartCard.entity.AuthCardData;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.entity.AuthCard;
import ru.ppr.utils.ByteUtils;

/**
 * @author Aleksandr Brazhkin
 */
public class AuthCardDataMapper {

    public AuthCardDataMapper() {

    }

    public AuthCard toLegacyAuthCard(AuthCardData authCardData, BscInformation legacyBscInformation, byte[] cardUid, byte[] rawServiceData) {
        AuthCard authCard = new AuthCard(
                authCardData.getRoles(),
                authCardData.getFio(),
                authCardData.getEds(),
                authCardData.getPassword(),
                rawServiceData,
                ByteUtils.concatArrays(authCardData.getLogin(), authCardData.getValidityPeriod()),
                legacyBscInformation,
                cardUid);

        return authCard;
    }
}

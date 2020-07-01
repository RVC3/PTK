package ru.ppr.edssft.stub;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;

import ru.ppr.edssft.LicType;
import ru.ppr.edssft.SftEdsChecker;
import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.edssft.model.GetStateResult;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.edssft.model.VerifySignResult;
import ru.ppr.edssft.stub.hash.Hash;
import ru.ppr.edssft.stub.hash.HashProvider;
import ru.ppr.edssft.stub.hash.HashVariants;
import ru.ppr.logger.Logger;

/**
 * Стабовый контроллер ЭЦП.
 *
 * @author Aleksandr Brazhkin
 */
public class StubSftEdsChecker implements SftEdsChecker {

    private static final String TAG = Logger.makeLogTag(StubSftEdsChecker.class);

    private final long userId;
    private final Hash hash;

    public StubSftEdsChecker(long userId) {
        this.userId = userId;
        hash = HashProvider.provideHash(HashVariants.SHA_512);
    }

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public boolean close() {
        return true;
    }

    @NonNull
    @Override
    public SignDataResult signData(byte[] data, Date signDateTime) {
        Logger.info(TAG, "signData: start");

        long keyNumber = userId;
        byte[] signature = hash.computeHash(data);

        SignDataResult signDataResult = new SignDataResult();
        signDataResult.setSuccessful(true);
        signDataResult.setDescription(null);
        signDataResult.setData(data);
        signDataResult.setSignature(signature);
        signDataResult.setEdsKeyNumber(keyNumber);
        signDataResult.setSignDateTime(signDateTime);

        Logger.info(TAG, "signData: end");

        return signDataResult;
    }

    @NonNull
    @Override
    public GetKeyInfoResult getKeyInfo(long edsKeyNumber) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 42);

        Date effectiveDate = new Date(0);
        Date expireDate = calendar.getTime();
        Date dateOfRevocation = null;

        GetKeyInfoResult getKeyInfoResult = new GetKeyInfoResult();
        getKeyInfoResult.setSuccessful(true);
        getKeyInfoResult.setDescription(null);
        getKeyInfoResult.setEdsKeyNumber(edsKeyNumber);
        getKeyInfoResult.setDeviceId(edsKeyNumber);
        getKeyInfoResult.setEffectiveDate(effectiveDate);
        getKeyInfoResult.setExpireDate(expireDate);
        getKeyInfoResult.setDateOfRevocation(dateOfRevocation);

        return getKeyInfoResult;
    }

    @NonNull
    @Override
    public VerifySignResult verifySign(byte[] data, byte[] signature, long edsKeyNumber) {
        byte[] hashFromData = hash.computeHash(data);
        boolean signValid = Arrays.equals(hashFromData, signature);

        VerifySignResult verifySignResult = new VerifySignResult();
        verifySignResult.setSuccessful(true);
        verifySignResult.setDescription(null);
        verifySignResult.setSignValid(signValid);
        return verifySignResult;
    }

    @Override
    public GetStateResult getState() {
        GetStateResult getStateResult = new GetStateResult();
        getStateResult.setSuccessful(true);
        getStateResult.setState(SFT_STATE_ALL_LICENSES);
        return getStateResult;
    }

    @Override
    public boolean takeLicenses() {
        return true;
    }

    @Override
    public void createLicRequest(EnumSet<LicType> licTypes) {

    }
}

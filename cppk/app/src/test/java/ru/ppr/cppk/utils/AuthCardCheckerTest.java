package ru.ppr.cppk.utils;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;

import ru.ppr.core.logic.FioFormatter;
import ru.ppr.core.manager.eds.CheckSignResult;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.nsi.NsiDbOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.AuthCard;
import ru.ppr.cppk.entity.AuthResult;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.ProductionSectionRepository;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;
import ru.ppr.security.entity.SecurityCard;
import ru.ppr.security.entity.SecuritySettings;
import ru.ppr.security.entity.UserDvc;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by Артем on 28.03.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NsiDbOperations.class, SchedulersCPPK.class})
public class AuthCardCheckerTest {

    //<editor-fold desc="Params">
    byte[] roleArray = new byte[]{
            (byte) 0xE4, 0x05, (byte) 0xA0, (byte) 0xDD, (byte) 0xD4, 0x74, (byte) 0xAE, 0x2A, 0x7A, 0x38, 0x62, 0x16, 0x17, 0x34, (byte) 0x9E, (byte) 0xD3,
            (byte) 0xBC, (byte) 0xF2, (byte) 0xAF, 0x77, (byte) 0x90, (byte) 0xCA, 0x21, (byte) 0x80, 0x77, 0x17, (byte) 0xBB, (byte) 0xF8, 0x66, 0x0F, (byte) 0xFD, (byte) 0xAF,
            0x63, 0x76, (byte) 0xB8, 0x23, 0x23, 0x38, (byte) 0x82, 0x4C, (byte) 0xE7, 0x3A, 0x21, 0x31, 0x32, 0x23, 0x12, (byte) 0xC1,
            0x0B, 0x0A, (byte) 0xEB, (byte) 0xC2, (byte) 0xCA, 0x5B, (byte) 0x9E, (byte) 0x84, (byte) 0x96, 0x0D, 0x6B, 0x78, (byte) 0x89, (byte) 0xCA, (byte) 0xC2, (byte) 0xD8,
            0x33, (byte) 0xD8, 0x0A, 0x2F, 0x24, 0x76, (byte) 0xA4, (byte) 0xBB, 0x0C, (byte) 0xE9, (byte) 0xB5, (byte) 0xFF, 0x5A, 0x0B, 0x5E, 0x6A,
            (byte) 0x8A, 0x3A, (byte) 0xB0, (byte) 0xBA, 0x43, (byte) 0xF1, 0x05, 0x42, (byte) 0xDD, 0x66, 0x55, 0x70, 0x71, (byte) 0xCA, (byte) 0xA5, 0x5D,
            (byte) 0xA4, (byte) 0xAF, (byte) 0x99, 0x53, (byte) 0xDA, 0x06, (byte) 0x94, (byte) 0xC6, 0x77, (byte) 0xC1, (byte) 0xF2, 0x5F, (byte) 0xCD, 0x3E, 0x17, 0x24,
            (byte) 0xD6, (byte) 0xCB, 0x71, 0x61, 0x01, (byte) 0xA4, (byte) 0xFF, 0x1E, (byte) 0xCC, 0x3F, 0x68, (byte) 0xA6, (byte) 0xBF, 0x7E, (byte) 0xCE, 0x19,
            (byte) 0xFB, (byte) 0x8D, 0x11, 0x1F, 0x43, (byte) 0x89, 0x6A, (byte) 0xD4, (byte) 0xD8, (byte) 0xD7, (byte) 0x80, 0x48, (byte) 0xE5, 0x49, (byte) 0xDA, (byte) 0xCF,
            0x4C, (byte) 0xF5, 0x14, (byte) 0xEC, 0x78, 0x35, 0x71, (byte) 0xF3, (byte) 0x83, 0x60, (byte) 0x86, 0x5D, (byte) 0x86, (byte) 0x8D, 0x34, (byte) 0x96,
            0x30, 0x26, 0x72, (byte) 0xBC, (byte) 0xBE, 0x0E, (byte) 0xD7, (byte) 0x99, (byte) 0x81, (byte) 0xC5, (byte) 0xA9, (byte) 0xB1, (byte) 0xF3, (byte) 0x85, 0x23, (byte) 0xE4,
            (byte) 0x9E, 0x1F, (byte) 0x84, (byte) 0xC0, (byte) 0x88, (byte) 0xCD, (byte) 0xD4, (byte) 0xBF, 0x3C, (byte) 0xFC, 0x39, (byte) 0x9C, (byte) 0xC5, 0x68, (byte) 0xDB, 0x01};

    byte[] fioArray = new byte[]{
            (byte) 0xE3, (byte) 0xA0, (byte) 0x85, (byte) 0xCB, 0x23, (byte) 0x89, 0x46, 0x7A, 0x32, (byte) 0xA2, 0x3A, (byte) 0xC7, 0x67, (byte) 0xC0, 0x04, 0x41, 0x17,
            (byte) 0x88, (byte) 0x9E, (byte) 0xAD, (byte) 0xF4, (byte) 0xAA, 0x45, 0x0A, (byte) 0xCB, (byte) 0x91, (byte) 0x81, (byte) 0xD5, (byte) 0xE2, 0x4F, 0x2D, (byte) 0x97, 0x5D, 0x7B,
            0x36, 0x58, (byte) 0xC6, 0x06, 0x2E, (byte) 0xAA, 0x73, 0x08, 0x62, (byte) 0xD1, (byte) 0xD8, 0x42, 0x2D, 0x30
    };

    byte[] ecpArray = new byte[]{
            (byte) 0xDC, 0x45, (byte) 0xB0, (byte) 0x9B, (byte) 0xFE, (byte) 0xA3, 0x09, 0x2E, (byte) 0xAC, (byte) 0x8C, 0x66, (byte) 0xD1, 0x59, (byte) 0x9A, 0x37, 0x1C,
            (byte) 0x85, 0x6E, 0x68, (byte) 0xBF, (byte) 0xD8, 0x5C, 0x13, 0x53, 0x1E, (byte) 0xF9, (byte) 0xB4, (byte) 0x92, (byte) 0x86, (byte) 0xE2, (byte) 0xFB, 0x48,
            (byte) 0x8D, 0x61, (byte) 0x80, (byte) 0xE6, 0x07, 0x0C, 0x0F, 0x11, (byte) 0xF0, (byte) 0x86, 0x15, (byte) 0xF1, (byte) 0xF9, 0x4D, (byte) 0xEF, 0x72,
            (byte) 0xBC, 0x04, 0x59, 0x5B, (byte) 0xD0, (byte) 0x92, 0x5E, (byte) 0x8A, (byte) 0xDA, 0x4D, 0x0E, (byte) 0xDB, 0x5C, 0x51, (byte) 0xD7, (byte) 0x8D
    };

    byte[] passwordArray = new byte[]{
            (byte) 0xE2, 0x79, 0x1F, (byte) 0x94, (byte) 0xDD, 0x3C, (byte) 0xD0, (byte) 0xF3, 0x3D, 0x78, (byte) 0xF3, 0x75, (byte) 0xC9, (byte) 0xA1, (byte) 0xBC, (byte) 0xA9
    };

    byte[] secureArray = new byte[]{
            0x77, (byte) 0xC1, 0x03, 0x00, (byte) 0xC0, 0x23, 0x23, (byte) 0xE8, 0x56, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    byte[] loginAndDate = new byte[]{
            (byte) 0xA8, (byte) 0xB6, 0x12, (byte) 0x85, (byte) 0x83, 0x10, 0x21, 0x27, 0x27, (byte) 0xC8, (byte) 0xD7, (byte) 0xDC, 0x52, (byte) 0xDD, 0x1E, 0x3F, (byte) 0xB8,
            (byte) 0x87, (byte) 0xD1, (byte) 0xAC, 0x44, (byte) 0xF5, 0x4E, (byte) 0xB9, (byte) 0xCF, (byte) 0xFC, 0x43, (byte) 0x84, 0x0C, (byte) 0xCE, 0x66, (byte) 0x89
    };

    byte[] idEcp = new byte[]{
            (byte) 0xC1, 0x03, 0x00, (byte) 0xC0
    };

    BscInformation bscInformation = null;
    AuthCard authCard;
    private static final String PIN = "8632401579";
    //</editor-fold>

    @Before
    public void setUp() throws Exception {
        bscInformation = mock(BscInformation.class);
        authCard = spy(new AuthCard(roleArray, fioArray, ecpArray, passwordArray, secureArray, loginAndDate, null, new byte[]{(byte) 0xa6, 0x13, (byte) 0xbf, 0x73}));
        doReturn(new byte[8]).when(authCard).getCristallNumber();
        doReturn(new byte[8]).when(authCard).getOuterNumberBytes();
        doReturn(idEcp).when(authCard).getIdEcp();
    }

    @Test
    @Ignore
    public void testExecCheck_valid() throws Exception {
        EdsManager edsManager = mock(EdsManager.class);
        GetKeyInfoResult getKeyInfoResult = new GetKeyInfoResult();
        getKeyInfoResult.setSuccessful(true);
        getKeyInfoResult.setDeviceId(2890433604L);
        getKeyInfoResult.setEffectiveDate(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 60)); // - день от текущего дня
        getKeyInfoResult.setExpireDate(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 60)); // + день от текущего дня

        CheckSignResult checkSignResult = new CheckSignResult();
        checkSignResult.setState(CheckSignResultState.VALID);
        when(edsManager.verifySign(null, ecpArray, 123)).thenReturn(checkSignResult);

        LocalDaoSession localDaoSession = mock(LocalDaoSession.class);
        when(localDaoSession.getLocalDb()).thenReturn(null);
        NsiDaoSession nsiDaoSession = mock(NsiDaoSession.class);
        when(nsiDaoSession.getNsiDb()).thenReturn(null);
        SecurityDaoSession securityDaoSession = mock(SecurityDaoSession.class);
        when(securityDaoSession.getSecurityDb()).thenReturn(null);

        SecuritySettings.Builder securitySettingsBuilder = new SecuritySettings.Builder();
        securitySettingsBuilder.setTimeLockAccess(3);
        securitySettingsBuilder.setAccessCardLoginLimitationPeriod(24);
        securitySettingsBuilder.setLimitLoginAttempts(3);

        RoleDvc seniorCashierRole = new RoleDvc();

        when(Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getSettingDao().getSecuritySettings()).thenReturn(securitySettingsBuilder.create());
        UserDvc userDvc = new UserDvc();
        userDvc.setFirstName("Name");
        userDvc.setLastName("Lastname");
        userDvc.setLogin("login");
        userDvc.setMiddleName("middlename");
        userDvc.setValidFrom(new Date(0));
        userDvc.setValidTo(new Date(0));
        userDvc.setId(1);
        when(Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getUserDvcDao().getUserFromUserDvc("login")).thenReturn(userDvc);
        when(Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getRoleDvcDao().getUserRoleForProductionSection(1, 1)).thenReturn(seniorCashierRole);
        SecurityCard securityCard = new SecurityCard();
        securityCard.setSalt("d");
        securityCard.setPasswordHash("d");
        when(Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getSecurityCardDao().getSecurityCard("0008a613bf73", String.valueOf(userDvc.getId()))).thenReturn(securityCard);
        when(Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getRolePermissionDvcDao().isPermissionEnabled(seniorCashierRole, PermissionDvc.Auth)).thenReturn(true);

        when(Di.INSTANCE.getPrivateSettings().get().getProductionSectionId()).thenReturn(10);

        mockStatic(NsiDbOperations.class);
        when(Dagger.appComponent().productionSectionRepository().load(0L, Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId())).thenReturn(null);
        when(NsiDbOperations.getRoleToId(null, 1)).thenReturn(seniorCashierRole);

        mockStatic(SchedulersCPPK.class);
        when(SchedulersCPPK.background()).thenReturn(Schedulers.computation());

        ProductionSectionRepository productionSectionRepository = mock(ProductionSectionRepository.class);

        AuthCardChecker authCardChecker = new AuthCardChecker(PIN, authCard, edsManager, securityDaoSession,
                Di.INSTANCE.nsiVersionManager(), productionSectionRepository, new FioFormatter());

        final AuthCardChecker.CheckResult[] actual = new AuthCardChecker.CheckResult[1];
        authCardChecker.authCheckRx()
                .subscribeOn(Schedulers.immediate())
                .observeOn(Schedulers.immediate())
                .subscribe(
                        checkResult -> actual[0] = checkResult,
                        Throwable::printStackTrace);

        assertNotNull(actual[0]);
        assertEquals(AuthResult.SUCCESS, actual[0].result);
        assertEquals(seniorCashierRole, actual[0].roleDvc);
    }
}
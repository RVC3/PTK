package ru.ppr.cppk.utils;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.logic.FioFormatter;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.cppk.db.nsi.NsiDbOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.AuthCard;
import ru.ppr.cppk.entity.AuthResult;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.legacy.EcpUtils;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.ProductionSection;
import ru.ppr.nsi.repository.ProductionSectionRepository;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;
import ru.ppr.security.entity.SecurityCard;
import ru.ppr.security.entity.SecuritySettings;
import ru.ppr.security.entity.UserDvc;
import ru.ppr.utils.CommonUtils;
import ru.ppr.utils.MD5Utils;
import rx.Observable;

/**
 * Класс валидирует Авторизационные карты Данные для подписи собираются из:
 * <p>
 * cardUid byte[8] = Идентификатор кристалла [4 byte] + нули [4 byte]
 *
 * @author A.Ushakov
 */
public class AuthCardChecker {

    private static final String TAG = AuthCardChecker.class.getSimpleName();

    private static final int DATE_START_BYTE = 16;
    private static final int LOGIN_START_BYTE = 0;

    private static final int BYTE_IN_BLOCK = 16;
    private static final long DEVICE_ID_FOR_FRESH_AUTH_CARD = 4000000000L;
    // Время, с которого у авторизационных карт записанный девайс ид должен быть больше DEVICE_ID_FOR_FRESH_AUTH_CARD
    private static final long TIMESTAMP_FOR_FRESH_CARD = 1458777600000L; // 24.03.2016

    // Временная переменная содержащая время после которого карта должна
    // появится в БД. Задается в настройках ПТК

    private String pin;
    private AuthCard authCard;
    private final EdsManager edsManager;

    private RoleDvc roleDvc;
    private String login;
    private String fioString;
    private final SecurityDaoSession securityDaoSession;
    private final NsiVersionManager nsiVersionManager;
    private final ProductionSectionRepository productionSectionRepository;
    private final FioFormatter fioFormatter;

    public AuthCardChecker(String pin,
                           AuthCard authCard,
                           @NonNull final EdsManager edsManager,
                           SecurityDaoSession securityDaoSession,
                           NsiVersionManager nsiVersionManager,
                           ProductionSectionRepository productionSectionRepository,
                           FioFormatter fioFormatter) {
        this.pin = pin;
        this.authCard = authCard;
        this.edsManager = edsManager;
        this.securityDaoSession = securityDaoSession;
        this.nsiVersionManager = nsiVersionManager;
        this.productionSectionRepository = productionSectionRepository;
        this.fioFormatter = fioFormatter;
    }

    private static class AuthObject {
        byte[] loginAndTimeData;
        byte[] fioArray;
        byte[] password;
        byte[] role;
        byte[] data;
        byte[] sig;
        long keyNumber;

        @Override
        public String toString() {
            return "AuthObject{" +
                    "loginAndTimeData=" + CommonUtils.byteArrayToString(loginAndTimeData) +
                    ", fioArray=" + CommonUtils.byteArrayToString(fioArray) +
                    ", password=" + CommonUtils.byteArrayToString(password) +
                    ", role=" + CommonUtils.byteArrayToString(role) +
                    ", data=" + CommonUtils.byteArrayToString(data) +
                    ", sig=" + CommonUtils.byteArrayToString(sig) +
                    ", keyNumber=" + keyNumber +
                    '}';
        }

        public void print() {
            Logger.info(TAG, "AuthObject print START -->");
            Logger.info(TAG, "AuthObject print loginAndTimeData: " + CommonUtils.byteArrayToString(loginAndTimeData));
            Logger.info(TAG, "AuthObject print fioArray: " + CommonUtils.byteArrayToString(fioArray));
            Logger.info(TAG, "AuthObject print fioString: " + new String(fioArray, Charset.forName("windows-1251")));

            Logger.info(TAG, "AuthObject print password: " + CommonUtils.byteArrayToString(password));
            Logger.info(TAG, "AuthObject print role: " + CommonUtils.byteArrayToString(role));
            Logger.info(TAG, "AuthObject print data: " + CommonUtils.byteArrayToString(data));
            Logger.info(TAG, "AuthObject print sig: " + CommonUtils.byteArrayToString(sig));
            Logger.info(TAG, "AuthObject print keyNumber: " + keyNumber);
            Logger.info(TAG, "<-- AuthObject print FINISH!");
        }
    }

    public static class CheckResult {
        public final AuthResult result;
        public final RoleDvc roleDvc;
        public final String login;
        public final String fio;

        public CheckResult(AuthResult result, RoleDvc roleDvc, String login, String fio) {
            this.result = result;
            this.roleDvc = roleDvc;
            this.login = login;
            this.fio = fio;
        }
    }

    public Observable<CheckResult> authCheckRx() {
        return Observable
                .fromCallable(this::createAuthObject)
                .flatMap(authObject -> {
                    final EdsType edsType = edsManager.getCurrentEdsType();
                    Logger.info(TAG, "Текущий EdsType - " + edsType);
                    //Если текущий тип сфт - STUB, то считаем что эцп валидна
//                    if (EdsType.STUB == edsType) {
                        return Observable.just(new Pair<>(Boolean.TRUE, authObject));
//                    } else {
//                        return checkSignRx(authObject);
//                    }
                })
                .observeOn(SchedulersCPPK.background())
                .flatMap(booleanAuthObjectPair -> performCheck(booleanAuthObjectPair.first, booleanAuthObjectPair.second))
                .subscribeOn(SchedulersCPPK.background());
    }

    @NonNull
    private AuthObject createAuthObject() {
        Logger.info(TAG, "Аутентификация пользователя");

        // make data for ecp check
        byte[] dataForCheck = new byte[320];

        byte[] uidData = authCard.getCristallNumber();

        System.arraycopy(uidData, 0, dataForCheck, 0, uidData.length);

        Logger.info(TAG, "uidData: " + CommonUtils.byteArrayToString(uidData));

        byte[] extNum = authCard.getOuterNumberBytes();
        System.arraycopy(extNum, 0, dataForCheck, 8, extNum.length);
        Logger.info(TAG, "extNum: " + MD5Utils.convertHashToString(extNum));

        byte[] securityData = authCard.getSecurityData();
        Logger.info(TAG, "securityData: " + CommonUtils.byteArrayToString(securityData));
        // обнуляем данные, в которых содержится ид ключа эцп, т.к. он не нужен
        // для проверки
        securityData[1] = 0;
        securityData[2] = 0;
        securityData[3] = 0;
        securityData[4] = 0;
        System.arraycopy(securityData, 0, dataForCheck, 16, securityData.length);
        Logger.info(TAG, "обнуляем данные, в которых содержится ид ключа эцп, т.к. он не нужен для проверки\nsecurityData: " + CommonUtils.byteArrayToString(securityData));

        byte[] password = authCard.getPassword(pin);
        System.arraycopy(password, 0, dataForCheck, 32, password.length);
        Logger.info(TAG, "password: " + CommonUtils.byteArrayToString(password));

        byte[] fioArray = authCard.getBytesFio(pin);
        System.arraycopy(fioArray, 0, dataForCheck, 48, fioArray.length);
        Logger.info(TAG, "fio: " + CommonUtils.byteArrayToString(fioArray));

        byte[] loginAndTimeData = authCard.getLoginAndTime(pin);
        System.arraycopy(loginAndTimeData, 0, dataForCheck, 96, loginAndTimeData.length);
        Logger.info(TAG, "loginAndTimeData: " + CommonUtils.byteArrayToString(loginAndTimeData));

        byte[] role = authCard.getRole(pin);
        System.arraycopy(role, 0, dataForCheck, 128, role.length);
        Logger.info(TAG, "role: " + CommonUtils.byteArrayToString(role));

        byte[] idEcpData = authCard.getIdEcp();

        long idEcp = EcpUtils.getEcpKeyNumberFromHex(idEcpData);
        Logger.info(TAG, "idEcpData: " + CommonUtils.byteArrayToString(idEcpData));

        /*
         * uidBytes, serialBytes, data.ServiceData, data.Password, data.Fio,
         * data.ProfileData, data.Roles);
         */

        AuthObject ao = new AuthObject();
        ao.fioArray = fioArray;
        ao.loginAndTimeData = loginAndTimeData;
        ao.role = role;
        ao.data = dataForCheck;
        ao.sig = authCard.getEcp(pin);
        ao.keyNumber = idEcp;
        ao.password = password;

        ao.print();
        return ao;
    }

    /**
     * Вычисляет хэшфункцию от пароля+соль
     *
     * @param password байты пароля
     * @param salt     соль
     * @return
     */
    private byte[] computeHash(byte[] password, byte[] salt) {

        byte[] data = new byte[password.length + salt.length];
        System.arraycopy(password, 0, data, 0, password.length);
        System.arraycopy(salt, 0, data, password.length, salt.length);
        byte[] hash = null;
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            hash = sha1.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    /*
     * Возвращает роль, под которой авторизовали пользователя
     */
    @NonNull
    public RoleDvc getRoleDvc() {
        return roleDvc;
    }

    /*
     * Возвращает ФИО авторизованного пользователя
     */
    public String getFio() {
        return fioString;
    }

    public String getLogin() {
        return login;
    }

    private Observable<Pair<Boolean, AuthObject>> checkSignRx(AuthObject ao) {
        return Observable
                .fromCallable(() -> edsManager.verifySign(ao.data, ao.sig, ao.keyNumber))
                .map(checkSignResult -> {
                    boolean result = checkSignResult.getState() != CheckSignResultState.INVALID;

                    if (result) {

                        final Date writeTime = new Date(authCard.getWriteTime());
                        final Date time = new Date(TIMESTAMP_FOR_FRESH_CARD);

                        if ((writeTime.after(time) && checkSignResult.getDeviceId() < DEVICE_ID_FOR_FRESH_AUTH_CARD)
                                || (checkSignResult.getState() == CheckSignResultState.KEY_REVOKED)) {
                            // если карта записана после 24.03.2016 и девайс ид меньше 4млрд, то эцп невалидна
                            // или эцп валидна но отозвана
                            result = false;
                        }
                    }

                    return new Pair<>(result, ao);
                })
                .subscribeOn(SchedulersCPPK.eds());
    }

    private Observable<CheckResult> performCheck(boolean isSignOk, AuthObject ao) {

        return Observable
                .fromCallable(() -> {
                            AuthResult authResult;
                            if (!isSignOk) {
                                Logger.info(TAG, "Некорректная ЭЦП");
                                authResult = AuthResult.ECP_ERROR;
                            } else {
                                Logger.info(TAG, "Проверка ЭЦП пройдена успешно!");

                                byte[] loginData = new byte[BYTE_IN_BLOCK];
                                byte[] dateArray = new byte[BYTE_IN_BLOCK];
                                System.arraycopy(ao.loginAndTimeData, LOGIN_START_BYTE, loginData, 0, BYTE_IN_BLOCK);
                                System.arraycopy(ao.loginAndTimeData, DATE_START_BYTE, dateArray, 0, BYTE_IN_BLOCK);
                                Logger.info(TAG, "loginData: " + MD5Utils.convertHashToString(loginData));
                                Logger.info(TAG, "dateArray: " + MD5Utils.convertHashToString(dateArray));

                                long currentTime = System.currentTimeMillis() / 1000;
                                long cardWriteTime = authCard.getWriteTime();
                                long delta = currentTime - cardWriteTime;
                                String loginFromCard = CommonUtils.makeCorrectString(new String(loginData));

                                Logger.info(TAG, "currentTime: " + currentTime);
                                Logger.info(TAG, "cardWriteTime: " + cardWriteTime);
                                Logger.info(TAG, "delta=(currentTime - cardWriteTime): " + delta);
                                Logger.info(TAG, "login: " + new String(loginData));

                                SecuritySettings securitySettings = Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getSettingDao().getSecuritySettings();
                                // период появления карты с БД переводим в секунды
                                if (delta > 0 && delta < securitySettings.getAccessCardLoginLimitationPeriod() * 60 * 60) {

                                    //пробуем получить роль из базы, если успешно получили из бд, то используем ее
                                    //иначе пытаемся получить роль из данных, записанных на карте
                                    UserDvc userDvc = Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getUserDvcDao().getUserFromUserDvc(loginFromCard);
                                    if (userDvc != null) {
                                        int cProductionSectionCode = Di.INSTANCE.getPrivateSettings().get().getProductionSectionId();
                                        RoleDvc roleFromDb = Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getRoleDvcDao().getUserRoleForProductionSection(cProductionSectionCode, userDvc.getId());
                                        if (roleFromDb != null) {
                                            roleDvc = roleFromDb;
                                            fioString = fioFormatter.getFullNameAsSurnameWithInitials(userDvc.getLastName(),
                                                    userDvc.getFirstName(), userDvc.getMiddleName());
                                            this.login = userDvc.getLogin();
                                            authResult = AuthResult.SUCCESS;
                                        } else {
                                            Logger.info(TAG, "INVALID_ROLE_1");
                                            authResult = AuthResult.INVALID_ROLE;
                                        }
                                    } else {
                                        RoleChecker roleChecker = new RoleChecker();
                                        int currentRegionNumber = Di.INSTANCE.getPrivateSettings().get().getProductionSectionId();
                                        Integer idRole = roleChecker.getIdRole(ao.role, currentRegionNumber);
                                        if (idRole == null) {
                                            authResult = AuthResult.INVALID_ROLE_NEW_CARD;
                                        } else {
                                            /*
                                             * Забираем ID роли и устанавливаем флаг успешной
                                             * авторизации
                                             */
                                            roleDvc = NsiDbOperations.getRoleToId(securityDaoSession.getSecurityDb(), idRole);
                                            fioString = new String(ao.fioArray, Charset.forName("windows-1251")).trim();
                                            this.login = loginFromCard;
                                            Logger.info(TAG, "roleDvc: " + roleDvc);
                                            Logger.info(TAG, "fioString: " + fioString);
                                            authResult = AuthResult.SUCCESS;
                                        }
                                    }

                                } else if (delta < 0) {
                                    authResult = AuthResult.INVALID_TIME;
                                } else {
                                    /*
                                     * по полученному имени учетной записи пытаемся получить
                                     * информацию о пользователе
                                     */
                                    UserDvc userDvc = Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getUserDvcDao().getUserFromUserDvc(loginFromCard);
                                    /*
                                     * Если вернулся null - значит в бд нет информации по такому
                                     * имени учетной записи.
                                     */
                                    if (userDvc == null) {
                                        authResult = AuthResult.USER_NOT_FOUND;
                                    } else {
                                        login = userDvc.getLogin();
                                        /*
                                         * Пытаемся получить информацию о карте по UID карты. Если
                                         * вернулся null - значит в бд нет информации о данной карте
                                         */
                                        String uidCard = CppkUtils.convertCardUIDToStopListNumber(authCard.getCardUID()).trim();
                                        Logger.info(TAG, "uidCard: " + uidCard);
                                        SecurityCard securityCard = Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getSecurityCardDao().getSecurityCard(uidCard.trim(), String.valueOf(userDvc.getId()).trim());
                                        if (securityCard == null) {
                                            authResult = AuthResult.CARD_NOT_FOUND;
                                        } else if (!securityCard.isActive()) {
                                            authResult = AuthResult.CARD_NOT_ACTIVE;
                                        } else {
                                            /*
                                             * Вычисляем хеш пароля и сравниваем с эталонным
                                             * значением, которое берем из таблицы SecurityCard Если
                                             * они не совпадают - ошибка авторизации
                                             */
                                            byte[] passwordHash = computeHash(ao.password, securityCard.getSalt().getBytes());
                                            byte[] passwordHashFromDatabase = CommonUtils.hexStringToByteArray(securityCard.getPasswordHash());

                                            if (passwordHash == null || passwordHashFromDatabase == null || !Arrays.equals(passwordHash, passwordHashFromDatabase)) {
                                                authResult = AuthResult.INVALID_PASSWORD;
                                            } else {
                                                //берем из БД первую доступную роль для текущего участка
                                                int cProductionSectionCode = Di.INSTANCE.getPrivateSettings().get().getProductionSectionId();

                                                // временная заглушка для авторизации по карте. Потом нужно убрать !!!
                                                if (cProductionSectionCode == PrivateSettings.Default.PRODUCTION_SECTION_CODE) cProductionSectionCode = 35;
                                                // временная заглушка для авторизации по карте. Потом нужно убрать !!!

                                                RoleDvc roleFromDb = Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getRoleDvcDao().getUserRoleForProductionSection(cProductionSectionCode, userDvc.getId());
                                                if (roleFromDb != null) {
                                                    roleDvc = roleFromDb;
                                                    fioString = Dagger.appComponent().fioFormatter().getFullNameAsSurnameWithInitials(userDvc.getLastName(), userDvc.getFirstName(), userDvc.getMiddleName());
                                                    authResult = AuthResult.SUCCESS;
                                                } else {
                                                    Logger.info(TAG, "INVALID_ROLE_2");
                                                    authResult = AuthResult.INVALID_ROLE;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (authResult == AuthResult.SUCCESS && getRoleDvc() == null) {
                                Logger.info(TAG, "INVALID_ROLE_3");
                                authResult = AuthResult.INVALID_ROLE;
                            }

                            int productionSectionCode = Di.INSTANCE.getPrivateSettings().get().getProductionSectionId();
                            ProductionSection productionSection = productionSectionRepository.load((long) productionSectionCode, nsiVersionManager.getCurrentNsiVersionId());
                            String productionSectionName = (productionSection == null) ? "null" : productionSection.getName();
                            Logger.info(TAG, "Текущий участок работы ПТК: " + productionSectionName + "(" + productionSectionCode + ")");

                            if (AuthResult.SUCCESS.equals(authResult)) {
                                if (!Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getRolePermissionDvcDao().isPermissionEnabled(getRoleDvc(), PermissionDvc.Auth)) {
                                    authResult = AuthResult.AuthDasabledForThisRole;
                                }
                            }

                            return new CheckResult(authResult, getRoleDvc(), getLogin(), getFio());
                        }

                );

    }
}

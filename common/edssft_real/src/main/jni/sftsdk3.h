#ifndef  __sftsdk_40__F3C6831C_4980_4559_805A_B8210908A330__
#define  __sftsdk_40__F3C6831C_4980_4559_805A_B8210908A330__


#if defined ( _WIN32 ) || defined( _WINDOWS ) || defined( _WIN64 )
#define SFTSDK3_C_CALL __cdecl
#if defined( BUILD_SFTSDK4 )
#define SFTSDK3_C_DECL __declspec( dllexport )
#else
#define SFTSDK3_C_DECL __declspec( dllimport )
#endif

#elif defined ( __unix__ ) || defined( __linux__ )
#define SFTSDK3_C_CALL
#define SFTSDK3_C_DECL
#endif

//  ����������� ��� �������� ��� ������� Sft Sdk 3.0
#define SFTSDK3_API extern "C" SFTSDK3_C_DECL long SFTSDK3_C_CALL


#include <stdint.h>


/// @brief ��������������� ������ ��� ��������� ���� ������.
#define MAKE_SFTSDK_ERROR(err)     ( 0x80de0000 + err )

/// @brief ��������� � ������ ������.
enum ErrorCodes {
    // ���� ��� ����� ���������� ������� ����� ������������ ��������.
            InvalidArgument = MAKE_SFTSDK_ERROR(1)
    // ������������� ����������� �� ��� ��������� (��� ����������� ������ �������).
    , AlreadyInitialized = MAKE_SFTSDK_ERROR(2)
    // ���������� �� �� ���������������.
    , NotInitialized = MAKE_SFTSDK_ERROR(3)
    // �� ������� ������� ���� � ���������� ������.
    , KeyStorageFile_CannotOpen = MAKE_SFTSDK_ERROR(4)
    // ���� � ���������� ������ ����� ������������ ���������.
    , KeyStorageFile_Corrupt = MAKE_SFTSDK_ERROR(5)
    // �� ������������ �������� �� ������������� ����������� ��.
    , LicenseNotActivated = MAKE_SFTSDK_ERROR(6)
    // �� ������ ���� ��� �������� ����������� ��.
    , KeyNotFound = MAKE_SFTSDK_ERROR(7)
    // �������� ������������ ������ (���� �� ����� �� �� ������������� ���������� �������).
    , InvalidBarCodeData = MAKE_SFTSDK_ERROR(8)
    // � �� ��������� ������ ������������ ����.
    , UnknownBarCodeData = MAKE_SFTSDK_ERROR(9)
    // �� �� �������� ���������.
    , BarCodeVerifyFail = MAKE_SFTSDK_ERROR(10)
    // ������ ��� �������� ����������� ��.
    , BarCodeVerifyError = MAKE_SFTSDK_ERROR(11)
    // �� ������� ��������� ���������� �����.
    , UpdateKeyProcessFail = MAKE_SFTSDK_ERROR(12)
    // ������ ��� ������� ������ ��.
    , BarCodeSignError = MAKE_SFTSDK_ERROR(13)
    // �������� ��� ����������� � ������ ������.
    , AlreadyRunning = MAKE_SFTSDK_ERROR(14)
    // ������ ���������������� �� ��������������.
    , NotSupportedFeature = MAKE_SFTSDK_ERROR(16)
    // �� ��������� ������������ ��� ���������� �������� ����������.
    , NoConfig = MAKE_SFTSDK_ERROR(23)
    // ������������ ��� ���������� �������� ���������� ��������.
    , ConfigObsolete = MAKE_SFTSDK_ERROR(18)
    // ���� �������� �������.
    , KeyObsolete = MAKE_SFTSDK_ERROR(19)
    // ���� �� ������.
    , FileNotFound = MAKE_SFTSDK_ERROR(21)
    // �������������� ���������� � ����� �� ���������.
    , AdditionalKeyInfoNotLoaded = MAKE_SFTSDK_ERROR(22)
};



/// @brief ��������� ��������� SDK.

///< ���� ��������, ����������� ��������� ������ ����������� �������
#define SDK_STATE_SUBURBAN_TICKET_IS_ALLOWED          0x02
///< ���� ��������, ����������� ��������� ������ ����������� �������
#define SDK_STATE_SUBURBAN_TICKET_SELLING_IS_ALLOWED  0x04




/// @brief ��������� ��������, ����������� ��� ������ ������������� ����������� ��.
/// @details � ���������, ������� ���������
/// - �������� ���������� � ��������� �� ������������� ����������� ��,
/// - �������� �������� ���������� (��).
/// ��� ���������� ������������� ����������� �� ���������� ������� ������� MTCS_SDK_CloseBarCodeProcessor.
///
/// @param[in] workingFolder ��������� ������ ������� �������, ���������� ���� � ��������, � ������� �����������
/// ��������� ����� SDK; � ���������, � ���� �������� ������������� ����� � ����������.
/// @param[in] workingFolderSize ������ ������� workingFolder;
/// @param[in] transportFolder ��������� ������ ������� �������, ���������� ���� � ��������, � ����������� In ��������
/// ������������� ����� � �����, ����������� ����� ��, � ����������� Out - ����� � ��������� ��, ������� ���������
/// ������ �������.
/// @param[in] transportFolderSize ������ ������� transportFolder;
///
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
/// NoError � ������ ������;
SFTSDK3_API SFT_SDK3_OpenBarCodeProcessor(const char *workingFolder, unsigned int workingFolderSize,
                                          const char *transportFolder,
                                          unsigned int transportFolderSize);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_OpenBarCodeProcessorProc)(const char *workingFolder,
                                                                 unsigned int workingFolderSize,
                                                                 const char *transportFolder,
                                                                 unsigned int transportFolderSize);


/// @brief ��������� ������������� ����������� ��.
/// @details ����� ���� ������� ��� ������������� ������ MTCS_SDK_OpenBarCodeProcessor �� ����� �������� �������.
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
SFTSDK3_API SFT_SDK3_CloseBarCodeProcessor();

typedef long (SFTSDK3_C_CALL *SFT_SDK3_CloseBarCodeProcessorProc)();



/// @brief ������� ������������ 2D �����-����.
///
/// @param[in] barcodeInfo ���������� � ������ � ���� ������������������ XML ������
/// @param[in] barcodeInfoSizeInByte ������ ������������������ XML ������ � ������.
/// @param[out] barcode �������� ������, ���� ����� ������� �������������� 2D �����-���.
/// @param[out] barcodeSizeInByte ������ ������� ��� 2D �����-���.
/// @param[out] usedBarcodeSizeInByte ���������� ������������/����������� ������ ������� ��� ��������������� 2D �����-����.
///
/// @return � ������ ��������� ������������ 2D �����-���� ������������ 0.
/// @throw ���������� �� ������������.
SFTSDK3_API SFT_SDK3_Make2DBarcode(const char *barcodeInfo, unsigned int barcodeInfoSizeInByte,
                                   unsigned char *barcode, unsigned int barcodeSizeInByte,
                                   unsigned int *usedBarcodeSizeInByte);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_Make2DBarcodeProc)(const char *barcodeInfo,
                                                          unsigned int barcodeInfoSizeInByte,
                                                          unsigned char *barcode,
                                                          unsigned int barcodeSizeInByte,
                                                          unsigned int *usedBarcodeSizeInByte);



/// @brief ������� �������� � ������� 2D �����-����.
///
/// @param[in] barcode �������� ������, ���������� 2D �����-���.
/// @param[in] barcodeSizeInByte ������ �������� 2D �����-����.
/// @param[out] barcodeInfo ������, ���� ����� �������� ����������, ���������� �� ������� 2D �����-���.
/// @param[out] barcodeInfoSizeInByte ������ ������, ��� ���������� �� 2D �����-����.
/// @param[out] usedBarcodeInfoSizeInByte ���������� ������������/����������� ������ ������ ��� ���������� �� 2D �����-����.
///
/// @return � ������ ��������� �������� � ������� 2D �����-���� ������������ 0.
/// @throw ���������� �� ������������.
SFTSDK3_API SFT_SDK3_Process2DBarcode(const unsigned char *barcode, unsigned int barcodeSizeInByte,
                                      char *barcodeInfo, unsigned int barcodeInfoSizeInByte,
                                      unsigned int *usedBarcodeInfoSizeInByte);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_Process2DBarcodeProc)(const unsigned char *barcode,
                                                             unsigned int barcodeSizeInByte,
                                                             char *barcodeInfo,
                                                             unsigned int barcodeInfoSizeInByte,
                                                             unsigned int *usedBarcodeInfoSizeInByte);


//
// ��������� ���� ������� ��������, ������� � ������ SDK 3.8.x
// � ���������������� ������ ��� ������ ������
//


/// @brief ����������� �������� ������
///
/// @param[out] errInfo � ��� ���������� ����� ������� ��������� �� ������, ���������� �������� ������.
///     ������ ����� ������������ �� ���������� ������ �������  SFT_SDK3_*.
///     �.�. ����� ������ (*errInfo ) � ��������� �� ������ ������ �������� ������.
///     (*errInfo )== 0 ���� ������ � ���������� ������ SFT_SDK3_* �� ����.
///     ��������� ������������ ������� � UTF-8 (��� ���������� � ����������� ��� ���������� ��������� ��������� ANSI),
///     ��������� ���������.
///     0 ���� ������ � ���������� ������ SFT_SDK3_* �� ����.
/// @param[out] errInfoSize �� ����� ��������� ����� ������� ������ �������� ������ � ������.
///     0 ���� ������ � ���������� ������ SFT_SDK3_* �� ����.
/// @return ������������ ��������:
///     0 � ������� ��������� �������
///     1 � 1-�� �������� ����� 0
///     2 � 2-�� �������� ����� 0
///     3 � ���������� ������
/// @throw ���������� �� ������������.
SFTSDK3_API SFT_SDK3_GetLastErrorDescription(const char **errInfo, unsigned int *errInfoSize);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_GetLastErrorDescriptionProc)(const char **errInfo,
                                                                    unsigned int *errInfoSize);


/// @brief ��������� ����������������� �������������� ����������
/// @detail ������� ������ ���������������� ������������� ����������, ������� ����� ������� � ������
///       ������� �� ����������� ��������� �����. � ���������� ������ ���� ������������� �����
///       ���������� � ������� SFT_SDK3_GetKeyInfo.
///
///  ������� ����� ���������� �� ������� ������������� SDK 1.0 / 2.0.
///  ����� ������ ������� ����� ����� ������ ��� ����������� ������� �������.
///  ���� ���������� ������������ ������ ��� �������� �������, �� ������� ����� �� ��������.
///
/// @param[in] userDeviceId ��������� �� ������� ������, ��� �������� ���������������� ������������� ����������
/// @param[in] userDeviceIdSize ������ ����������������� �������������� ���������� � ������.
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
/// @throw ���������� �� ������������.
SFTSDK3_API SFT_SDK3_SetUserId(const unsigned char *userDeviceId, unsigned int userDeviceIdSize);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_SetUserIdProc)(const unsigned char *userDeviceId,
                                                      unsigned int userDeviceIdSize);


/// @brief ������� ������
/// @detail ������� ��������� ������� ������, ���� ���������� �� ������ ������� ����: ������� ���� ������
///       ������� � �������� �������� �����. ������������ ������ ����� �������������� ������������ ������
///       �� ���������� ������ SFT_SDK3_*.
/// @param[in] inputData ��������� �� ������� ������, ��� �������� ������, ������� ������� ���������.
/// @param[in] inputDataSize ������ ������ ��� ������� � ������.
/// @param[in] time posix ����� � ��������.
/// @param[out] signBuffer �� ����� ��������� ����� ������� ��������� �� �����, ��� ����� �������.
/// @param[out] signSize �� ����� ��������� ����� �������� ����� ������� � ������.
/// @param[out] keyNumber �� ����� ��������� ����� ������� ������������� �����.
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
/// @throw ���������� �� ������������.
SFTSDK3_API SFT_SDK3_SignData(const unsigned char *inputData, unsigned int inputDataSize,
                              uint64_t time, const unsigned char **signBuffer,
                              unsigned int *signSize, uint32_t *keyNumber);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_SignDataProc)(const unsigned char *inputData,
                                                     unsigned int inputDataSize,
                                                     uint64_t time,
                                                     const unsigned char **signBuffer,
                                                     unsigned int *signSize, uint32_t *keyNumber);


/// @brief �������� �������
/// @detail ������� ��������� ������������ ������� �� ������ �����. ������������ �������� �������� ������� �����.
///       ������������ ������ ����� �������������� ������������ ������ �� ���������� ������ SFT_SDK3_*.
/// @param[in] inputData ��������� �� ������� ������, ��� �������� ������, ������� ������� ������� ���������.
/// @param[in] inputDataSize ������ ������ ��� �������� ������� � ������.
/// @param[in] sign ��������� �� ������� ������, ��� �������� �������.
/// @param[in] signSize ������ ������� � ������.
/// @param[in] keyNumber ����� �����, �� ������� ������������ �������.
///       ������������� ��������� pKeyNumber  � ������� SFT_SDK3_SignData.
/// @param[out] isSignValid  - ���������, �� �������� ����� ������� ��������� ��������. 0 � ������� �� �����, �� 0 � ������� �����
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
/// @throw ���������� �� ������������.
SFTSDK3_API SFT_SDK3_VerifySign(const unsigned char *inputData, unsigned int inputDataSize,
                                const unsigned char *sign, unsigned int signSize,
                                uint32_t keyNumber, unsigned int *isSignValid);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_VerifySignProc)(const unsigned char *inputData,
                                                       unsigned int inputDataSize,
                                                       const unsigned char *sign,
                                                       unsigned int signSize, uint32_t keyNumber,
                                                       unsigned int *isSignValid);


/// @brief ��������� �������������� ���������� �� �����
/// @detail ������� ���������� �������������� ���������� � ����� �� ��� ������..
///       ������������ ������ ����� �������������� ������������ ������ �� ���������� ������ SFT_SDK3_*.
/// @param[in] keyNumber � ����� �����
/// @param[out] keyValidSince � ���������, �� �������� ����� �������� posix ����� ������ �������� �����.
/// @param[out] keyValidTill - ���������, �� �������� ����� �������� posix ����� ��������� �������� �����.
/// @param[out] keyWhenRevocated  - ���������, �� �������� ����� �������� posix ����� ������ �����. 0 ���� ���� �� ��� �������.
/// @param[out] userDeviceId - ���������, �� �������� ����� ������� ��������� �� ������ ������ ������������������ �������������� ����������.
/// @param[out] userDeviceIdSize - ���������, �� �������� ����� �������� ����� ����������������� �������������� ���������� � ������.
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
/// @throw ���������� �� ������������.
SFTSDK3_API SFT_SDK3_GetKeyInfo(uint32_t keyNumber, uint64_t *keyValidSince, uint64_t *keyValidTill,
                                uint64_t *keyWhenRevocated, const unsigned char **userDeviceId,
                                unsigned int *userDeviceIdSize);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_GetKeyInfoProc)(uint32_t keyNumber, uint64_t *keyValidSince,
                                                       uint64_t *keyValidTill,
                                                       uint64_t *keyWhenRevocated,
                                                       const unsigned char **userDeviceId,
                                                       unsigned int *userDeviceIdSize);


/// @brief ������� ��������� ������ ��� ��������� ���������� � ���������� ��� (��. ����)
/// @details ��������� �������� ��������� �������� ���, �� ������� ���� ������������������ �������� �����.
/// ������ ���������� 0, ���� ���������� ���������� ������������ ��������� � �������� �������� �� 0 � ��������� ������.
/// @param[in] arg ������, ������������ � ������� �����
/// @param[in] validSince ���� ������ �������� �����
/// @param[in] validTill ���� ��������� �������� �����
typedef int ( *SFT_SDK3_PrivateKeyRangeProc )(void *arg, long long validSince, long long validTill);


/// @brief ���������� ���������� � ���������� ���, �� ������� ���� ������������������ �������� �����.
/// @details ������� ����� ����� ������ ��� ����������� �������� �� ������� ����������� �������.
/// @param[in] callback ��������� �� ������� ��������� ������, ������� ��������� ���������� ����������.
/// @param[in] callbackArg ������ ������������ � ������� ��������� ������ callback.
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
SFTSDK3_API SFT_SDK3_GetPrivateKeyRanges(SFT_SDK3_PrivateKeyRangeProc callback, void *callbackArg);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_GetPrivateKeyRangesProc)(
        SFT_SDK3_PrivateKeyRangeProc callback, void *callbackArg);

/// @brief ���������� ���������� � ���� ������������ ����������� ������������ � ���� � ������� �� �������� ���
/// �������������.
/// @details �������� ������� ������������ ��. � �������� ������� ProcessInputAndMakeOutputFiles.
/// @param[out] whenCreated � ������ ��������� ���������� �� ����� ��������� ����� ����������� �������� posix-����
/// ������������ ������������, �.�. ���� ������� � �������� ������������ �������������.
/// @param[out] validTill � ������ ��������� ���������� �� ����� ��������� ����� ����������� �������� posix-���� ��
/// �������� ������������ �������������.
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
SFTSDK3_API SFT_SDK3_GetLastSuccessfulConfigInfo(long long *whenCreated, long long *validTill);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_GetLastSuccessfulConfigInfoProc)(long long *whenCreated,
                                                                        long long *validTill);



/// @brief ���������� ��������� SDK.
/// @param[out] state � ������ ��������� ���������� �� ����� ��������� ����� ����������� ������� ����� ������������
///       ����� �������� ( SDK_STATE_SUBURBAN_TICKET_IS_ALLOWED �/��� SDK_STATE_SUBURBAN_TICKET_SELLING_IS_ALLOWED )
///
/// @return 0 � ������ ��������� ����������, �� 0 � ��������� ������. ��������� �������� ������
///       ����� �������� ������� SFT_SDK3_GetLastErrorDescription().
SFTSDK3_API SFT_SDK3_GetState(unsigned int *state);

typedef long (SFTSDK3_C_CALL *SFT_SDK3_GetStateProc)(unsigned int *state);


#endif // __sftsdk_40__F3C6831C_4980_4559_805A_B8210908A330__

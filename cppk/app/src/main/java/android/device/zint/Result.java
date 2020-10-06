package android.device.zint;

public class Result {
 // Warning and error conditions
    public int ZINT_WARN_INVALID_OPTION  =  2;
    public int ZINT_WARN_USES_ECI             =   3;
    public int ZINT_ERROR_TOO_LONG     =  5;
    public int ZINT_ERROR_INVALID_DATA      =     6;
    public int ZINT_ERROR_INVALID_CHECK    =  7;;
    public int ZINT_ERROR_INVALID_OPTION  =   8;
    public int ZINT_ERROR_ENCODING_PROBLEM=   9;
    public int ZINT_ERROR_FILE_ACCESS         =   10;
    public int ZINT_ERROR_MEMORY      =  11;
    private int retCode = -1;
    private int rawBytesSize;
    private int[] rawPixels;
    private byte[] rawBytes;
    private int symbolWidth;
    private int symbolHeight;
    public int getRawBytesSize() {
        return rawBytesSize;
    }
    public void setRawBytesSize(int rawBytesSize) {
        this.rawBytesSize = rawBytesSize;
    }
    public byte[] getRawBytes() {
        return rawBytes;
    }
    public void setRawBytes(byte[] rawBytes) {
        this.rawBytes = rawBytes;
    }
    public int getSymbolWidth() {
        return symbolWidth;
    }
    public void setSymbolWidth(int symbolWidth) {
        this.symbolWidth = symbolWidth;
    }
    public int getSymbolHeight() {
        return symbolHeight;
    }
    public void setSymbolHeight(int symbolHeight) {
        this.symbolHeight = symbolHeight;
    }
    public int getRetCode() {
        return retCode;
    }
    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }
    public int[] getRawPixels() {
        return rawPixels;
    }
    public void setRawPixels(int[] rawPixels) {
        this.rawPixels = rawPixels;
    }
    
}

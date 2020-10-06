package android.device.zint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;

public class EncodeHander {
    public static final int CODE11 = 1;
    public static final int Interleaved2of5 = 3;
    public static final int Code39  = 8;
    public static final int ExtendedCode39 = 9;
    public static final int EAN = 13;
    public static final int EAN8 = 12;
    public static final int EANCheck = 14;
    public static final int Codabar = 18;
    public static final int Code128 = 20;
    public static final int Code93 = 25;
    public static final int MSIPlessey = 47;
    public static final int UPCA = 34;//H70
    public static final int UPCACheck = 35;
    public static final int UPCE = 37;//H70
    public static final int UPCECheck = 38;
    public static final int QRCode = 58;
    /*" 1: Code 11           50: Logmars                  90: KIX Code\n"
    " 2: Standard 2of5     51: Pharma One-Track         92: Aztec Code\n"
    " 3: Interleaved 2of5  52: PZN                      93: DAFT Code\n"
    " 4: IATA 2of5         53: Pharma Two-Track         97: Micro QR Code\n"
    " 6: Data Logic        55: PDF417                   98: HIBC Code 128\n"
    " 7: Industrial 2of5   56: PDF417 Trunc             99: HIBC Code 39\n"
    " 8: Code 39           57: Maxicode                102: HIBC Data Matrix\n"
    " 9: Extended Code 39  58: QR Code                 104: HIBC QR Code\n"
    "13: EAN               60: Code 128-B              106: HIBC PDF417\n"
    "14: EAN + Check       63: AP Standard Customer    108: HIBC MicroPDF417\n"
    "16: GS1-128           66: AP Reply Paid           110: HIBC Codablock-F\n"
    "18: Codabar           67: AP Routing              112: HIBC Aztec Code\n"
    "20: Code 128          68: AP Redirection          115: DotCode\n"
    "21: Leitcode          69: ISBN                    116: Han Xin Code\n"
    "22: Identcode         70: RM4SCC                  128: Aztec Runes\n"
    "23: Code 16k          71: Data Matrix             129: Code 32\n"
    "24: Code 49           72: EAN-14                  130: Comp EAN\n"
    "25: Code 93           74: Codablock-F             131: Comp GS1-128\n"
    "28: Flattermarken     75: NVE-18                  132: Comp DataBar Omni\n"
    "29: GS1 DataBar Omni  76: Japanese Post           133: Comp DataBar Ltd\n"
    "30: GS1 DataBar Ltd   77: Korea Post              134: Comp DataBar ExpOm\n"
    "31: GS1 DataBar ExpOm 79: GS1 DataBar Stack       135: Comp UPC-A\n"
    "32: Telepen Alpha     80: GS1 DataBar Stack Omni  136: Comp UPC-E\n"
    "34: UPC-A             81: GS1 DataBar ESO         137: Comp DataBar Stack\n"
    "35: UPC-A + Check     82: Planet                  138: Comp DataBar Stack Omni\n"
    "37: UPC-E             84: MicroPDF                139: Comp DataBar ESO\n"
    "38: UPC-E + Check     85: USPS OneCode            140: Channel Code\n"
    "40: Postnet           86: UK Plessey              141: Code One\n"
    "47: MSI Plessey       87: Telepen Numeric         142: Grid Matrix\n"
    "49: FIM               89: ITF-14\n"*/
    static {
            if (Build.VERSION_CODES.M >= Build.VERSION.SDK_INT) {
                System.loadLibrary("UzintEncode_5.1");
            } else {
                System.loadLibrary("UzintEncode");
            }
    }
    public static native Result nativeEncode(int symbol, int height, int borderWidth, int reverseColour, float symbolScale, int hidetext, float DotSize, int securityLevel, int rotate, int alignment, int returnType,
                                             byte[] inputData);
    public static native Bitmap nativeEncodeBitmap(int symbol, int height, int borderWidth, int reverseColour, float symbolScale, int hidetext, float DotSize, int securityLevel, int rotate,
            byte[] inputData);
    public static Bitmap mixtureBitmap(String contents, Bitmap originalBitmap, int scaleWidth, int scaleHeight, int marginW, int HRI_location) {
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.BLACK);
        // text size in pixels
        //paint.setTextSize((int) (16 * scale));
        paint.setTextSize(20);
        // text shadow
        //paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(contents, 0, contents.length(), bounds);
        int height = HRI_location == 3 ? scaleHeight + bounds.height() * 2 : scaleHeight + bounds.height();
        Bitmap whiteBgBitmap = Bitmap.createBitmap( scaleWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(whiteBgBitmap);
        canvas.drawColor(Color.WHITE);
        if(HRI_location == 3) {
            int x = (scaleWidth - bounds.width())/2 ;
            int y = /*marginW + originalBitmap.getHeight() + */bounds.height();// (scaleHeight - bounds.height())/2 + bounds.height();
            canvas.drawText(contents, x, y, paint);
            canvas.drawBitmap(originalBitmap, 0, marginW + bounds.height(), null);
            y = 2*marginW + originalBitmap.getHeight() + bounds.height() *2;
            canvas.drawText(contents, x, y, paint);
        } else if(HRI_location == 2) { // draw text to the up
            int x = (scaleWidth - bounds.width())/2 ;
            int y = /*marginW + originalBitmap.getHeight() + */bounds.height();// (scaleHeight - bounds.height())/2 + bounds.height();
            canvas.drawText(contents, x, y, paint);
            canvas.drawBitmap(originalBitmap, 0, marginW + bounds.height(), null);
        } else { // draw text to the bottom
            canvas.drawBitmap(originalBitmap, 0, 0, null);
            int x = (scaleWidth - bounds.width())/2 ;
            int y = marginW + originalBitmap.getHeight() + bounds.height();
            canvas.drawText(contents, x, y, paint);
        } 
        return whiteBgBitmap;
    }
}

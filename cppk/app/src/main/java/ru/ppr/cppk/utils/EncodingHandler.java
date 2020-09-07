
package ru.ppr.cppk.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Hashtable;
/*BarcodeFormat.CODE_128; // 表示高密度数据， 字符串可变长，符号内含校验码  
BarcodeFormat.CODE_39;  
BarcodeFormat.CODE_93;  
BarcodeFormat.CODABAR; // 可表示数字0 - 9，字符$、+、 -、还有只能用作起始/终止符的a,b,c d四个字符，可变长度，没有校验位  
BarcodeFormat.DATA_MATRIX;  
BarcodeFormat.EAN_8;  
BarcodeFormat.EAN_13;  
BarcodeFormat.ITF;  
BarcodeFormat.PDF417; // 二维码  
BarcodeFormat.QR_CODE; // 二维码  
BarcodeFormat.RSS_EXPANDED;  
BarcodeFormat.RSS14;  
BarcodeFormat.UPC_E; // 统一产品代码E:7位数字,最后一位为校验位  
BarcodeFormat.UPC_A; // 统一产品代码A:12位数字,最后一位为校验位  
BarcodeFormat.UPC_EAN_EXTENSION;  */
public class EncodingHandler {
	/**
	 * 生成二维码 要转换的地址或字符串,可以是中文
	 * 
	 * @param context
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap createQRImage(String context, final int width, final int height) {
		try {
			// 判断URL合法性
			if (context == null || "".equals(context) || context.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(context, BarcodeFormat.QR_CODE, width,
					height, hints);
			int[] pixels = new int[width * height];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					} else {
						pixels[y * width + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 生成条形码
	 * 
	 * @param context
	 * @param contents 需要生成的内容
	 * @param desiredWidth 生成条形码的宽带
	 * @param desiredHeight 生成条形码的高度
	 * @param displayCode 是否在条形码下方显示内容
	 * @return
	 */
	public static Bitmap creatBarcode(Context context, String contents, int desiredWidth,
			int desiredHeight, boolean displayCode, int marginW, BarcodeFormat barcodeFormat) {
		Bitmap ruseltBitmap = null;
		int size = contents.length();
		for (int i = 0; i < size; i++) {
			int c = contents.charAt(i);
			if ((19968 <= c && c < 40623)) {
				return ruseltBitmap;
			}
		}
		/**
		 * 图片两端所保留的空白的宽度
		 */
		 // int marginW = 20;
		/**
		 * 条形码的编码类型
		 */
		// BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

		if (displayCode) {
			Bitmap barcodeBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth,
					desiredHeight);
			/*Bitmap codeBitmap = creatCodeBitmap(contents, desiredWidth + 2 * marginW,
                    desiredHeight, context);*/
			ruseltBitmap = mixtureBitmap(contents, barcodeBitmap, desiredWidth, desiredHeight + 2 * marginW, marginW );
			if(barcodeBitmap != null) {
				barcodeBitmap.recycle();
				barcodeBitmap = null;
			}
			/*if(codeBitmap != null) {
                codeBitmap.recycle();
                codeBitmap = null;
            }*/
		} else {
			ruseltBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth, desiredHeight);
		}

		return ruseltBitmap;
	}
	static float scale = 1.5f;
	private static Bitmap mixtureBitmap(String contents, Bitmap originalBitmap, int scaleWidth, int scaleHeight, int marginW) {
		// new antialised Paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// text color - #3D3D3D
		paint.setColor(Color.BLACK);
		// text size in pixels
		//paint.setTextSize((int) (16 * scale));
		paint.setTextSize(18);
		// text shadow
		//paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
		// draw text to the Canvas center
		Rect bounds = new Rect();
		paint.getTextBounds(contents, 0, contents.length(), bounds);
		Bitmap whiteBgBitmap = Bitmap.createBitmap( scaleWidth, scaleHeight + bounds.height(), Config.ARGB_8888);
		Canvas canvas = new Canvas(whiteBgBitmap);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(originalBitmap, 0, 0, null);
		//canvas.drawBitmap(originalBitmap,  new Rect(0, 0, originalBitmap.getWidth(), originalBitmap.getHeight()),  new Rect(0, 0,scaleWidth, scaleHeight), null);

		// draw text to the bottom
		//int x = (scaleWidth - bounds.width())/2 ; //zhangwen
		int x =  0;
		int y = marginW + originalBitmap.getHeight() + bounds.height();// (scaleHeight - bounds.height())/2 + bounds.height();
		canvas.drawText(contents, x, y, paint);
		return whiteBgBitmap;
	}
	/**
	 * 生成条形码的Bitmap
	 * 
	 * @param contents 需要生成的内容
	 * @param format 编码格式
	 * @param desiredWidth
	 * @param desiredHeight
	 * @return
	 * @throws WriterException
	 */
	protected static Bitmap encodeAsBitmap(String contents,
			BarcodeFormat format, int desiredWidth, int desiredHeight) {
		final int WHITE = 0xFFFFFFFF;
		final int BLACK = 0xFF000000;

		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = null;
		try {
			result = writer.encode(contents, format, desiredWidth,
					desiredHeight, null);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 生成显示编码的Bitmap
	 * 
	 * @param contents
	 * @param width
	 * @param height
	 * @param context
	 * @return
	 */
	protected static Bitmap creatCodeBitmap(String contents, int width,
			int height, Context context) {
		TextView tv = new TextView(context);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(layoutParams);
		tv.setText(contents);
		tv.setHeight(height);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		tv.setWidth(width);
		tv.setDrawingCacheEnabled(true);
		tv.setTextColor(Color.BLACK);
		tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

		tv.buildDrawingCache();
		Bitmap bitmapCode = tv.getDrawingCache();
		return bitmapCode;
	}

	/**
	 * 将两个Bitmap合并成一个
	 * 
	 * @param first
	 * @param second
	 * @param fromPoint
	 *            第二个Bitmap开始绘制的起始位置（相对于第一个Bitmap）
	 * @return
	 */
	protected static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
			PointF fromPoint, int marginW) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		Bitmap newBitmap = Bitmap.createBitmap(
				first.getWidth() + second.getWidth() + marginW,
				first.getHeight() + second.getHeight(), Config.ARGB_4444);
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(first, marginW, 0, null);
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
		//cv.save(Canvas.ALL_SAVE_FLAG);
		cv.save();
		cv.restore();

		return newBitmap;
	}
	// 生成QR图
	public static Bitmap createQRCodeLogoImage(String text, int w, int h, Bitmap logo) {
		try {
			Bitmap scaleLogo = getScaleLogo(logo, w, h);
			int offsetX = (w - scaleLogo.getWidth()) / 2;
			int offsetY = (h - scaleLogo.getHeight()) / 2;
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, w, h,
					hints);
			int[] pixels = new int[w * h];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					if (x >= offsetX && x < offsetX + scaleLogo.getWidth() && y >= offsetY
							&& y < offsetY + scaleLogo.getHeight()) {
						int pixel = scaleLogo.getPixel(x - offsetX, y - offsetY);
						if (pixel == 0) {
							if (bitMatrix.get(x, y)) {
								pixel = 0xff000000;
							} else {
								pixel = 0xffffffff;
							}
						}
						pixels[y * w + x] = pixel;
					} else {
						if (bitMatrix.get(x, y)) {
							pixels[y * w + x] = 0xff000000;
						} else {
							pixels[y * w + x] = 0xffffffff;
						}
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
			if(scaleLogo != null) {
				scaleLogo.recycle();
				scaleLogo = null;
			}
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Bitmap getScaleLogo(Bitmap logo, int w, int h) {
		if (logo == null)
			return null;
		Matrix matrix = new Matrix();
		float scaleFactor = Math.min(w * 1.0f / 5 / logo.getWidth(),
				h * 1.0f / 5 / logo.getHeight());
		matrix.postScale(scaleFactor, scaleFactor);
		Bitmap result = Bitmap.createBitmap(logo, 0, 0, logo.getWidth(), logo.getHeight(), matrix,
				true);
		return result;
	}

}

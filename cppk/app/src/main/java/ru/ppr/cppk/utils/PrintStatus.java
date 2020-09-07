package ru.ppr.cppk.utils;

public class PrintStatus {
	public static final int ERROR_NONE = 0x00;
	public static final int ERROR_PAPERENDED = 0xF0;
	public static final int ERROR_HARDERR = 0xF2;
	public static final int ERROR_OVERHEAT = 0xF3;
	public static final int ERROR_BUFOVERFLOW = 0xF5;
	public static final int ERROR_LOWVOL = 0xE1;
	public static final int ERROR_PAPERENDING = 0xF4;
	public static final int ERROR_MOTORERR = 0xFB;
	public static final int ERROR_PENOFOUND = 0xFC;
	public static final int ERROR_PAPERJAM = 0xEE;
	public static final int ERROR_NOBM = 0xF6;
	public static final int ERROR_BUSY = 0xF7;
	public static final int ERROR_BMBLACK = 0xF8;
	public static final int ERROR_WORKON = 0xE6;
	public static final int ERROR_LIFTHEAD = 0xE0;
	public static final int ERROR_CUTPOSITIONERR = 0xE2;
	public static final int ERROR_LOWTEMP = 0xE3;
	/*打印机状态:
	 * <ul>
	 * <li>ERROR_NONE(0x00) - 状态正常</li>
	 * <li>ERROR_PAPERENDED(0xF0) - 缺纸,不能打印</li>
	 * <li>ERROR_HARDERR(0xF2) - 硬件错误</li>
	 * <li>ERROR_OVERHEAT(0xF3) - 打印头过热</li>
	 * <li>ERROR_BUFOVERFLOW(0xF5) - 缓冲模式下所操作的位置超出范围
     </li>
	 * <li>ERROR_LOWVOL(0xE1) - 低压保护 </li>
	 * <li>ERROR_PAPERENDING(0xF4) - 纸张将要用尽,还允许打印(单步进针打
	 * <li>ERROR_MOTORERR(0xFB) - 打印机芯故障(过快或者过慢)</li>
	 * <li>ERROR_PENOFOUND(0xFC) - 自动定位没有找到对齐位置,纸张回到原来
             位置
             </li>
	 * <li>ERROR_PAPERJAM(0xEE) - 卡纸</li>
	 * <li>ERROR_NOBM(0xF6) - 没有找到黑标</li>
	 * <li>ERROR_BUSY(0xF7) - 打印机处于忙状态</li>
	 * <li>ERROR_BMBLACK(0xF8) - 黑标探测器检测到黑色信号</li>
	 * <li>ERROR_WORKON(0xE6) - 打印机电源处于打开状态</li>
	 * <li>ERROR_LIFTHEAD(0xE0) - 打印头抬起(自助热敏打印机特有返回值)</li>
	 * <li>ERROR_CUTPOSITIONERR(0xE2) - 切纸刀不在原位(自助热敏打印机特
             有返回值)</li>
	 * <li>ERROR_LOWTEMP(0xE3) - 低温保护或AD出错(自助热敏打印机特有返
             回值)</li>*/
}

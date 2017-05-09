package com.cmccpoc.config;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.View;
import com.airtalkee.sdk.engine.AirEngine;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.cmccpoc.R;
import com.cmccpoc.Util.Util;

/**
 * poc配置
 * @author Yao
 */
public class Config
{
	public static final int TRACE_MODE_ON_SCREEN = 0;
	public static final int TRACE_MODE_ON_FILE = 1;
	public static final int TRACE_MODE_OFF = 2;
	// VERSION
	public static final String VERSION_PLATFORM = "ANDROID";
	public static final String VERSION_TYPE = "ANDROID STD 2.0~";
	public static String VERSION_CODE = "";
	
	public static int marketCode = 130;
	// =======================================================

	public static final int TRACE_MODE = TRACE_MODE_ON_SCREEN;

	// =======================================================
	// DEFINE
	// =======================================================
	public static final int ENGINE_MEDIA_HB_SIZE_LARGE = 50;
	public static final int ENGINE_MEDIA_HB_SIZE_NONE = 0;

	public static final int ENGINE_MEDIA_HB_SECOND_HIGH = 5;
	public static final int ENGINE_MEDIA_HB_SECOND_FAST = 10;
	public static final int ENGINE_MEDIA_HB_SECOND_MEDIUM = 15;
	public static final int ENGINE_MEDIA_HB_SECOND_SLOW = 20;

	// =======================================================
	// FUNCTIONS
	// =======================================================
	/** 手机型号 */
	public static String model = "";
	/**
	 * PTT按键 广播 的action 对应的pttKeycode 如果pttKeycode 等于 KeyEvent.KEYCODE_UNKNOWN
	 * action 等于 ""
	 */
	public static String pttButtonAction = "";
	/** 是否直接使用PTT按钮广播事件Action的Up和Down来控制话语权 **/
	public static String pttButtonActionUpDownCode = "";
	/** 要监控的PTT单独的广播事件 */
	public static String pttButtonActionUp = "";
	public static String pttButtonActionDown = "";
	public static String pttButtonActionUpExternal = "";
	public static String pttButtonActionDownExternal = "";
	/** 要监控的PTT按键 键值 */
	public static int pttButtonKeycode = KeyEvent.KEYCODE_UNKNOWN;
	/** 未登录状态，是否允许PTT物理按键唤醒应用 */
	public static boolean pttButtonPressWakeupWhenStandby = true;
	/** PTT按钮是否显示 如果pttVolumeEnable 是true Visibility=VISIBLE 否则 Visibility=GONE */
	public static int pttButtonVisibility = View.VISIBLE;
	/** 是否支持点击屏幕按键获得/释放话语权 */
	public static boolean pttClickSupport = false;
	/** 是否使用音量键控制话语权 */
	public static boolean pttVolumeKeySupport = false;
	/** 屏幕方向 */
	public static int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	/** 是否支持一直保持在对讲界面，即便按back/home （这个是针对 车载台） */
	public static boolean screenAlwaysPtt = false;
	/** 媒体服务设置：心跳时间间隔（秒） */
	public static int engineMediaSettingHbSeconds = ENGINE_MEDIA_HB_SECOND_SLOW;
	/** 媒体服务设置：心跳包大小 */
	public static int engineMediaSettingHbPackSize = ENGINE_MEDIA_HB_SIZE_NONE;
	/** 是否启用音频播放放大器 */
	public static boolean audioAmplifierEnabled = false;

	// =======================================================
	public static boolean funcFORCE = false;

	/** 呼叫中心选项是否显示在主菜单 */
	public static boolean funcCenterCallMenuShow = true;
	/** 是否支持呼叫中心 */
	public static int funcCenterCall = AirFunctionSetting.SETTING_ENABLE;
	/** 是否呼叫中心的电话号码 */
	public static String funcCenterCallNumber = "";
	/** 是否支持上报资料版本 */
	public static boolean funcCenterReport = true;
	/** 上报位置选项是否显示在主菜单 */
	public static boolean funcCenterLocationMenuShow = false;
	/** 是否支持上报位置版本 */
	public static int funcCenterLocation = AirFunctionSetting.SETTING_ENABLE;
	/** 是否有用户手册 */
	public static boolean funcManual = true;
	/** 是否有意见反馈 */
	public static boolean funcfeedback = true;
	/** 是否显示流量监控 */
	public static boolean funcStatisticNetwork = true;
	/** 是否允许查看全部用户 */
	public static boolean funcUserAll = true;
	/** 是否有广播 */
	public static boolean funcBroadcast = true;
	/** 是否有视频 */
	public static boolean funcVideo = true;
	/** 是佛支持视频摄像头 **/
	public static boolean funcVideoDevice = false;
	/** 短视频支持的最长时间 **/
	public static int funcVideoSectionTimeMax = 60*1000;
	public static int funcVideoSectionSizeMax = 100 * 1024 * 1024;
	/** 是否显示客户LOGO */
	public static boolean funcShowCustomLogo = false;
	public static int funcShowCustomLogoIconId = 0;
	public static int funcShowCustomLogoStringId1 = 0;
	public static int funcShowCustomLogoStringId2 = 0;
	public static boolean  funcServerIPSetting = false;

	public static boolean  funcPlayMediaTalkPrepare = false;
	public static boolean  funcPlayMediaTalkOff = true;
	/** 是否开机启动欢迎界面*/
	public static boolean funcBootLaunch= false;
	/** 按ptt按键启动对讲 */
	public static boolean funcPTTLaunch = true;
	/** 是否允许安装后首次自启动*/
	public static boolean funcAllowFirstLaunch = true;
	
	public static int defaultTheme = R.style.AppTheme_Dark;
	// ========================================================================

	/** MARKET 不同配置 **/
	public static String app_name = "";
	public static String serverAddress = "";
	public static String serverUrlInfosys = "";
	

	public static void marketConfig(Context context)
	{
		app_name = context.getString(R.string.app_name_poc);
		VERSION_CODE = Util.appVersion(context);
		serverAddress = "112.33.0.187";
		funcStatisticNetwork = true;
		defaultTheme = R.style.AppTheme_Dark;
		funcShowCustomLogo = true;
		funcShowCustomLogoIconId = R.drawable.logo_cmcc;
		funcShowCustomLogoStringId1 = R.string.copyright_cmcc1;
		funcShowCustomLogoStringId2 = R.string.copyright_cmcc2;
	}

}
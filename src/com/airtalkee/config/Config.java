package com.airtalkee.config;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.View;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.sdk.entity.AirFunctionSetting;

public class Config
{
	public static final int TRACE_MODE_ON_SCREEN = 0;
	public static final int TRACE_MODE_ON_FILE = 1;
	public static final int TRACE_MODE_OFF = 2;
	// =======================================================
	// ������������
	public final static int MARKET_WEPTT_TEST = 80;
	public final static int MARKET_WEPTT_V2 = 88;
	public final static int MARKET_WEPTT = 100; //@string/app_name
	public final static int MARKET_CMCC = 130;	//@string/app_name_cmcc
	public final static int MARKET_CMCC_TEST = 131;	//@string/app_name_cmcc
	public final static int MARKET_CMCC_DATANG = 132;	//@string/app_name_cmcc
	public final static int MARKET_CMCCLIMIT = 134;	//@string/app_name_cmcc
	public final static int MARKET_CMCC_PICC = 258;//������������
	public final static int MARKET_CMCC_CPIC = 262;//������������
	public final static int MARKET_CMCC_SUBPLATFORM_GX = 137;	// ������������������
	public final static int MARKET_CMCC_35 = 113; // 35������
	public final static int MARKET_NAVIDOG_TEST = 120;
	public final static int MARKET_BPER = 101; //@string/app_name_bper
	public final static int MARKET_BPER_SINGAPORE = 106; //@string/app_name_bper
	public final static int MARKET_BPER_GERMANY = 105; //@string/app_name_bper
	public final static int MARKET_BPER_RUSSIA_TAXI = 107; //@string/app_name_bper
	public final static int MARKET_BPER_MTT = 102; //@string/app_name_bper_mtt
	public final static int MARKET_CHINA_TELECOM = 110; // ������������
	public final static int MARKET_CHINA_35 = 111; // 35������
	public final static int MARKET_CHINA_35_P = 112; // 35������
	public final static int MARKET_UNICOM_GUIZHOU = 501; // ������������			//@string/app_name_unicom
	public final static int MARKET_UNICOM = 500; // ������������			//@string/app_name
	public final static int MARKET_JINYUANXINTONG = 502; // ������������
	public final static int MARKET_TAITONGYUAN = 504; // ���������
	public final static int MARKET_TRACKSYSTEM_GUANGZHOU = 505; // ������������������
	public final static int MARKET_TRACKSYSTEM_SHANGHAI = 506; // ������������������
	public final static int MARKET_JIZHEN_SHENYANG = 210; // ������������
	public final static int MARKET_JIZHEN_LIAONING = 211; // ������������
	public final static int MARKET_GONGAN_JINGWEIJU = 503; // ������������������
	public final static int MARKET_GONGAN_WEIFANG = 201; //���������������������
	public final static int MARKET_GONGAN_NANJING = 202; //���������������������
	public final static int MARKET_GONGAN_MTG = 204; // ���������������
	public final static int MARKET_WUJING_SHANDONG = 207; // ������������
	public final static int MARKET_GUGONG = 203; // ������
	public final static int MARKET_DATANG = 205; // ������������
	public final static int MARKET_DATANG_51POC = 206; // ������������
	public final static int MARKET_REALWAY_TAIYUAN = 250; //������������������
	public final static int MARKET_XUZHOU = 251;
	public final static int MARKET_FIRE_BJ = 601; // ���������������������
	public final static int MARKET_SANXIA = 252;
	public final static int MARKET_UNI_STRONG = 253;//������������
	public final static int MARKET_LI_SHI_TECHNOLOGY = 254;//������������
	public final static int MARKET_WO_XIN_DA = 255;//���������
	public final static int MARKET_HUBEI_MOBILE = 256;//������������
	public final static int MARKET_SONIM_TELECOM = 257;//sonim������������
	public final static int MARKET_GX_GUIGANG_HAISHI = 259;//���������������������
	public final static int MARKET_FENGHUO_WUHAN = 260;	//������������
	public final static int MARKET_FENGHUO_BEIJING = 261;//������������
	public final static int MARKET_ACEVISION =263;//������������  ACE
	public final static int MARKET_KCELL =265;//���������  kcell
	// =======================================================

	// =======================================================
	// VERSION
	public static final String VERSION_PLATFORM = "ANDROID";
	public static final String VERSION_TYPE = "ANDROID STD 2.0~";
	public static String VERSION_CODE = "";
	
	public static int marketCode = MARKET_CMCC;   
	// =======================================================

	public static final int TRACE_MODE = TRACE_MODE_OFF;

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
	/** ������������ */
	public static String model = "";
	/**
	 * PTT������ ������ ���action ���������pttKeycode ������pttKeycode ������ KeyEvent.KEYCODE_UNKNOWN
	 * action ������ ""
	 */
	public static String pttButtonAction = "";
	/** ������������������PTT������������������Action���Up���Down������������������ **/
	public static String pttButtonActionUpDownCode = "";
	/** ������������PTT��������������������� */
	public static String pttButtonActionUp = "";
	public static String pttButtonActionDown = "";
	/** ������������PTT������ ������ */
	public static int pttButtonKeycode = KeyEvent.KEYCODE_UNKNOWN;
	/** ������������������������������PTT������������������������ */
	public static boolean pttButtonPressWakeupWhenStandby = true;
	/** PTT������������������ ������pttVolumeEnable ���true Visibility=VISIBLE ������ Visibility=GONE */
	public static int pttButtonVisibility = View.VISIBLE;
	/** ������������������������������������/��������������� */
	public static boolean pttClickSupport = false;
	/** ������������������������������������ */
	public static boolean pttVolumeKeySupport = false;
	/** ������������ ������������UP������ */
	public static boolean pttEarphoneLongPress = false;
	/** ���������������KeyCode */
	public static int pttEarphoneKeycode = KeyEvent.KEYCODE_HEADSETHOOK;
	public static int pttBtKeycode = KeyEvent.KEYCODE_MEDIA_NEXT;
	/** ������������������������������ */
	public static boolean pttEarphonePlug = false;
	/** ������������ */
	public static int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	/** ���������������������������������������������������back/home ������������������ ������������ */
	public static boolean screenAlwaysPtt = false;
	/** ������������������������������������������������ */
	public static int engineMediaSettingHbSeconds = ENGINE_MEDIA_HB_SECOND_SLOW;
	/** ������������������������������������ */
	public static int engineMediaSettingHbPackSize = ENGINE_MEDIA_HB_SIZE_NONE;
	/** ��������������������������������� */
	public static boolean audioAmplifierEnabled = false;

	// =======================================================
	public static boolean funcFORCE = false;

	/** ������������������������������������������ */
	public static boolean funcCenterCallMenuShow = true;
	/** ������������������������ */
	public static int funcCenterCall = AirFunctionSetting.SETTING_ENABLE;
	/** ��������������������������������� */
	public static String funcCenterCallNumber = "";
	/** ������������������������������ */
	public static boolean funcCenterReport = true;
	/** ������������������������������������������ */
	public static boolean funcCenterLocationMenuShow = false;
	/** ������������������������������ */
	public static int funcCenterLocation = AirFunctionSetting.SETTING_ENABLE;
	/** ��������������������� */
	public static boolean funcManual = true;
	/** ��������������������� */
	public static boolean funcfeedback = true;
	/** ������������������������ */
	public static boolean funcStatisticNetwork = true;
	/** ������������UMENG������ */
	public static boolean funcStatisticUmeng = true;
	/** ������������������������������������ */
	public static boolean funcUserPhone = false;
	/** ������������������������ */
	public static boolean funcUserRegistration = false;
	/** ������������������������������ */
	public static boolean funcUserAll = true;
	/** ������������������������������������������ */
	public static boolean funcUserIdAndPwdIsNumber = true;
	/** ������������������������������ */
	public static boolean funcChannelManage = false;
	/** ��������������������������������������� */
	public static boolean funcChannelCallIn = false;
	/** ��������������� */
	public static boolean funcBroadcast = true;
	/** ��������� ������������*/
	public static boolean funcTaskDispatch = false;
	/** ��������� ������*/
	public static boolean funcAttendance = false;
	/** ��������������� */
	public static boolean funcVideo = false;
	/** ������������������������*/
	public static boolean funcThemeChange = false;
	/** ������������������LOGO */
	public static boolean funcShowCustomLogo = false;
	public static int funcShowCustomLogoIconId = 0;
	public static int funcShowCustomLogoStringId1 = 0;
	public static int funcShowCustomLogoStringId2 = 0;
	
	public static boolean  funcPlayMediaTalkPrepare = false;
	public static boolean  funcPlayMediaTalkOff = true;
	/** ������������������������*/
	public static boolean funcTTS = false;
	/** ������������������������������*/
	public static boolean funcBootLaunch= false;
	/** ���ptt������������������ */
	public static boolean funcPTTLaunch = true;
	/** ������������������������������������*/
	public static boolean funcAllowFirstLaunch = true;
	public static int defaultTheme = R.style.AppTheme_Dark;
	// ========================================================================
	
	
	// ========================================================================
	// ������������������
	// ========================================================================
	public static boolean SUB_PLATFORM_VALID = false;
	public static String SUB_PLATFORM_ADDRESS_DM = "";
	public static String SUB_PLATFORM_ADDRESS_WEB = "";
	public static String SUB_PLATFORM_ADDRESS_NOTICE = "";
	// ========================================================================
	
	

	/** MARKET ������������ **/
	public static String app_name = "";
	public static String serverAddress = "";
	public static String serverUrlInfosys = "";

	public static void marketConfig(Context context)
	{
		app_name = context.getString(R.string.app_name);
		VERSION_CODE = Util.appVersion(context);
		switch (marketCode)
		{
			case MARKET_WEPTT_TEST:
				serverAddress = "106.37.190.22";
				funcStatisticNetwork = true;
				//funcUserIdAndPwdIsNumber = false;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_WEPTT:
				serverAddress = "ptt.weptt.com";
				funcStatisticNetwork = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_WEPTT_V2:
				serverAddress = "sv2.weptt.com";
				funcStatisticNetwork = true;
				pttEarphonePlug = true;
				funcTaskDispatch = true;
				funcVideo = true;
				funcChannelCallIn = false;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_CMCC:
				serverAddress = "112.33.0.187";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_cmcc;
				funcShowCustomLogoStringId1 = R.string.copyright_cmcc1;
				funcShowCustomLogoStringId2 = R.string.copyright_cmcc2;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				app_name = context.getString(R.string.app_name_cmcc);
				break;
			case MARKET_CMCCLIMIT:
				serverAddress = "112.33.0.187";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_cmcc;
				funcShowCustomLogoStringId1 = R.string.copyright_cmcc1;
				funcShowCustomLogoStringId2 = R.string.copyright_cmcc2;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				app_name = context.getString(R.string.app_name_cmcc);
				break;
			case MARKET_CMCC_DATANG:
				serverAddress = "112.33.0.187";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_cmcc;
				funcShowCustomLogoStringId1 = R.string.copyright_cmcc1;
				funcShowCustomLogoStringId2 = R.string.copyright_cmcc2;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				app_name = context.getString(R.string.app_name_cmcc);
				funcPTTLaunch = false;
				break;
			case MARKET_CMCC_TEST:
				serverAddress = "112.33.0.89";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_cmcc;
				funcShowCustomLogoStringId1 = R.string.copyright_cmcc1;
				funcShowCustomLogoStringId2 = R.string.copyright_cmcc2;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				funcChannelCallIn = false;
				funcTaskDispatch = true;
				funcAttendance = true;
				app_name = context.getString(R.string.app_name_cmcc);
				break;
			case MARKET_GX_GUIGANG_HAISHI:
				serverAddress = "117.141.200.15";
				defaultTheme = R.style.AppTheme_Dark;
				funcStatisticNetwork = true;
				funcTaskDispatch = true;
				funcChannelCallIn = false;
				break;
			case MARKET_FENGHUO_WUHAN:
				serverAddress = "10.10.10.1";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				funcCenterCallMenuShow = false;
				funcCenterReport = false;
				funcChannelCallIn = false;
				funcManual = false;
				funcBroadcast = false;
				app_name = context.getString(R.string.app_name);
				break;
			case MARKET_FENGHUO_BEIJING:
				serverAddress = "192.168.0.203";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_fhkj;
				defaultTheme = R.style.AppTheme_Dark;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				funcCenterCallMenuShow = false;
				funcCenterReport = false;
				funcChannelCallIn = false;
				funcManual = false;
				funcBroadcast = false;
				app_name = context.getString(R.string.app_name);
				break;
			case MARKET_CMCC_PICC:
				serverAddress = "112.33.0.187";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Light;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_picc;
				funcShowCustomLogoStringId1 = R.string.copyright_picc1;
				funcShowCustomLogoStringId2 = R.string.copyright_picc2;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				app_name = context.getString(R.string.app_name_picc);
				break;
			case MARKET_CMCC_CPIC:
				serverAddress = "112.33.0.187";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_cpic;
				funcShowCustomLogoStringId1 = R.string.copyright_cpic1;
				funcShowCustomLogoStringId2 = R.string.copyright_cpic2;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				app_name = context.getString(R.string.app_name_cpic);
				break;
			case MARKET_CMCC_SUBPLATFORM_GX:
				serverAddress = "112.33.0.89";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_cmcc;
				funcShowCustomLogoStringId1 = R.string.copyright_cmcc1;
				funcShowCustomLogoStringId2 = R.string.copyright_cmcc2;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				funcChannelCallIn = false;
				funcTaskDispatch = true;
				funcAttendance = true;
				SUB_PLATFORM_VALID = true;
				SUB_PLATFORM_ADDRESS_DM = "112.33.0.89";
				SUB_PLATFORM_ADDRESS_WEB = "http://112.33.0.89:1880/airtalkeemobile/mobile/mobileAction.action";
				SUB_PLATFORM_ADDRESS_NOTICE = "http://112.33.0.89:2880/airtalkeenotice/jsp/";
				app_name = context.getString(R.string.app_name_cmcc);
				break;
			case MARKET_SONIM_TELECOM:
				serverAddress = "42.123.89.4";
				funcStatisticNetwork = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_HUBEI_MOBILE:
				serverAddress = "ptt.weptt.com";
				funcStatisticNetwork = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_NAVIDOG_TEST:
				serverAddress = "211.103.234.238";
				funcStatisticNetwork = true;
				break;
			case MARKET_CHINA_TELECOM:
				serverAddress = "202.153.169.43";
				funcStatisticNetwork = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_CMCC_35:
				serverAddress = "112.33.0.187";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_cmcc;
				funcShowCustomLogoStringId1 = R.string.copyright_cmcc1;
				funcShowCustomLogoStringId2 = R.string.copyright_cmcc2;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				app_name = context.getString(R.string.app_name_cmcc);
				break;
			case MARKET_CHINA_35:
				serverAddress = "202.3.175.181";
				funcStatisticNetwork = true;
				break;
			case MARKET_CHINA_35_P:
				serverAddress = "219.239.230.252";
				funcStatisticNetwork = true;
				break;
			case MARKET_DATANG:
				serverAddress = "ptt.weptt.com";
				funcStatisticNetwork = true;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_datang;
				funcShowCustomLogoStringId1 = R.string.copyright_datang1;
				funcShowCustomLogoStringId2 = R.string.copyright_datang2;
				break;
			case MARKET_DATANG_51POC:
				serverAddress = "51poc.cn";
				funcStatisticNetwork = true;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_datang;
				funcShowCustomLogoStringId1 = R.string.copyright_datang1;
				funcShowCustomLogoStringId2 = R.string.copyright_datang2;
				break;
			case MARKET_TRACKSYSTEM_GUANGZHOU:
				serverAddress = "10.2.21.185";
				funcBroadcast = false;
				funcStatisticNetwork = true;
				funcUserIdAndPwdIsNumber = false;
				break;
			case MARKET_TRACKSYSTEM_SHANGHAI:
				serverAddress = "112.124.127.62";
				funcBroadcast = false;
				funcStatisticNetwork = true;
				funcUserIdAndPwdIsNumber = false;
				break;
			case MARKET_BPER:
				serverAddress = "ptt.weptt.com";
				funcCenterCallMenuShow = false;
				funcCenterLocationMenuShow = true;
				funcThemeChange = false;
				pttButtonPressWakeupWhenStandby = false;
				app_name = context.getString(R.string.app_name_bper);
				break;
			case MARKET_BPER_SINGAPORE:
				serverAddress = "117.120.4.106";
				funcCenterCallMenuShow = false;
				funcCenterLocationMenuShow = true;
				pttButtonPressWakeupWhenStandby = false;
				funcThemeChange = false;
				app_name = context.getString(R.string.app_name_bper);
				break;
			case MARKET_BPER_GERMANY:
				serverAddress = "bper.isafe-mobile.com";
				funcCenterCallMenuShow = false;
				funcCenterLocationMenuShow = true;
				pttButtonPressWakeupWhenStandby = false;
				funcThemeChange = false;
				app_name = context.getString(R.string.app_name_bper);
				break;
			case MARKET_BPER_RUSSIA_TAXI:
				serverAddress = "bper.taxi956.ru";
				funcCenterCallMenuShow = false;
				funcCenterLocationMenuShow = true;
				pttButtonPressWakeupWhenStandby = false;
				funcThemeChange = false;
				app_name = context.getString(R.string.app_name_bper);
				break;
			case MARKET_BPER_MTT:
				serverAddress = "54.72.128.85";
				funcCenterCallMenuShow = false;
				funcCenterLocationMenuShow = true;
				pttButtonPressWakeupWhenStandby = false;
				funcThemeChange = false;
				app_name = context.getString(R.string.app_name_bper_mtt);
				break;
			case MARKET_JIZHEN_SHENYANG:
				serverAddress = "61.161.191.154";
				funcStatisticNetwork = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_JIZHEN_LIAONING:
				serverAddress = "18.18.18.50";
				funcStatisticNetwork = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_UNICOM:
				serverAddress = "ptt.weptt.com";
				funcStatisticNetwork = true;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_unicom;
				funcShowCustomLogoStringId1 = R.string.copyright_unicomgz1;
				funcShowCustomLogoStringId2 = R.string.copyright_unicomgz2;
				pttEarphonePlug = true;
				break;
			case MARKET_UNICOM_GUIZHOU:
				serverAddress = "poclient.gzunicom.com.cn";
				pttVolumeKeySupport = true;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_unicom;
				funcShowCustomLogoStringId1 = R.string.copyright_unicomgz1;
				funcShowCustomLogoStringId2 = R.string.copyright_unicomgz2;
				app_name = context.getString(R.string.app_name_unicom);
				break;
			case MARKET_JINYUANXINTONG:
				serverAddress = "115.56.226.244";
				pttVolumeKeySupport = true;
				break;
			case MARKET_TAITONGYUAN:
				serverAddress = "60.210.98.53";
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_taitongyuan;
				funcShowCustomLogoStringId1 = R.string.copyright_taitongyuan1;
				funcShowCustomLogoStringId2 = R.string.copyright_taitongyuan2;
				break;
			case MARKET_GONGAN_WEIFANG:
				serverAddress = "ptt.weptt.com";
				serverUrlInfosys = "http://dl.airtalkee.com/infosys/wfinfo.apk";
				funcStatisticNetwork = true;
				break;
			case MARKET_GONGAN_NANJING:
				serverAddress = "ptt.weptt.com";
				funcStatisticNetwork = true;
				break;
			case MARKET_GONGAN_MTG:
				serverAddress = "111.207.23.93";
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_WUJING_SHANDONG:
				serverAddress = "11.21.144.3";
				funcStatisticNetwork = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_GONGAN_JINGWEIJU:
				serverAddress = "192.168.234.178";
				funcFORCE = true;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				funcCenterReport = false;
				funcCenterLocation = AirFunctionSetting.SETTING_DISABLE;
				funcBroadcast = true;
				funcStatisticNetwork = false;
				funcStatisticUmeng = false;
				funcChannelManage = false;
				funcManual = false;
				funcUserAll = false;
				funcThemeChange = false;
				funcPlayMediaTalkOff = true;
				break;
			case MARKET_GUGONG:
				serverAddress = "ptt.weptt.com";
				funcStatisticNetwork = true;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_gugong;
				funcShowCustomLogoStringId1 = R.string.copyright_gugong1;
				funcShowCustomLogoStringId2 = R.string.copyright_gugong2;
				break;
			case MARKET_REALWAY_TAIYUAN:
				serverAddress = "221.204.187.229";
				funcFORCE = true;
				funcCenterCall = AirFunctionSetting.SETTING_ENABLE;
				funcCenterReport = true;
				funcCenterLocation = AirFunctionSetting.SETTING_LOCATION_FORCE;
				funcStatisticNetwork = false;
				funcChannelManage = false;
				funcManual = true;
				funcBroadcast = false;
				break;
			case MARKET_FIRE_BJ:
				serverAddress = "10.8.72.32";
				funcFORCE = true;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				funcCenterReport = false;
				funcCenterLocation = AirFunctionSetting.SETTING_ENABLE;
				funcBroadcast = false;
				funcStatisticNetwork = false;
				funcStatisticUmeng = false;
				funcChannelManage = false;
				funcManual = false;
				funcfeedback = false;
				funcUserAll = false;
				engineMediaSettingHbSeconds = ENGINE_MEDIA_HB_SECOND_FAST;
				engineMediaSettingHbPackSize = ENGINE_MEDIA_HB_SIZE_LARGE;
				break;
			case MARKET_XUZHOU:
				serverAddress = "115.28.56.223";
				funcFORCE = true;
				funcCenterCall = AirFunctionSetting.SETTING_ENABLE;
				funcCenterReport = true;
				funcCenterLocation = AirFunctionSetting.SETTING_LOCATION_FORCE;
				funcStatisticNetwork = false;
				funcManual = true;
				funcBroadcast = false;
				break;
			case MARKET_SANXIA:
				serverAddress = "192.168.206.6";
				funcStatisticNetwork = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_UNI_STRONG:
				serverAddress = "223.72.145.133";
				funcStatisticNetwork = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_LI_SHI_TECHNOLOGY:
				serverAddress = "101.71.51.192";
				funcStatisticNetwork = true;
				funcShowCustomLogo = true;
				funcVideo = true;
				funcShowCustomLogoIconId = R.drawable.logo_technology;
				funcShowCustomLogoStringId1 = R.string.copyright_lishi_technology1;
				funcShowCustomLogoStringId2 = R.string.copyright_lishi_technology2;
				break;
			case MARKET_WO_XIN_DA:
				serverAddress = "ptt.weptt.com";
				funcStatisticNetwork = true;
				funcShowCustomLogo = true;
				funcShowCustomLogoIconId = R.drawable.logo_woxinda;
				funcShowCustomLogoStringId1 = R.string.copyright;
				funcShowCustomLogoStringId2 = R.string.copyright;
				break;
			case MARKET_ACEVISION:
				serverAddress = "121.121.20.174";
				funcStatisticNetwork = true;
				pttEarphonePlug = true;
				funcVideo = true;
				funcChannelCallIn = false;
				defaultTheme = R.style.AppTheme_Dark;
				break;
			case MARKET_KCELL:
				serverAddress = "185.9.145.17";
				funcStatisticNetwork = true;
				funcUserPhone = true;
				pttEarphonePlug = true;
				defaultTheme = R.style.AppTheme_Dark;
				funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				break;
			default:
				break;
		}
	}

}
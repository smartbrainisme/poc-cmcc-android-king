package com.cmccpoc.Util;

import android.media.AudioManager;
import com.cmccpoc.config.Config;
import com.cmccpoc.services.AirServices;

/**
 * app选项设置类
 * @author Yao
 */
public class Setting
{
	private static final String SETTING_VOICE_MODE = "SETTING_VOICE_MODE";
	private static final String SETTING_VOICE_AMPLIFIER = "SETTING_VOICE_AMPLIFIER";
	private static final String SETTING_PTT_ANSWER = "SETTING_PTT_ANSWER";
	private static final String SETTING_PTT_ISB = "SETTING_PTT_ISB";
	private static final String SETTING_PTT_VOLUME = "SETTING_PTT_VOLUME";
	private static final String SETTING_PTT_CLICK = "SETTING_PTT_CLICK";
	private static final String SETTING_PTT_HB = "SETTING_PTT_HB";
	private static final String SETTING_VIDEO_RESOLUTION_W = "SETTING_VIDEO_RESOLUTION_W";
	private static final String SETTING_VIDEO_RESOLUTION_H = "SETTING_VIDEO_RESOLUTION_H";
	private static final String SETTING_VIDEO_FRAME_RATE = "SETTING_VIDEO_FRAME_RATE";

	public static final int[] VIDEO_RESOLUTION_W = { 1280, 640, 480 };
	public static final int[] VIDEO_RESOLUTION_H = { 720, 480, 320 };
	public static final String[] VIDEO_RESOLUTION_RATE = { "1280 * 720", "640 * 480", "480 * 320" };

	/**
	 * 获取音频通道模式
	 */
	public static int getVoiceMode()
	{
		return AirServices.iOperator.getInt(SETTING_VOICE_MODE, AudioManager.MODE_NORMAL);
	}

	/**
	 * 设置音频通道模式
	 */
	public static void setVoiceMode(int mode)
	{
		AirServices.iOperator.putInt(SETTING_VOICE_MODE, mode);
	}

	/**
	 * 是否开启声音放大器
	 * @return
	 */
	public static boolean getVoiceAmplifier()
	{
		return AirServices.iOperator.getBoolean(SETTING_VOICE_AMPLIFIER, Config.audioAmplifierEnabled);
	}

	/**
	 * 设置声音放大器
	 * @param enable 是否开启
	 */
	public static void setVoiceAmplifier(boolean enable)
	{
		AirServices.iOperator.putBoolean(SETTING_VOICE_AMPLIFIER, enable);
	}

	/**
	 * 获取PTT应答模式
	 * @return
	 */
	public static boolean getPttAnswerMode()
	{
		return AirServices.iOperator.getBoolean(SETTING_PTT_ANSWER, false);
	}

	/**
	 * 设置应答模式
	 * @param isAutoAnswer
	 */
	public static void setPttAnswerMode(boolean isAutoAnswer)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_ANSWER, isAutoAnswer);
	}

	/**
	 * 获取免打扰模式
	 * @return
	 */
	public static boolean getPttIsb()
	{
		return AirServices.iOperator.getBoolean(SETTING_PTT_ISB, false);
	}

	/**
	 * 设置免打扰模式
	 * @param isIsb 是否开启
	 */
	public static void setPttIsb(boolean isIsb)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_ISB, isIsb);
	}

	/**
	 * 是否支持PTT音量键
	 * @return
	 */
	public static boolean getPttVolumeSupport()
	{
		Config.pttVolumeKeySupport = AirServices.iOperator.getBoolean(SETTING_PTT_VOLUME, Config.pttVolumeKeySupport);
		return Config.pttVolumeKeySupport;
	}

	/**
	 * 设置PTT音量键
	 * @param isSupportVolumeKey 是否开启
	 */
	public static void setPttVolumeSupport(boolean isSupportVolumeKey)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_VOLUME, isSupportVolumeKey);
		Config.pttVolumeKeySupport = isSupportVolumeKey;
	}

	/**
	 * 是否支持屏幕PTT键
	 * @return
	 */
	public static boolean getPttClickSupport()
	{
		Config.pttClickSupport = AirServices.iOperator.getBoolean(SETTING_PTT_CLICK, Config.pttClickSupport);
		return Config.pttClickSupport;
	}

	/**
	 * 设置屏幕PTT键
	 * @param isSupportClick
	 */
	public static void setPttClickSupport(boolean isSupportClick)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_CLICK, isSupportClick);
		Config.pttClickSupport = isSupportClick;
	}

	/**
	 * 获取PTT心跳
	 * @return
	 */
	public static int getPttHeartbeat()
	{
		int hb = AirServices.iOperator.getInt(SETTING_PTT_HB, Config.engineMediaSettingHbSeconds);
		if (hb > Config.ENGINE_MEDIA_HB_SECOND_SLOW)
			hb = Config.ENGINE_MEDIA_HB_SECOND_SLOW;
		if (Config.engineMediaSettingHbPackSize == Config.ENGINE_MEDIA_HB_SIZE_NONE)
		{
			Config.engineMediaSettingHbSeconds = hb;
		}
		else
		{
			hb = Config.engineMediaSettingHbSeconds;
		}
		return hb;
	}

	/**
	 * 设置PTT心跳
	 * @param seconds 心跳时长
	 */
	public static void setPttHeartbeat(int seconds)
	{
		if (seconds > Config.ENGINE_MEDIA_HB_SECOND_SLOW)
			seconds = Config.ENGINE_MEDIA_HB_SECOND_SLOW;
		AirServices.iOperator.putInt(SETTING_PTT_HB, seconds);
		Config.engineMediaSettingHbSeconds = seconds;
	}

	/**
	 * 获取视频宽度
	 * @return
	 */
	public static int getVideoResolutionWidth()
	{
		return AirServices.iOperator.getInt(SETTING_VIDEO_RESOLUTION_W, 640);
	}

	/**
	 * 设置视频宽度
	 */
	public static void setVideoResolutionWidth(int width)
	{
		AirServices.iOperator.putInt(SETTING_VIDEO_RESOLUTION_W, width);
	}

	/**
	 * 获取视频高度
	 * @return
	 */
	public static int getVideoResolutionHeight()
	{
		return AirServices.iOperator.getInt(SETTING_VIDEO_RESOLUTION_H, 480);
	}

	/**
	 * 设置视频高度
	 * @param height
	 */
	public static void setVideoResolutionHeight(int height)
	{
		AirServices.iOperator.putInt(SETTING_VIDEO_RESOLUTION_H, height);
	}

	/**
	 * 获取视频帧率
	 * @return
	 */
	public static int getVideoFrameRate()
	{
		return AirServices.iOperator.getInt(SETTING_VIDEO_FRAME_RATE, 20);
	}

	/**
	 * 设置帧率
	 * @param rate
	 */
	public static void setVideoFrameRate(int rate)
	{
		AirServices.iOperator.putInt(SETTING_VIDEO_FRAME_RATE, rate);
	}

	/**
	 * 获取视频码率
	 * @return
	 */
	public static int getVideoCodeRate()
	{
		int w = getVideoResolutionWidth();
		int h = getVideoResolutionHeight();
		int f = getVideoFrameRate();
		return (int) ((w * h + (1280 * 720)) * (f + 60) / (1000 * 180));
	}
	
	/**
	 * 获取视频分辨率
	 * @return
	 */
	public static String getVideoRate()
	{
		int w = getVideoResolutionWidth();//1280, 640, 480
		switch (w)
		{
			case 1280:
				return VIDEO_RESOLUTION_RATE[0];
			case 640:
				return VIDEO_RESOLUTION_RATE[1];
			case 480:
				return VIDEO_RESOLUTION_RATE[2];
			default:
				return VIDEO_RESOLUTION_RATE[1];
		}
		
	}
}

package com.airtalkee.Util;

import android.media.AudioManager;
import com.airtalkee.config.Config;
import com.airtalkee.services.AirServices;

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

	public static int getVoiceMode()
	{
		return AirServices.iOperator.getInt(SETTING_VOICE_MODE, AudioManager.MODE_NORMAL);
	}

	public static void setVoiceMode(int mode)
	{
		AirServices.iOperator.putInt(SETTING_VOICE_MODE, mode);
	}

	public static boolean getVoiceAmplifier()
	{
		return AirServices.iOperator.getBoolean(SETTING_VOICE_AMPLIFIER, Config.audioAmplifierEnabled);
	}

	public static void setVoiceAmplifier(boolean enable)
	{
		AirServices.iOperator.putBoolean(SETTING_VOICE_AMPLIFIER, enable);
	}

	public static boolean getPttAnswerMode()
	{
		return AirServices.iOperator.getBoolean(SETTING_PTT_ANSWER, false);
	}

	public static void setPttAnswerMode(boolean isAutoAnswer)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_ANSWER, isAutoAnswer);
	}

	public static boolean getPttIsb()
	{
		return AirServices.iOperator.getBoolean(SETTING_PTT_ISB, false);
	}

	public static void setPttIsb(boolean isIsb)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_ISB, isIsb);
	}

	public static boolean getPttVolumeSupport()
	{
		Config.pttVolumeKeySupport = AirServices.iOperator.getBoolean(SETTING_PTT_VOLUME, Config.pttVolumeKeySupport);
		return Config.pttVolumeKeySupport;
	}

	public static void setPttVolumeSupport(boolean isSupportVolumeKey)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_VOLUME, isSupportVolumeKey);
		Config.pttVolumeKeySupport = isSupportVolumeKey;
	}

	public static boolean getPttClickSupport()
	{
		Config.pttClickSupport = AirServices.iOperator.getBoolean(SETTING_PTT_CLICK, Config.pttClickSupport);
		return Config.pttClickSupport;
	}

	public static void setPttClickSupport(boolean isSupportClick)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_CLICK, isSupportClick);
		Config.pttClickSupport = isSupportClick;
	}

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

	public static void setPttHeartbeat(int seconds)
	{
		if (seconds > Config.ENGINE_MEDIA_HB_SECOND_SLOW)
			seconds = Config.ENGINE_MEDIA_HB_SECOND_SLOW;
		AirServices.iOperator.putInt(SETTING_PTT_HB, seconds);
		Config.engineMediaSettingHbSeconds = seconds;
	}

	public static int getVideoResolutionWidth()
	{
		return AirServices.iOperator.getInt(SETTING_VIDEO_RESOLUTION_W, 640);
	}

	public static void setVideoResolutionWidth(int width)
	{
		AirServices.iOperator.putInt(SETTING_VIDEO_RESOLUTION_W, width);
	}

	public static int getVideoResolutionHeight()
	{
		return AirServices.iOperator.getInt(SETTING_VIDEO_RESOLUTION_H, 480);
	}

	public static void setVideoResolutionHeight(int height)
	{
		AirServices.iOperator.putInt(SETTING_VIDEO_RESOLUTION_H, height);
	}

	public static int getVideoFrameRate()
	{
		return AirServices.iOperator.getInt(SETTING_VIDEO_FRAME_RATE, 20);
	}

	public static void setVideoFrameRate(int rate)
	{
		AirServices.iOperator.putInt(SETTING_VIDEO_FRAME_RATE, rate);
	}

	public static int getVideoCodeRate()
	{
		int w = getVideoResolutionWidth();
		int h = getVideoResolutionHeight();
		int f = getVideoFrameRate();
		return (int) ((w * h + (1280 * 720)) * (f + 60) / (1000 * 180));
	}
	
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

package com.airtalkee.Util;

import android.media.AudioManager;

import com.airtalkee.config.Config;
import com.airtalkee.sdk.engine.AirEngine;
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
	private static final String SETTING_PTT_ENCRYPT = "SETTING_PTT_ENCRYPT";
	private static final String SETTING_PTT_VOX = "SETTING_PTT_VOX";
	
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

	public static boolean getPttEncrypt()
	{
		return AirServices.iOperator.getBoolean(SETTING_PTT_ENCRYPT, true);
	}

	public static void setPttEncrypt(boolean valid)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_ENCRYPT, valid);
		AirEngine.serviceSecretSettingValidEncrypt(valid);
	}
	public static boolean getPttVox()
	{
		return AirServices.iOperator.getBoolean(SETTING_PTT_VOX, false);
	}
	
	public static void setPttVox(boolean enable)
	{
		AirServices.iOperator.putBoolean(SETTING_PTT_VOX, enable);
	}

}

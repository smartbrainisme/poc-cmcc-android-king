package com.cmccpoc.Util;

import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;

public class SoundPlayer
{
	public static final int PLAYER_MEDIA_ME_ON = 0;
	public static final int PLAYER_MEDIA_ME_OFF = 1;
	public static final int PLAYER_MEDIA_OTHER_ON = 2;
	public static final int PLAYER_MEDIA_OTHER_OFF = 3;
	public static final int PLAYER_MEDIA_ME_ON_LOW = 4;
	public static final int PLAYER_MEDIA_ME_OFF_LOW = 5;
	public static final int PLAYER_MEDIA_OTHER_ON_LOW = 6;

	public static final int POOL_MAX = 7;
	private static AudioManager audioManager;
	private static SoundPool pool;

	static class SoundInfo
	{
		int soundID = -1;
		int streamID = 0;
		int streamVolume = 0;
	}

	private static Map<Integer, SoundInfo> soundIds = new HashMap<Integer, SoundInfo>();
	private static int[] rawRes = new int[] { R.raw.sound_media_me_on, R.raw.sound_media_me_off, R.raw.sound_media_other_on,R.raw.sound_media_other_off,R.raw.sound_media_me_on_low ,R.raw.sound_media_me_off_low,R.raw.sound_media_other_on_low};

	public static void soundInit(Context context)
	{
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		pool = new SoundPool(POOL_MAX, AudioManager.STREAM_MUSIC, 100);
		for (int i = 0; i < POOL_MAX; i++)
		{
			SoundInfo info = new SoundInfo();
			info.soundID = pool.load(context, rawRes[i], 100);
			soundIds.put(i, info);
		}
	}

	/**
	 * @param isLoop
	 *            loop mode (0 = no loop, -1 = loop forever n= 0 loop to n)
	 */
	public static boolean soundPlay(int playerId, boolean loop)
	{
		boolean ok = false;
		SoundInfo info = soundIds.get(playerId);
		if (info != null)
		{
			info.streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			info.streamID = pool.play(info.soundID, info.streamVolume, info.streamVolume, 1, !loop ? 0 : -1, 1);
			Log.i(SoundPlayer.class, "soundPlay playerId=" + playerId + "  streamID=" + info.streamID);
			if (info.streamID > 0)
				ok = true;
		}
		return ok;
	}

	public static void soundStop(int playerID)
	{
		SoundInfo info = soundIds.get(playerID);
		if (info != null)
		{
			pool.stop(info.streamID);
		}
	}

	public static void soundRelease()
	{
		if (pool != null)
		{
			for (int i = 0; i < POOL_MAX; i++)
			{
				SoundInfo info = soundIds.get(i);
				pool.unload(info.soundID);
			}
			pool.release();
		}
	}

	public static void setStreamVolume(Context context, int streamVolume, int streamType,int flag)
	{
		try
		{
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			am.setStreamVolume(streamType, streamVolume, flag);
		}
		catch (Exception e)
		{

		}
	}
	
	public static int getStreamVolumeMax(Context context, int streamType)
	{
		int streamVolume = 0;
		try
		{
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			streamVolume = am.getStreamMaxVolume(streamType);
		}
		catch (Exception e)
		{

		}
		return streamVolume;
	}

	public static int getStreamVolume(Context context, int streamType)
	{
		int streamVolume = 0;
		try
		{
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			streamVolume = am.getStreamVolume(streamType);
		}
		catch (Exception e)
		{

		}
		return streamVolume;
	}
}

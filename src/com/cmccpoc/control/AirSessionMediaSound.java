package com.cmccpoc.control;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import com.airtalkee.sdk.OnMediaSoundListener;
import com.airtalkee.sdk.controller.SessionMediaController;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.Util.Sound;
import com.cmccpoc.Util.SoundPlayer;
import com.cmccpoc.Util.Util;
import com.cmccpoc.config.Config;
import com.cmccpoc.receiver.ReceiverPhoneState;

/**
 * 会话媒体音管理类
 * @author Yao
 */
public class AirSessionMediaSound implements OnMediaSoundListener
{
	private static final String PTT_ACTION = "com.android.action.PTT_LIGHT_CONTROL_ACTION";
	private static Context context = null;
	static AudioManager am;

	public AirSessionMediaSound(Context ct)
	{
		context = ct;
		am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 开始听时操作处理
	 * @param session 会话Entity
	 * @param content 内容
	 */
	@Override
	public void onMediaSoundListenBegin(AirSession session, String content)
	{
		// LED灯状态
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_OTHER_ON);
		if (!ReceiverPhoneState.isCalling)
		{
			if (VoiceManager.getInstance().getMode() == AudioManager.MODE_IN_COMMUNICATION)
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_OTHER_ON_LOW, false);
			else
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_OTHER_ON, false);
			Sound.vibrate(30, context);
		}
	}

	/**
	 * 听结束时操作处理
	 * @param session 会话Entity
	 */
	@Override
	public void onMediaSoundListenEnd(AirSession session)
	{
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_OTHER_OFF);
		if (!ReceiverPhoneState.isCalling)
		{
			if (VoiceManager.getInstance().getMode() != AudioManager.MODE_IN_COMMUNICATION)
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_OTHER_OFF, false);

		}
		Util.closeNotification(Util.NOTIFI_ID_VOICE_LISTEN);

	}

	/**
	 * 开始说话时
	 * @param session 会话Entity
	 */
	@Override
	public void onMediaSoundTalkBegin(AirSession session)
	{
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_ME_ON);
		if (VoiceManager.getInstance().getMode() == AudioManager.MODE_IN_COMMUNICATION)
			SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_ME_ON_LOW, false);
		else
			SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_ME_ON, false);

		Sound.vibrate(50, context);

	}

	/**
	 * 讲话被拒绝时
	 * @param session 会话Entity
	 */
	@Override
	public void onMediaSoundTalkDeny(AirSession session)
	{
		Sound.playSound(Sound.PLAYER_MEDIA_ERROR, context);
		Sound.vibrate(50, context);
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_TALK_DENY);
	}

	/**
	 * 讲话结束时
	 * @param session 会话Entity
	 */
	@Override
	public void onMediaSoundTalkEnd(AirSession session)
	{
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_ME_OFF);
		if (Config.funcPlayMediaTalkOff)
		{
			if (VoiceManager.getInstance().getMode() == AudioManager.MODE_IN_COMMUNICATION)
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_ME_OFF_LOW, false);
			else
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_ME_OFF, false);
		}
	}

	/**
	 * 开始请求说话
	 * @param session 会话Entity
	 */
	@Override
	public void onMediaSoundTalkRequestBegin(AirSession session)
	{
		// TODO Auto-generated method stub
		if (Config.funcPlayMediaTalkPrepare)
			Sound.playSound(Sound.PLAYER_MEDIAN_TALK_PREPARE, context);
	}

	/**
	 * 请求说话结束
	 * @param session 会话Entity
	 */
	@Override
	public void onMediaSoundTalkRequestEnd(AirSession session)
	{
		// TODO Auto-generated method stub
	}

	/**
	 * LED灯状态刷新
	 * @param session 会话Entity
	 * @param state 说话状态
	 */
	private void pttLightStateRefresh(AirSession session, int state)
	{
		try
		{
			com.airtalkee.sdk.util.Log.d(AirSessionMediaSound.class, "pttLightStateRefresh begin state =[" + state + "]");
			int color = 0xFFFF0000;
			boolean on = false;

			switch (state)
			{
				case SessionMediaController.MEDIA_SOUND_ME_ON:
					color = 0xFFFF0000;
					on = true;
					break;
				case SessionMediaController.MEDIA_SOUND_ME_OFF:
					color = 0xFFFF0000;
					on = false;
					break;
				case SessionMediaController.MEDIA_SOUND_OTHER_ON:
					color = 0xFF00FF00;
					on = true;
					break;
				case SessionMediaController.MEDIA_SOUND_OTHER_OFF:
					color = 0xFF00FF00;
					on = false;
					break;
				default:
					if (session != null)
					{
						switch (session.getMediaState())
						{
							case AirSession.MEDIA_STATE_LISTEN:
								color = 0xFF00FF00;
								on = true;
								break;
							case AirSession.MEDIA_STATE_TALK:
								color = 0xFFFF0000;
								on = true;
								break;
							case AirSession.MEDIA_STATE_IDLE:
								on = false;
								break;
						}
					}
					break;
			}

			if (on)
			{
				com.airtalkee.sdk.util.Log.d(AirSessionMediaSound.class, "pttLightStateRefresh PTT LIGHT " + color + " ON");
			}
			else
			{
				com.airtalkee.sdk.util.Log.d(AirSessionMediaSound.class, "pttLightStateRefresh PTT LIGHT " + color + " OFF");
			}
			toggleLight(on, color, context);
			changeAudioFocus(on, context);
			com.airtalkee.sdk.util.Log.d(AirSessionMediaSound.class, "pttLightStateRefresh end");
		}
		catch (Exception e)
		{
			Log.e(AirSessionMediaSound.class, "pttLightStateRefresh error =" + e.toString());
		}
	}

	/**
	 * LED灯开关
	 * @param on 是否打开
	 * @param color 颜色
	 * @param context 上下文
	 */
	public static void toggleLight(boolean on, int color, Context context)
	{
		try
		{
			Intent intent = new Intent(PTT_ACTION);
			intent.putExtra("on", on); // 打开指示灯
			intent.putExtra("color", color); // 传入值为红色，传入绿色为0xFF00FF00
			context.sendBroadcast(intent);
		}
		catch (Exception e)
		{
			Log.e(AirSessionMediaSound.class, "toggleLight error =" + e.toString());
		}
	}

	/**
	 * 更改媒体焦点
	 * @param pause 是否停止
	 * @param context 上下文
	 */
	public static void changeAudioFocus(boolean pause, Context context)
	{
		try
		{
			if (pause)
				am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			else
				am.abandonAudioFocus(afChangeListener);
		}
		catch (Exception e)
		{
			Log.e(AirSessionMediaSound.class, "changeAudioFocus error =" + e.toString());
		}
	}

	static OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener()
	{
		public void onAudioFocusChange(int focusChange)
		{

		}
	};

	/**
	 * 清除状态
	 */
	public static void destoryState()
	{
		toggleLight(false, 0xFF00FF00, context);
		toggleLight(false, 0xFFFF0000, context);
		changeAudioFocus(false, context);
	}

	private static native void setGreenLedStatusNative(int status);// status: 0 关闭灯 1 开灯
	private static native void setRedLedStatusNative(int status);// status: 0 关闭灯 1 开灯
}

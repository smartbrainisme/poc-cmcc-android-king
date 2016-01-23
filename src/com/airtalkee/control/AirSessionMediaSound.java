package com.airtalkee.control;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import com.airtalkee.R;
import com.airtalkee.Util.Sound;
import com.airtalkee.Util.SoundPlayer;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.AccountActivity;
import com.airtalkee.activity.MainActivity;
import com.airtalkee.activity.home.HomeActivity;
import com.airtalkee.bluetooth.BluetoothManager;
import com.airtalkee.config.Config;
import com.airtalkee.receiver.ReceiverPhoneState;
import com.airtalkee.sdk.OnMediaSoundListener;
import com.airtalkee.sdk.controller.SessionMediaController;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;

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

	@Override
	public void onMediaSoundListenBegin(AirSession session, String content)
	{
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_OTHER_ON);
		if (!ReceiverPhoneState.isCalling)
		{

			if (BluetoothManager.getInstance().getMode() == AudioManager.MODE_IN_COMMUNICATION)
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_OTHER_ON_LOW, false);
			else
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_OTHER_ON, false);
			Sound.vibrate(30, context);
		}

//		if (session != null && session.getType() == AirSession.TYPE_CHANNEL && !Utils.isEmpty(content))
//		{
//			boolean show = false;
//			Intent intent = null;
//			if (HomeActivity.getInstance() == null)
//			{
//				intent = new Intent();
//				intent.setClass(context, AccountActivity.class);
//				show = true;
//			}
//			else if (!HomeActivity.getInstance().isShowing)
//			{
//				intent = new Intent();
//				intent.setClass(context, MainActivity.class);
//				show = true;
//			}
//			if (show)
//			{
//				Util.showNotification(Util.NOTIFI_ID_VOICE_LISTEN, context, intent, content, content + " " + context.getString(R.string.talk_speaking), context.getString(R.string.talk_speaking), null);
//			}
//		}
	}

	@Override
	public void onMediaSoundListenEnd(AirSession session)
	{
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_OTHER_OFF);
		if (!ReceiverPhoneState.isCalling)
		{
			if (BluetoothManager.getInstance().getMode() != AudioManager.MODE_IN_COMMUNICATION)
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_OTHER_OFF, false);

		}
		Util.closeNotification(Util.NOTIFI_ID_VOICE_LISTEN);

	}

	@Override
	public void onMediaSoundTalkBegin(AirSession session)
	{
		// TODO Auto-generated method stub
		/*
		 * Sound.playSound(Sound.PLAYER_MEDIA_ME_ON, context,new
		 * OnCompletionListener() {
		 * 
		 * @Override public void onCompletion(MediaPlayer mp) { // TODO
		 * Auto-generated method stub am.setBluetoothScoOn(true);
		 * am.startBluetoothSco(); } });
		 */
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_ME_ON);
		if (BluetoothManager.getInstance().getMode() == AudioManager.MODE_IN_COMMUNICATION)
			SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_ME_ON_LOW, false);
		else
			SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_ME_ON, false);

		Sound.vibrate(50, context);

	}

	@Override
	public void onMediaSoundTalkDeny(AirSession session)
	{
		// TODO Auto-generated method stub
		Sound.playSound(Sound.PLAYER_MEDIA_ERROR, context);
		Sound.vibrate(50, context);
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_TALK_DENY);
	}

	@Override
	public void onMediaSoundTalkEnd(AirSession session)
	{
		// TODO Auto-generated method stub
		pttLightStateRefresh(session, SessionMediaController.MEDIA_SOUND_ME_OFF);
		if (Config.funcPlayMediaTalkOff)
		{
			if (BluetoothManager.getInstance().getMode() == AudioManager.MODE_IN_COMMUNICATION)
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_ME_OFF_LOW, false);
			else
				SoundPlayer.soundPlay(SoundPlayer.PLAYER_MEDIA_ME_OFF, false);
		}

		// am.setBluetoothScoOn(false);
		// am.stopBluetoothSco();
	}

	@Override
	public void onMediaSoundTalkRequestBegin(AirSession session)
	{
		// TODO Auto-generated method stub
		if (Config.funcPlayMediaTalkPrepare)
			Sound.playSound(Sound.PLAYER_MEDIAN_TALK_PREPARE, context);
	}

	@Override
	public void onMediaSoundTalkRequestEnd(AirSession session)
	{
		// TODO Auto-generated method stub
	}

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
				// Log.i(AirSessionMediaSound.class, "PTT LIGHT "+color+" ON");
				com.airtalkee.sdk.util.Log.d(AirSessionMediaSound.class, "pttLightStateRefresh PTT LIGHT " + color + " ON");
			}
			else
			{
				// Log.i(AirSessionMediaSound.class, "PTT LIGHT "+color+" OFF");
				com.airtalkee.sdk.util.Log.d(AirSessionMediaSound.class, "pttLightStateRefresh PTT LIGHT " + color + " OFF");
			}
			toggleLight(on, color, context);
			changeAudioFocus(on, context);
//			toggleLedStatus(color == 0xFFFF0000 ? 0 : 1, on ? 1 : 0);
			com.airtalkee.sdk.util.Log.d(AirSessionMediaSound.class, "pttLightStateRefresh end");
		}
		catch (Exception e)
		{
			Log.e(AirSessionMediaSound.class, "pttLightStateRefresh error =" + e.toString());
		}
	}

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

	public static void destoryState()
	{
		toggleLight(false, 0xFF00FF00, context);
		toggleLight(false, 0xFFFF0000, context);
		changeAudioFocus(false, context);
//		toggleLedStatus(0, 0);
//		toggleLedStatus(1, 0);
	}

	private static native void setGreenLedStatusNative(int status);// status: 0
																	// 关闭灯 1 开灯

	private static native void setRedLedStatusNative(int status);// status: 0
																	// 关闭灯 1 开灯
}

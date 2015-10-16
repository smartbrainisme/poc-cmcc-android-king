package com.airtalkee.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.Setting;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;

public class MenuSettingPttActivity extends ActivityBase implements OnClickListener, OnSeekBarChangeListener, OnCheckedChangeListener
{

	private ImageView mVoiceModeOn;
	private ImageView mVoiceModeOff;
	private CheckBox mVoiceAmplifier, mPttClick, mPttVolume, mPttAnswer, mPttIsb;
	View pttAnswerLayout;
	private SeekBar mVoiceVolumeSeekBar;
	private TextView mVoiceModeText;
	private TextView mFrequenceText;
	private int[] mFrequenceValue = { Config.ENGINE_MEDIA_HB_SECOND_HIGH, Config.ENGINE_MEDIA_HB_SECOND_FAST, Config.ENGINE_MEDIA_HB_SECOND_MEDIUM,
		Config.ENGINE_MEDIA_HB_SECOND_SLOW };
	private String[] mFrequence = null;
	private int mFrequenceSelected = 0;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_setting_ptt);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_setting_voice);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		findViewById(R.id.talk_setting_voice_amplifier).setOnClickListener(this);
		mVoiceAmplifier = (CheckBox) findViewById(R.id.talk_setting_voice_amplifier_check);
		mVoiceAmplifier.setChecked(Setting.getVoiceAmplifier());
		mVoiceAmplifier.setOnCheckedChangeListener(this);

		mVoiceVolumeSeekBar = (SeekBar) findViewById(R.id.SoundSettingBarView);
		mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
		mVoiceVolumeSeekBar.setOnSeekBarChangeListener(this);

		findViewById(R.id.talk_setting_voice_mode).setOnClickListener(this);
		mVoiceModeText = (TextView) findViewById(R.id.talk_setting_voice_mode_text);
		mVoiceModeOn = (ImageView) findViewById(R.id.talk_setting_voice_mode_on);
		mVoiceModeOff = (ImageView) findViewById(R.id.talk_setting_voice_mode_off);
		readVoiceMode();

		pttAnswerLayout = findViewById(R.id.talk_setting_answer);
		pttAnswerLayout.setOnClickListener(this);
		mPttAnswer = (CheckBox) findViewById(R.id.talk_setting_answer_check);
		mPttAnswer.setChecked(Setting.getPttAnswerMode());
		mPttAnswer.setOnCheckedChangeListener(this);

		findViewById(R.id.talk_setting_isb).setOnClickListener(this);
		mPttIsb = (CheckBox) findViewById(R.id.talk_setting_isb_check);
		mPttIsb.setChecked(Setting.getPttIsb());
		mPttIsb.setOnCheckedChangeListener(this);

		if (Config.pttButtonVisibility == View.VISIBLE)
		{
			findViewById(R.id.talk_setting_ptt_click).setOnClickListener(this);
			mPttClick = (CheckBox) findViewById(R.id.talk_setting_ptt_click_check);
			mPttClick.setChecked(Setting.getPttClickSupport());
			mPttClick.setOnCheckedChangeListener(this);
		}
		else
		{
			findViewById(R.id.talk_setting_ptt_click).setVisibility(View.GONE);
			findViewById(R.id.talk_setting_ptt_click_line).setVisibility(View.GONE);
		}

		if (Config.pttButtonKeycode == KeyEvent.KEYCODE_UNKNOWN && Utils.isEmpty(Config.pttButtonAction))
		{
			findViewById(R.id.talk_setting_ptt_volume).setOnClickListener(this);
			mPttVolume = (CheckBox) findViewById(R.id.talk_setting_ptt_volume_check);
			mPttVolume.setChecked(Setting.getPttVolumeSupport());
			mPttVolume.setOnCheckedChangeListener(this);
		}
		else
		{
			findViewById(R.id.talk_setting_ptt_volume).setVisibility(View.GONE);
			findViewById(R.id.talk_setting_ptt_volume_line).setVisibility(View.GONE);
		}

		if (Config.engineMediaSettingHbPackSize == Config.ENGINE_MEDIA_HB_SIZE_NONE)
		{
			findViewById(R.id.talk_setting_hb).setOnClickListener(this);
			mFrequenceText = (TextView) findViewById(R.id.talk_setting_hb_text);
			mFrequence = getResources().getStringArray(R.array.hb_setting);
			int hb = Setting.getPttHeartbeat();
			for (int i = 0; i < mFrequenceValue.length; i++)
			{
				if (hb == mFrequenceValue[i])
				{
					mFrequenceSelected = i;
					break;
				}
			}
			mFrequenceText.setText(getString(R.string.talk_tools_setting_hb) + " (" + mFrequence[mFrequenceSelected] + ")");
		}
		else
		{
			findViewById(R.id.talk_setting_hb).setVisibility(View.GONE);
			findViewById(R.id.talk_setting_hb_line).setVisibility(View.GONE);
		}
		
		refreshPttAnswerItem();
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
	}

	private void readVoiceMode()
	{
		if (Util.getMode(this) == AudioManager.MODE_NORMAL)
		{
			mVoiceModeText.setText(getString(R.string.talk_tools_setting_voice_mode_speaker));
			mVoiceModeOn.setVisibility(View.VISIBLE);
			mVoiceModeOff.setVisibility(View.GONE);
		}
		else
		{
			mVoiceModeText.setText(getString(R.string.talk_tools_setting_voice_mode_earphone));
			mVoiceModeOn.setVisibility(View.GONE);
			mVoiceModeOff.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.talk_setting_voice_amplifier:
				mVoiceAmplifier.setChecked(!mVoiceAmplifier.isChecked());
				break;
			case R.id.talk_setting_voice_mode:
			{
				Util.setMode(this);
				readVoiceMode();
				new AlertDialog.Builder(this).setTitle(R.string.talk_tools_setting_hb).setItems(R.array.play_mode, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (mFrequenceSelected != which)
						{
							mFrequenceSelected = which;
							mFrequenceText.setText(getString(R.string.talk_tools_setting_hb) + " (" + mFrequence[mFrequenceSelected] + ")");
							Setting.setPttHeartbeat(mFrequenceValue[mFrequenceSelected]);
							AirtalkeeSessionManager.getInstance().setMediaEngineSetting(Config.engineMediaSettingHbSeconds, Config.engineMediaSettingHbPackSize);
						}
					}
				}).show();
				break;
			}
			case R.id.talk_setting_voice_volume:
				break;
			case R.id.talk_setting_answer:
				mPttAnswer.setChecked(!mPttAnswer.isChecked());
				break;
			case R.id.talk_setting_isb:
				mPttIsb.setChecked(!mPttIsb.isChecked());
				break;
			case R.id.talk_setting_ptt_click:
				mPttClick.setChecked(!mPttClick.isChecked());
				break;
			case R.id.talk_setting_ptt_volume:
				mPttVolume.setChecked(!mPttVolume.isChecked());
				break;
			case R.id.talk_setting_hb:
				new AlertDialog.Builder(this).setTitle(R.string.talk_tools_setting_hb).setItems(R.array.hb_setting, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (mFrequenceSelected != which)
						{
							mFrequenceSelected = which;
							mFrequenceText.setText(getString(R.string.talk_tools_setting_hb) + " (" + mFrequence[mFrequenceSelected] + ")");
							Setting.setPttHeartbeat(mFrequenceValue[mFrequenceSelected]);
							AirtalkeeSessionManager.getInstance().setMediaEngineSetting(Config.engineMediaSettingHbSeconds, Config.engineMediaSettingHbPackSize);
						}
					}
				}).show();
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		// TODO Auto-generated method stub
		if (buttonView != null)
		{
			switch (buttonView.getId())
			{
				case R.id.talk_setting_voice_amplifier_check:
					Setting.setVoiceAmplifier(mVoiceAmplifier.isChecked());
					AirtalkeeSessionManager.getInstance().setAudioAmplifier(mVoiceAmplifier.isChecked());
					break;

				case R.id.talk_setting_answer_check:
					Setting.setPttAnswerMode(mPttAnswer.isChecked());
					if (mPttAnswer.isChecked())
						AirtalkeeSessionManager.getInstance().setSessionDialogSetAnswerMode(AirSession.INCOMING_MODE_AUTO);
					else
						AirtalkeeSessionManager.getInstance().setSessionDialogSetAnswerMode(AirSession.INCOMING_MODE_MANUALLY);
					break;
					
				case R.id.talk_setting_isb_check:
					Setting.setPttIsb(mPttIsb.isChecked());
					if (mPttIsb.isChecked())
						AirtalkeeSessionManager.getInstance().setSessionDialogSetIsbMode(true);
					else
						AirtalkeeSessionManager.getInstance().setSessionDialogSetIsbMode(false);
					
					refreshPttAnswerItem();
					break;

				case R.id.talk_setting_ptt_click_check:
					Setting.setPttClickSupport(mPttClick.isChecked());
					break;

				case R.id.talk_setting_ptt_volume_check:
					Setting.setPttVolumeSupport(mPttVolume.isChecked());
					break;

				default:
					break;
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		boolean handled = false;
		switch (event.getKeyCode())
		{
			case KeyEvent.KEYCODE_VOLUME_UP:
				Util.setStreamVolumeUp(this);
				mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
				handled = true;
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				Util.setStreamVolumeDown(this);
				mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
				handled = true;
				break;
			case KeyEvent.KEYCODE_CAMERA:
				if (event.getAction() == KeyEvent.ACTION_UP)
				{
					Util.setMode(this);
					readVoiceMode();
				}
				handled = true;
				break;
		}
		return handled ? handled : super.dispatchKeyEvent(event);
	}
	
	private void refreshPttAnswerItem()
	{
		if(mPttIsb != null )
		{
			if(mPttIsb.isChecked())
			{
				pttAnswerLayout.setClickable(false);
				pttAnswerLayout.setEnabled(false);
				mPttAnswer.setChecked(false);
				mPttAnswer.setClickable(false);
				mPttAnswer.setEnabled(false);
			}
			else
			{
				pttAnswerLayout.setClickable(true);
				pttAnswerLayout.setEnabled(true);
				mPttAnswer.setClickable(true);
				mPttAnswer.setEnabled(true);
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		// TODO Auto-generated method stub
		Util.setStreamVolume(this, progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub

	}

}

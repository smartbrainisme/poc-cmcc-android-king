package com.cmccpoc.activity.home.widget;

import java.util.Date;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.Util.AirMmiTimer;
import com.cmccpoc.Util.AirMmiTimerListener;
import com.cmccpoc.Util.Toast;
import com.cmccpoc.Util.Util;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.services.AirServices;

public class StatusTalkBtn extends LinearLayout implements OnTouchListener, AirMmiTimerListener
{
	private final int TIMEOUT_LONG_CLICK = 200;
	private AirSession session;
	private View btnTalk, textLay;
	private ImageView bgTalkBack, bgTalkFront;
	private TextView tvBold, tvNormal;
	private boolean isTalkLongClick = false;
	private AirChannel channel = null;

	public StatusTalkBtn(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater.from(this.getContext()).inflate(R.layout.include_home_talkbtn, this);
	}

	public void setSession(AirSession s)
	{
		this.session = s;
		if (s != null)
		{
			channel = AirtalkeeChannel.getInstance().ChannelGetByCode(s.getSessionCode());
		}
		refreshPttButton();
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		initFindView();
	}

	private void initFindView()
	{
		btnTalk = findViewById(R.id.media_ptt_box);
		textLay = findViewById(R.id.media_talk_text_lay);
		bgTalkBack = (ImageView) findViewById(R.id.media_ptt_talk_press_bg);
		bgTalkFront = (ImageView) findViewById(R.id.media_ptt_talk_press_bg_front);
		tvBold = (TextView) findViewById(R.id.media_ptt_talk_text1);
		tvNormal = (TextView) findViewById(R.id.media_ptt_talk_text2);
		btnTalk.setOnTouchListener(this);
	}

	public void refreshPttButton()
	{
		if (session == null)
			return;
		Log.d(StatusBarTitle.class, "session button state = " + session.getMediaButtonState());
		switch (session.getMediaButtonState())
		{
			case AirSession.MEDIA_BUTTON_STATE_IDLE:
				if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					bgTalkFront.setVisibility(View.VISIBLE);
					bgTalkBack.setVisibility(View.INVISIBLE);
					textLay.setVisibility(View.GONE);
					bgTalkFront.setImageResource(R.drawable.btn_talk_idle_new);
				}
				else
				{
					bgTalkFront.setVisibility(View.VISIBLE);
					bgTalkBack.setVisibility(View.INVISIBLE);
					textLay.setVisibility(View.GONE);
					bgTalkFront.setImageResource(R.drawable.btn_talk_disconnect);
					if (session.getType() == AirSession.TYPE_DIALOG && Config.pttButtonVisibility == View.VISIBLE)
					{
//						btnTalkCall.setVisibility(View.VISIBLE);
//						btnTalkCall.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_call_idle, contextMain));
					}
				}
//				refreshMediaState();
				break;
			case AirSession.MEDIA_BUTTON_STATE_CONNECTING:

				bgTalkFront.setVisibility(View.VISIBLE);
				bgTalkBack.setVisibility(View.INVISIBLE);
				textLay.setVisibility(View.VISIBLE);
				bgTalkFront.setImageResource(R.drawable.btn_talk_empy);
				tvBold.setVisibility(View.VISIBLE);
				tvNormal.setVisibility(View.VISIBLE);
				tvBold.setText("连接中");
				tvNormal.setText("...");

				if (session.getType() == AirSession.TYPE_DIALOG && Config.pttButtonVisibility == View.VISIBLE)
				{
//					btnTalkCall.setVisibility(View.VISIBLE);
//					btnTalkCall.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_call_ing, contextMain));
				}
				break;
			case AirSession.MEDIA_BUTTON_STATE_TALKING:

				bgTalkFront.setVisibility(View.GONE);
				bgTalkBack.setVisibility(View.VISIBLE);
				textLay.setVisibility(View.GONE);

				break;
			case AirSession.MEDIA_BUTTON_STATE_QUEUE:
				bgTalkFront.setVisibility(View.VISIBLE);
				bgTalkBack.setVisibility(View.INVISIBLE);
				textLay.setVisibility(View.VISIBLE);
				bgTalkFront.setImageResource(R.drawable.btn_talk_empy);
				tvBold.setVisibility(View.VISIBLE);
				tvNormal.setVisibility(View.VISIBLE);
				Log.d(StatusBarTitle.class, "queues size = " + session.usersQueues().size());
				tvBold.setText(session.usersQueues().size()+"");
				tvNormal.setText("排队中");
				break;
			case AirSession.MEDIA_BUTTON_STATE_REQUESTING:
				bgTalkFront.setVisibility(View.VISIBLE);
				bgTalkBack.setVisibility(View.INVISIBLE);
				textLay.setVisibility(View.VISIBLE);
				bgTalkFront.setImageResource(R.drawable.btn_talk_empy);
				tvBold.setVisibility(View.VISIBLE);
				tvNormal.setVisibility(View.VISIBLE);
				tvBold.setText("申请中");
				tvNormal.setText("...");
				break;
			case AirSession.MEDIA_BUTTON_STATE_RELEASING:
				bgTalkFront.setVisibility(View.VISIBLE);
				bgTalkBack.setVisibility(View.INVISIBLE);
				textLay.setVisibility(View.VISIBLE);
				bgTalkFront.setImageResource(R.drawable.btn_talk_empy);
				tvBold.setVisibility(View.VISIBLE);
				tvNormal.setVisibility(View.VISIBLE);
				tvBold.setText("释放中");
				tvNormal.setText("...");
				break;
		}
	}

	private Date dateOld = null;
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		switch (v.getId())
		{
			case R.id.media_ptt_box:
			{
				try
				{
					boolean isAction = false;
					if (v.getId() == R.id.talk_btn_session)
					{
						if (event.getAction() == MotionEvent.ACTION_DOWN)
						{
							double d = Math.sqrt((btnTalk.getWidth() / 2 - event.getX()) * (btnTalk.getWidth() / 2 - event.getX()) + (btnTalk.getHeight() / 2 - event.getY()) * (btnTalk.getHeight() / 2 - event.getY()));
							if (btnTalk.getWidth() / 2 >= d)
							{
								isAction = true;
							}
						}
						else
							isAction = true;
					}
					else
						isAction = true;

					if (isAction && session != null)
					{
						if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
						{
							if (Config.pttClickSupport)
							{
								if (event.getAction() == MotionEvent.ACTION_DOWN)
								{
									isTalkLongClick = false;
									v.setPressed(true);
									AirMmiTimer.getInstance().TimerRegister(getContext(), this, false, true, TIMEOUT_LONG_CLICK, false, null);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
								{
									AirMmiTimer.getInstance().TimerUnregister(getContext(), this);
									if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
									{
										if (isTalkLongClick)
										{
											AirtalkeeSessionManager.getInstance().TalkRelease(session);
											isTalkLongClick = false;
											v.setPressed(false);
										}
										else
										{
											AirtalkeeSessionManager.getInstance().TalkButtonClick(session, channel != null ? channel.isRoleAppling() : false);
										}
									}
									isTalkLongClick = false;
								}
							}
							else
							{
								if (event.getAction() == MotionEvent.ACTION_DOWN)
								{
									AirtalkeeSessionManager.getInstance().TalkRequest(session, channel != null ? channel.isRoleAppling() : false);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
								{
									AirtalkeeSessionManager.getInstance().TalkRelease(session);
								}
							}
						}
						else if (session.getSessionState() == AirSession.SESSION_STATE_CALLING)
						{
							if (event.getAction() == MotionEvent.ACTION_DOWN)
							{
								Date dateNew = new Date();
								float seconds = (dateNew.getTime() - dateOld.getTime()) / 1000f;
								Log.i(StatusTalkBtn.class, "StatusTalkBtn seconds ="+seconds);
								if(seconds < 0.5)
								{
									Toast.makeText1(AirServices.getInstance(), "点击间隔太短", Toast.LENGTH_SHORT).show();	
									return false;
								}
								if (session.getType() == AirSession.TYPE_DIALOG)
								{
									AirSessionControl.getInstance().SessionEndCall(session);
								}
							}
							else if (event.getAction() == MotionEvent.ACTION_UP) 
							{
								dateOld = new Date();
							}
						}
						else if (session.getSessionState() == AirSession.SESSION_STATE_IDLE)
						{
							if (event.getAction() == MotionEvent.ACTION_DOWN)
							{
								if (session.getType() == AirSession.TYPE_DIALOG)
								{
									AirSessionControl.getInstance().SessionMakeCall(session);
								}
								else if (session.getType() == AirSession.TYPE_CHANNEL)
								{
									if (AirtalkeeAccount.getInstance().isEngineRunning())
									{
										AirSessionControl.getInstance().SessionChannelIn(session.getSessionCode());
									}
									else
									{
										Util.Toast(getContext(), getContext().getString(R.string.talk_network_warning));
									}
								}
							}
							else if (event.getAction() == MotionEvent.ACTION_UP) 
							{
								dateOld = null;
							}
						}
						return true;

					}
					break;
				}
				catch (Exception e)
				{ }
			}
		}
		return false;
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		// TODO Auto-generated method stub
		try
		{
			isTalkLongClick = true;
			AirtalkeeSessionManager.getInstance().TalkRequest(session, channel != null ? channel.isRoleAppling() : false);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

}

package com.airtalkee.activity.home.widget;

import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Toast;
import com.airtalkee.activity.MoreActivity;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.MessageController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;

public class StatusBarTitle extends LinearLayout implements OnClickListener
{
	private TextView tvTitle, tvMediaStatus;
	private ImageView ivMeidiaStatus;
	private View btnLeft, btnRight;
	private ImageView ivBtnLeft, ivUnReadDot, ivNoticeUnread;
	private AirSession session = null;

	public StatusBarTitle(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater.from(this.getContext()).inflate(R.layout.include_home_title, this);
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
		ivUnReadDot = (ImageView) findViewById(R.id.unread_dot);
		ivNoticeUnread = (ImageView) findViewById(R.id.iv_Unread);
		btnLeft = findViewById(R.id.left_button);
		btnRight = findViewById(R.id.right_button);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		ivMeidiaStatus = (ImageView) findViewById(R.id.iv_media_status);
		tvMediaStatus = (TextView) findViewById(R.id.tv_media_status);
		ivBtnLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		// findViewById(R.id.title_drag).setOnClickListener(this);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		if (Config.funcBroadcast && AirtalkeeAccount.getInstance().SystemBroadcastNumberGet() > 0)
		{
			ivNoticeUnread.setVisibility(View.VISIBLE);
		}
		else
		{
			ivNoticeUnread.setVisibility(View.GONE);
		}
	}

	public void setSession(AirSession s)
	{
		this.session = s;
		refreshMediaStatus();
		refreshNewMsg();
	}

	public void setLeftMenuInVisible()
	{
		btnLeft.setVisibility(View.INVISIBLE);
	}

	public void refreshMediaStatus()
	{
		if (session != null)
		{
			try
			{
				if (session.getType() == AirSession.TYPE_DIALOG)
				{
					btnLeft.setVisibility(View.INVISIBLE);
					tvTitle.setCompoundDrawables(null, null, null, null);
				}
				if (session.isVoiceLocked())
				{
					ivBtnLeft.setImageResource(R.drawable.ic_lock);
				}
				else
				{
					ivBtnLeft.setImageResource(R.drawable.ic_unlock);
				}
				tvTitle.setText(session.getDisplayName());
				switch (session.getSessionState())
				{
					case AirSession.SESSION_STATE_CALLING:
						tvMediaStatus.setText(R.string.talk_session_building);
						ivMeidiaStatus.setImageResource(R.drawable.media_idle);
						break;
					case AirSession.SESSION_STATE_DIALOG:
						switch (session.getMediaState())
						{
							case AirSession.MEDIA_STATE_IDLE:
							{
								tvMediaStatus.setText(R.string.talk_session_speak_idle);
								ivMeidiaStatus.setImageResource(R.drawable.media_idle);
								break;
							}
							case AirSession.MEDIA_STATE_TALK:
							{
								ivMeidiaStatus.setImageResource(R.drawable.media_talk);
								tvMediaStatus.setText(R.string.talk_speak_me);

								break;
							}
							case AirSession.MEDIA_STATE_LISTEN:
							{
								AirContact contact = session.getSpeaker();
								ivMeidiaStatus.setImageResource(R.drawable.media_listen);
								if (contact != null)
								{
									tvMediaStatus.setText(contact.getDisplayName() + "  " + this.getContext().getString(R.string.talk_speaking));
								}
								break;
							}
						}
						break;
					case AirSession.SESSION_STATE_IDLE:
						if (session.getType() == AirSession.TYPE_CHANNEL)
						{
							tvMediaStatus.setText(R.string.talk_channel_idle);
						}
						else
						{
							tvMediaStatus.setText(R.string.talk_session_speak_idle);
						}
						ivMeidiaStatus.setImageResource(R.drawable.media_idle);
						break;
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
	}

	@Override
	public void onClick(View arg0)
	{
		// TODO Auto-generated method stub
		switch (arg0.getId())
		{
			case R.id.left_button:
				if (session != null && session.getType() == AirSession.TYPE_CHANNEL && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					if (session.isVoiceLocked())
					{
						AirtalkeeSessionManager.getInstance().SessionLock(session, false);
						ivBtnLeft.setImageResource(R.drawable.ic_unlock);
						Toast.makeText1(this.getContext(), getContext().getString(R.string.talk_channel_unlock_tip), Toast.LENGTH_LONG).show();
					}
					else
					{
						AirtalkeeSessionManager.getInstance().SessionLock(session, true);
						ivBtnLeft.setImageResource(R.drawable.ic_lock);
						Toast.makeText1(this.getContext(), getContext().getString(R.string.talk_channel_lock_tip), Toast.LENGTH_LONG).show();
					}
				}
				break;
			case R.id.right_button:
				Intent it = new Intent(this.getContext(), MoreActivity.class);
				this.getContext().startActivity(it);
				break;
		}
	}

	public void refreshNewMsg()
	{
		int count = 0;

		List<AirChannel> channels = AirtalkeeChannel.getInstance().getChannels();
		for (int i = 0; i < channels.size(); i++)
		{
			AirChannel c = (AirChannel) channels.get(i);
			if (c != null)
			{
				if (c.getMsgUnReadCount() > 0)
					count++;
			}
		}
		count += MessageController.checkUnReadMessage();
		ivUnReadDot.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
	}

}

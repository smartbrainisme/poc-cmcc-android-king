package com.airtalkee.activity.home.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.MoreActivity;
import com.airtalkee.activity.home.SessionDialogActivity;
import com.airtalkee.activity.home.SessionNewActivity;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.MyRelativeLayout;

public class SessionAndChannelView extends LinearLayout implements
		OnClickListener, OnItemClickListener
{
	public interface ViewChangeListener
	{
		public void onViewChanged(String sessionCode);
	}

	private GridView gvChannels;
	private GridView gvSession;
	private AdapterChannel adapterChannel;
	private AdapterSession adapterSession;
	private TextView tvChannelTitle, tvSessionTitle;
	private CharSequence channelTitle, sessionTitle;
	private ViewChangeListener listener;

	public SessionAndChannelView(Context context, ViewChangeListener l)
	{
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(this.getContext()).inflate(R.layout.layout_channels, this);
		this.listener = l;
		
		gvChannels = (GridView) findViewById(R.id.gv_channels);
		adapterChannel = new AdapterChannel(this.getContext(), null);
		tvChannelTitle = (TextView) findViewById(R.id.tv_channel_title);
		channelTitle = tvChannelTitle.getText();
		gvChannels.setAdapter(adapterChannel);
		gvChannels.setOnItemClickListener(this);
		
		gvSession = (GridView) findViewById(R.id.gv_session);
		tvSessionTitle = (TextView) findViewById(R.id.tv_session_title);
		sessionTitle = tvSessionTitle.getText();
		adapterSession = new AdapterSession(getContext(), null);
		gvSession.setAdapter(adapterSession);
		gvSession.setOnItemClickListener(this);
		
		findViewById(R.id.channel_button_more).setOnClickListener(this);
		findViewById(R.id.iv_setting).setOnClickListener(this);

		registerSessionUpdateListener();
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();

	}

	public void resume()
	{
		if (adapterChannel != null)
		{
			adapterChannel.notifyDataSetChanged();
			tvChannelTitle.setText(channelTitle + "(" + adapterChannel.getCount() + ")");
		}
		if (adapterSession != null)
		{
			adapterSession.notifyDataSetChanged();
			tvSessionTitle.setText(sessionTitle + "(" + (adapterSession.getCount() - 1) + ")");
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.channel_button_more:
				Intent it = new Intent(this.getContext(), MoreActivity.class);
				this.getContext().startActivity(it);
				break;
//			case R.id.iv_setting:
//			{
//				
//				break;
//			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		switch (parent.getId())
		{
			case R.id.gv_channels:
				AirChannel channel = (AirChannel) adapterChannel.getItem(position);
				if (channel != null)
				{
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						AirSessionControl.getInstance().SessionChannelIn(channel.getId());
						if (listener != null)
						{
							listener.onViewChanged(channel.getId());
						}
					}
					else
					{
						Util.Toast(getContext(), getContext().getString(R.string.talk_network_warning));
					}
				}
				break;
			case R.id.gv_session:
				if (position == 0)
				{
					Intent it = new Intent(getContext(), SessionNewActivity.class);
					getContext().startActivity(it);
				}
				else
				{
					AirSession s = (AirSession) adapterSession.getItem(position);
					if (s != null)
					{
						Intent it = new Intent(getContext(), SessionDialogActivity.class);
						it.putExtra("sessionCode", s.getSessionCode());
						it.putExtra("type", AirServices.TEMP_SESSION_TYPE_RESUME);
						getContext().startActivity(it);
					}
				}
				break;
		}
	}

	private void registerSessionUpdateListener()
	{
		final IntentFilter filter = new IntentFilter();
		filter.addAction(MediaStatusBar.ACTION_ON_SESSION_UPDATE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		getContext().registerReceiver(new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				// TODO Auto-generated method stub
				if (intent.getAction().equals(MediaStatusBar.ACTION_ON_SESSION_UPDATE))
				{

					int type = intent.getIntExtra(MediaStatusBar.EXTRA_TYPE, 0);
					switch (type)
					{
						case MediaStatusBar.TYPE_ON_SESSION_ESTABLISHED:
						case MediaStatusBar.TYPE_ON_SESSION_PRESENCE:
							if (adapterChannel != null)
								adapterChannel.notifyDataSetChanged();
							break;
					}

				}
			}
		}, filter);
	}

}

package com.airtalkee.activity.home.widget;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.MoreActivity;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;

public class SessionAndChannelView extends LinearLayout implements OnClickListener,OnItemClickListener
{
	public interface ViewChangeListener
	{
		public void onViewChanged(String sessionCode);
	}
	private GridView gvChannels;
	private GridView gvSession;
	private AdapterChannel adapterChannel;
	private AdapterSession adapterSession;
	
	private ViewChangeListener listener;
	public SessionAndChannelView(Context context,ViewChangeListener l)
	{
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(this.getContext()).inflate(R.layout.layout_channels, this);
		this.listener = l;
		gvChannels = (GridView) findViewById(R.id.gv_channels);
		gvSession = (GridView)findViewById(R.id.gv_session);
		adapterChannel = new AdapterChannel(this.getContext(), null);
		gvChannels.setAdapter(adapterChannel);
		adapterSession = new AdapterSession(getContext(), null);
		gvSession.setAdapter(adapterSession);
		gvChannels.setOnItemClickListener(this);
		gvSession.setOnItemClickListener(this);
		findViewById(R.id.channel_button_more).setOnClickListener(this);
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		
	}
	
	public void resume()
	{
		if(adapterChannel != null)
			adapterChannel.notifyDataSetChanged();
		if(adapterSession != null)
			adapterSession.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.channel_button_more:
				Intent it = new Intent(this.getContext(), MoreActivity.class);
				this.getContext().startActivity(it);
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		switch(parent.getId())
		{
			case R.id.gv_channels:
				AirChannel channel = (AirChannel)adapterChannel.getItem(position);
				if(channel != null)
				{
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						AirSessionControl.getInstance().SessionChannelIn(channel.getId());
						if(listener != null)
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
				AirSession s = (AirSession)adapterSession.getItem(position);
				if(s != null)
				{
					AirServices.getInstance().switchToSessionTemp(s.getSessionCode(),  AirServices.TEMP_SESSION_TYPE_MESSAGE,
						getContext());
				}
				break;
		}
		
	}
	
}

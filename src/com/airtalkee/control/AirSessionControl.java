package com.airtalkee.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.airtalkee.Util.Sound;
import com.airtalkee.bluetooth.BluetoothManager;
import com.airtalkee.listener.OnMmiSessionListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnSessionListener;
import com.airtalkee.sdk.controller.ChannelController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;
import com.airtalkee.tts.TTSManager;


public class AirSessionControl implements OnSessionListener
{

	private static final String KEY_GROUP_KEEP = "GROUP_KEEP";
	private static final String KEY_GROUP_ATTACH = "GROUP_ATTACH_";
	
	private static final String JSON_GIDS = "gids";
	
	private  List<AirChannel> mChannels = new ArrayList<AirChannel>();
	private  List<AirSession> mSessions = new ArrayList<AirSession>();
	private  HashMap<String, AirSession> mSessionMap = new HashMap<String, AirSession>();
	private  AirSession mSessionCurrentChannel = null;

	private static AirSessionControl mInstance = null;
	private OnMmiSessionListener sessionListener = null;
	private int currentChannelIndex = 0;
	private AirChannel currentSelectChannel = null;
	private AirSessionControl()
	{}
	
	public static AirSessionControl getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirSessionControl();
			AirtalkeeSessionManager.getInstance().setOnSessionListener(mInstance);
		}
		return mInstance;
	}

	public void setOnMmiSessionListener(OnMmiSessionListener l)
	{
		this.sessionListener = l;
	}
	
	// ==============================
	// Session
	// ==============================
	
	public void SessionChannelAttach(boolean isKeep)
	{
		List<AirChannel> channels = new ArrayList<AirChannel>();
		if (isKeep)
		{
			channelJsonParse(KEY_GROUP_KEEP, channels);
		}
		else
		{
			channelJsonParse(KEY_GROUP_ATTACH + AirtalkeeAccount.getInstance().getUserId(), channels);
		}
		
		if (channels.size() > 0)
		{
			for (int i = 0; i < channels.size(); i ++)
			{
				AirSession session = AirtalkeeSessionManager.getInstance().SessionCall(channels.get(i).getId());
				sessionListPut(session, false);
			}
		}
		else
		{
			channels = AirtalkeeChannel.getInstance().getChannels();
			if (channels.size() > 0)
			{
				AirSession session = AirtalkeeSessionManager.getInstance().SessionCall(channels.get(0).getId());
				sessionListPut(session, false);
			}
		}
		channelJsonBuild(KEY_GROUP_KEEP, mChannels);
	}

	public void SessionChannelIn(String channelId)
	{
		AirSession s = mSessionMap.get(channelId);
		if (s != null && s.getSessionState() == AirSession.SESSION_STATE_DIALOG)
		{
			sessionListPut(s, true);
			//AirtalkeeSessionManager.getInstance().SessionLock(s, true);
		}
		else
		{
			AirSession session = AirtalkeeSessionManager.getInstance().SessionCall(channelId);
			sessionListPut(session, true);
		}
	}
	
	public void SessionChannelOut(String channelId)
	{
		AirtalkeeSessionManager.getInstance().SessionBye(channelId);
	}

	public void SessionMakeCall(AirSession session)
	{
		sessionListPut(session, false);
		AirtalkeeSessionManager.getInstance().SessionCall(session);
	}

	public void SessionMakeSpecialCall(AirSession session)
	{
		sessionListPut(session, false);
		AirtalkeeSessionManager.getInstance().SessionCallSpecial(session);
	}

	public void SessionEndCall(AirSession session)
	{
		Sound.stopSound(Sound.PLAYER_CALL_DIAL);
		AirtalkeeSessionManager.getInstance().SessionBye(session);
	}
	

	// ==============================
	// Session list management
	// ==============================
	
	public AirSession getCurrentSession()
	{
		AirSession session = null;
		if (mSessions.size() > 0)
		{
			session = mSessions.get(mSessions.size() - 1);
		}
		return session;
	}
	
	public boolean getCurrentSessionGrap()
	{
		boolean isGrap = false;
		AirSession session = getCurrentSession();
		if (session != null && session.getChannel() != null)
		{
			isGrap = session.getChannel().isRoleAppling();
		}
		return isGrap;
	}
	
	public void setCurrentChannelSession(AirSession session)
	{
		if (session != null && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
		{
			sessionListPut(session, true);
		}
	}
	
	public AirSession getCurrentChannelSession()
	{
		return mSessionCurrentChannel;
	}

	private void sessionListPut(AirSession session, boolean save)
	{
		if (session != null)
		{
			AirSession s = mSessionMap.get(session.getSessionCode());
			if (s != null)
			{
				mSessionMap.remove(session.getSessionCode());
				mSessions.remove(session);
				if (session.getChannel() != null)
					mChannels.remove(session.getChannel());
			}
			mSessions.add(session);
			mSessionMap.put(session.getSessionCode(), session);
			if (session.getChannel() != null)
				mChannels.add(session.getChannel());
			
			if (session.getType() == AirSession.TYPE_CHANNEL)
			{
				mSessionCurrentChannel = session;
			}
			
			if (save)
				channelJsonBuild(KEY_GROUP_KEEP, mChannels);
		}
	}
	
	private void sessionListRemove(AirSession session)
	{
		if (session != null)
		{
			mSessions.remove(session);
			mSessionMap.remove(session.getSessionCode());
			if (session.getChannel() != null)
				mChannels.remove(session.getChannel());
			if (session == mSessionCurrentChannel)
			{
				mSessionCurrentChannel = null;
				for (int i = mSessions.size() - 1; i >= 0; i --)
				{
					AirSession s = mSessions.get(i);
					if (s != null && s.getType() == AirSession.TYPE_CHANNEL)
					{
						mSessionCurrentChannel = s;
					}
				}
			}
			channelJsonBuild(KEY_GROUP_KEEP, mChannels);
		}
	}
	
	// ==============================
	// Events
	// ==============================

	@Override
	public void onSessionEstablished(AirSession session, boolean isOk)
	{
		// TODO Auto-generated method stub
		if (session != null)
		{
			if (session.getType() == AirSession.TYPE_DIALOG)
			{
				Sound.stopSound(Sound.PLAYER_CALL_DIAL);
				Sound.playSound(Sound.PLAYER_CALL_BEGIN, false, AirServices.getInstance());
				sessionListPut(session, false);
				//AirtalkeeSessionManager.getInstance().SessionLock(session, true);
			}
			else
			{
				channelIndexSet(session.getChannel());
				if (session == mSessionCurrentChannel)
				{
					//AirtalkeeSessionManager.getInstance().SessionLock(session, true);
				}
			}
			BluetoothManager.getInstance().btStateRecoveryDelayed();
		}
		if (sessionListener != null)
		{
			sessionListener.onSessionEstablished(session, isOk);
		}
	}

	@Override
	public void onSessionEstablishing(AirSession session)
	{
		// TODO Auto-generated method stub
		if (sessionListener != null)
		{
			sessionListener.onSessionEstablishing(session);
		}
	}

	@Override
	public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk)
	{
		// TODO Auto-generated method stub
		if (sessionListener != null)
		{
			sessionListener.onSessionMemberUpdate(session, members, isOk);
		}
	}

	@Override
	public void onSessionOutgoingRinging(AirSession session)
	{
		// TODO Auto-generated method stub
		Sound.playSound(Sound.PLAYER_CALL_DIAL, true, AirServices.getInstance());
		if (sessionListener != null)
		{
			sessionListener.onSessionOutgoingRinging(session);
		}
	}

	@Override
	public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence)
	{
		// TODO Auto-generated method stub
		if (sessionListener != null)
		{
			sessionListener.onSessionPresence(session, membersAll, membersPresence);
		}
	}

	@Override
	public void onSessionReleased(AirSession session, int reason)
	{
		// TODO Auto-generated method stub
		if (reason != AirSession.SESSION_RELEASE_REASON_NETWORK_TERMINATE)
			sessionListRemove(session);
		if (session != null && session.getType() == AirSession.TYPE_DIALOG)
		{
			Sound.stopSound(Sound.PLAYER_CALL_DIAL);
			Sound.playSound(Sound.PLAYER_CALL_END, false, AirServices.getInstance());
			//AirtalkeeSessionManager.getInstance().SessionLock(session, false);
		}
		if (sessionListener != null)
		{
			sessionListener.onSessionReleased(session, reason);
		}
	}
	
	// ==============================
	// GROUP keep & attach JSON
	// ==============================

	private void channelJsonParse(String key, List<AirChannel> channels)
	{
		String string = AirServices.iOperator.getString(key);
		if (string != null)
		{
			try
			{
				JSONObject json = new JSONObject(string);
				JSONArray array = json.optJSONArray(JSON_GIDS);
				if (array != null && array.length() > 0)
				{
					for (int i = 0; i < array.length(); i ++)
					{
						AirChannel ch = AirtalkeeChannel.getInstance().ChannelGetByCode(array.getString(i));
						if (ch != null)
						{
							channels.add(ch);
						}
					}
				}
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void channelJsonBuild(String key, List<AirChannel> channels)
	{
		try
		{
			JSONObject json = new JSONObject();
			JSONArray array = new JSONArray();
			for (int i = 0; i < channels.size(); i ++)
			{
				array.put(i, channels.get(i).getId());
			}
			json.put(JSON_GIDS, array);
			AirServices.iOperator.putString(key, json.toString());
		}
		catch (Exception e)
		{
			// TODO: handle exception
			AirServices.iOperator.putString(key, "");
		}
	}
	
	public void channelAttachLoad()
	{
		List<AirChannel> channels = new ArrayList<AirChannel>();
		channelJsonParse(KEY_GROUP_ATTACH + AirtalkeeAccount.getInstance().getUserId(), channels);
		for (int i = 0; i < channels.size(); i ++)
		{
			AirChannel ch = AirtalkeeChannel.getInstance().ChannelGetByCode(channels.get(i).getId());
			if (ch != null)
				ch.setAttachItem(true);
		}
	}
	
	public void channelAttachSave()
	{
		List<AirChannel> channels = new ArrayList<AirChannel>();
		for (int i = 0; i < AirtalkeeChannel.getInstance().getChannels().size(); i ++)
		{
			if (AirtalkeeChannel.getInstance().getChannels().get(i).isAttachItem())
				channels.add(AirtalkeeChannel.getInstance().getChannels().get(i));
		}
		channelJsonBuild(KEY_GROUP_ATTACH + AirtalkeeAccount.getInstance().getUserId(), channels);
	}
	
	public boolean channelToggle()
	{
		boolean isToogle = currentSelectChannel != null && mSessionCurrentChannel != null && !currentSelectChannel.getId().equals(mSessionCurrentChannel.getSessionCode());
		if(isToogle)
		{
			SessionChannelOut(mSessionCurrentChannel.getSessionCode());
			SessionChannelIn(currentSelectChannel.getId());
		}
		return isToogle;
	}
	
	public void channelIndexSet(AirChannel ch)
	{
		if(ch != null)
		{
			TTSManager.getInstance().synth("进入，"+ch.getDisplayName());
			List<AirChannel> channels = ChannelController.dataChannelsGet();
			if(channels != null && channels.size() > 0)
			{
				for(int i = 0; i < channels.size(); i++)
				{
					if(channels.get(i).getId().equals(ch.getId()))
					{
						currentChannelIndex = i;
						currentSelectChannel = null;
						break;
					}
				}
			}
		}
	}
	
	public void channelSelect(boolean  plus)
	{
		List<AirChannel> channels = ChannelController.dataChannelsGet();
		
		if(channels != null && channels.size() > 0)
		{
			if(plus)
				currentChannelIndex ++;
			else
				currentChannelIndex --;
			
			if(currentChannelIndex < 0)
				currentChannelIndex = channels.size() -1;
			else if(currentChannelIndex >= channels.size())
				currentChannelIndex = 0;
			
			AirChannel channel  = null;
			for(int i = 0; i < channels.size(); i++)
			{
				if(currentChannelIndex == i)
					channel = channels.get(i);
			}
			if(channel != null)
			{
				//TTS 
				currentSelectChannel = channel;
				TTSManager.getInstance().synth(channel.getDisplayName());
			}
			else
			{
				//tip channel is null
				TTSManager.getInstance().synth("频道错误");
			}
		}
		else
		{
			//tip channel list is null
			TTSManager.getInstance().synth("无频道信息");
		}
	}
}

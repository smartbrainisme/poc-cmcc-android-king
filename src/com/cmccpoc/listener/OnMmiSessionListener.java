package com.cmccpoc.listener;

import java.util.List;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;

public interface OnMmiSessionListener
{
	public void onSessionOutgoingRinging(AirSession session);

	public void onSessionEstablishing(AirSession session);

	public void onSessionEstablished(AirSession session, boolean isOk);

	public void onSessionReleased(AirSession session, int reason);

	public void onSessionPresence(AirSession session, final List<AirContact> membersAll, final List<AirContact> membersPresence);

	public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk);

}

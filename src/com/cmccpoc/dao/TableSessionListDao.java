package com.cmccpoc.dao;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;

public class TableSessionListDao
{
	private static DBHelp dbHelp;
	private static final TableSessionListDao instance = new TableSessionListDao();

	private TableSessionListDao()
	{};

	protected static TableSessionListDao getInstance(DBHelp db)
	{
		dbHelp = db;
		return instance;
	}

	/********************************
	 * Session List
	 ********************************/

	protected List<AirSession> sessionLoad()
	{
		List<AirSession> sessions = new ArrayList<AirSession>();
		SQLiteDatabase db = dbHelp.DatabaseReadableGet();
		if (db != null)
		{
			String sql = "SELECT * FROM " + DBDefine.db_session + " WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid() + " GROUP BY " + DBDefine.t_session.sid + " ORDER BY "
				+ DBDefine.t_session.sOrder + " DESC";
			try
			{
				Cursor c = db.rawQuery(sql, null);
				while (c.moveToNext())
				{
					AirSession s = new AirSession();
					s.setSessionCode(c.getString(c.getColumnIndex(DBDefine.t_session.sid)));
					s.setDisplayName(c.getString(c.getColumnIndex(DBDefine.t_session.name)));
					s.setPhotoId(c.getString(c.getColumnIndex(DBDefine.t_session.photoId)));
					s.setMessageUnreadCount(c.getInt(c.getColumnIndex(DBDefine.t_session.unreadCount)));
					s.setSpecialNumber(c.getInt(c.getColumnIndex(DBDefine.t_session.specialNumber)));
					sessions.add(s);
				}
				c.close();
			}
			catch (Exception e)
			{
				Log.e(DBHelp.class, "[SQL EXCEPTION] " + sql + " -> " + e.getMessage());
			}
			dbHelp.DatabaseReadableClose(db);
		}
		return sessions;
	}

	protected void sessionClean()
	{
		String sql = null;
		sql = "DELETE FROM " + DBDefine.db_session + " WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid();
		dbHelp.del(sql);
		sql = "DELETE FROM " + DBDefine.db_session_member + " WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid();
		dbHelp.del(sql);
		sql = "DELETE FROM " + DBDefine.db_message + " WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid();
		dbHelp.del(sql);
	}

	protected void sessionCleanUnread(String sid)
	{
		String sql = String.format("UPDATE " + DBDefine.db_session + " SET " + DBDefine.t_session.unreadCount + " = 0 " + "WHERE " + DBDefine.t_session.UID + "=%s AND "
			+ DBDefine.t_session.sid + " = '%s'", dbHelp.getUid(), sid);
		dbHelp.update(sql);
	}

	protected void sessionAppend(final AirSession session)
	{
		if (session.getType() == AirSession.TYPE_DIALOG)
		{
			ContentValues cv = new ContentValues();
			cv.put(DBDefine.t_session.UID, dbHelp.getUid());
			cv.put(DBDefine.t_session.sid, session.getSessionCode());
			cv.put(DBDefine.t_session.name, session.getDisplayName());
			cv.put(DBDefine.t_session.photoId, session.getPhotoId());
			cv.put(DBDefine.t_session.unreadCount, session.getMessageUnreadCount());
			cv.put(DBDefine.t_session.sOrder, 0);
			cv.put(DBDefine.t_session.specialNumber, session.getSpecialNumber());
			dbHelp.insert(DBDefine.db_session, cv);

			sessionMemberSave(session.getSessionCode(), session.getMemberAll());
		}
	}

	protected void sessionDelete(String sid)
	{
		String sql = null;
		sql = "DELETE FROM " + DBDefine.db_session + " WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_session.sid + " = '" + sid + "'";
		dbHelp.del(sql);
		sql = "DELETE FROM " + DBDefine.db_session_member + " WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_session.sid + " = '" + sid + "'";
		dbHelp.del(sql);
		sql = "DELETE FROM " + DBDefine.db_message + " WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_session.sid + " = '" + sid + "'";
		dbHelp.del(sql);
	}

	protected void sessionUpdate(final String sid, final AirSession session)
	{
		String sql = null;
		sql = String.format("UPDATE " + DBDefine.db_session + " SET " + DBDefine.t_session.UID + " = %s, " + DBDefine.t_session.sid + " = '%s', " + DBDefine.t_session.name
			+ " = '%s', " + DBDefine.t_session.photoId + " = '%s', " + DBDefine.t_session.unreadCount + " = %d, " + DBDefine.t_session.specialNumber + " = %d " + "WHERE "
			+ DBDefine.t_session.sid + " = '%s'", dbHelp.getUid(), session.getSessionCode(), session.getDisplayName(), session.getPhotoId(), session.getMessageUnreadCount(),
			session.getSpecialNumber(), sid);
		dbHelp.update(sql);

		sessionMemberClean(sid);
		sessionMemberSave(session.getSessionCode(), session.getMemberAll());

		if (!TextUtils.equals(sid, session.getSessionCode()))
		{
			// Update all sid of messages for session
			sql = String.format("UPDATE " + DBDefine.db_message + " SET " + DBDefine.t_message.sid + " = '%s' WHERE " + DBDefine.t_message.UID
				+ "=%s AND " + DBDefine.t_message.sid + " = '%s'", session.getSessionCode(), dbHelp.getUid(), sid);
			dbHelp.update(sql);
		}
	}
	
	protected void sessionOrder(String sid)
	{
		String sql = "UPDATE " + DBDefine.db_session + " SET " + DBDefine.t_session.sOrder + " = (SELECT max(" + DBDefine.t_session.sOrder + ") FROM " + DBDefine.db_session
			+ " WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid() + ") + 1 " + "WHERE " + DBDefine.t_session.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_session.sid
			+ " = '" + sid + "'";
		dbHelp.update(sql);
	}

	/********************************
	 * Session member
	 ********************************/

	protected void sessionMemberLoad(AirSession session)
	{
		SQLiteDatabase db = dbHelp.DatabaseReadableGet();
		if (db != null)
		{
			String sql = "SELECT * FROM " + DBDefine.db_session_member + " WHERE " + DBDefine.t_session_member.UID + "=" + dbHelp.getUid() + " AND "
				+ DBDefine.t_session_member.sid + " ='" + session.getSessionCode() + "'";
			session.getMemberAll().clear();
			try
			{
				Cursor c = db.rawQuery(sql, null);
				while (c.moveToNext())
				{
					AirContact ct = new AirContact();
					ct.setIpocId(c.getString(c.getColumnIndex(DBDefine.t_session_member.iId)));
					ct.setDisplayName(c.getString(c.getColumnIndex(DBDefine.t_session_member.iName)));
					ct.setPhotoId(c.getString(c.getColumnIndex(DBDefine.t_session_member.iPhotoId)));
					session.getMemberAll().add(ct);
				}
				c.close();
			}
			catch (Exception e)
			{
				Log.e(DBHelp.class, "[SQL EXCEPTION] " + sql + " -> " + e.getMessage());
			}
			dbHelp.DatabaseReadableClose(db);
		}
	}

	protected void sessionMemberClean(String sid)
	{
		String sql = "DELETE FROM " + DBDefine.db_session_member + " WHERE " + DBDefine.t_session_member.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_session_member.sid
			+ " = '" + sid + "'";
		dbHelp.del(sql);
	}

	protected void sessionMemberSave(String sid, List<AirContact> contacts)
	{
		sessionMemberClean(sid);
		List<ContentValues> cvs = new ArrayList<ContentValues>();
		for (int i = 0; i < contacts.size(); i++)
		{
			ContentValues cv = sessionMemberAppendBuild(sid, contacts.get(i));
			cvs.add(cv);
		}
		dbHelp.insert(DBDefine.db_session_member, cvs);
	}

	protected void sessionMemberAppend(String sid, final AirContact contact)
	{
		ContentValues cv = sessionMemberAppendBuild(sid, contact);
		dbHelp.insert(DBDefine.db_session_member, cv);
	}

	protected ContentValues sessionMemberAppendBuild(String sid, final AirContact contact)
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefine.t_session_member.UID, dbHelp.getUid());
		cv.put(DBDefine.t_session_member.sid, sid);
		cv.put(DBDefine.t_session_member.iId, contact.getIpocId());
		cv.put(DBDefine.t_session_member.iName, contact.getDisplayName());
		cv.put(DBDefine.t_session_member.iPhotoId, contact.getPhotoId());
		return cv;
	}

	protected void sessionMemberDelete(String sid, String ipocid)
	{
		String sql = "DELETE FROM " + DBDefine.db_session_member + " WHERE " + DBDefine.t_session_member.sid + " = '" + sid + "' " + " AND " + DBDefine.t_session_member.iId
			+ " = '" + ipocid + "'";
		dbHelp.del(sql);
	}

}

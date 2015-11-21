package com.airtalkee.activity.home;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.AirMmiTimerListener;
import com.airtalkee.Util.Smilify;
import com.airtalkee.Util.Sound;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.ActivityImagePager;
import com.airtalkee.activity.MainActivity;
import com.airtalkee.activity.SessionBoxMessage;
import com.airtalkee.adapter.AdapterSessionMessage;
import com.airtalkee.control.AirMessageTransaction;
import com.airtalkee.listener.OnMmiMessageListener;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.OnMessageListListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.PicFactory;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.MacRecordingView;
import com.airtalkee.widget.PullToRefreshListView;
import com.airtalkee.widget.PullToRefreshListView.OnPullToRefreshListener;

public class IMFragment extends BaseFragment implements OnClickListener,
		OnMessageListListener, OnLongClickListener, TextWatcher,
		OnMmiMessageListener, OnPullToRefreshListener, OnItemClickListener,
		AirMmiTimerListener, OnTouchListener
{
	private static final int REQUEST_CODE_BROWSE_IMAGE = 111;

	private View textVoicePannel, textPannel, voicePannel;
	private ImageView btnVoice;
	private PullToRefreshListView lvMessage;
	private AdapterSessionMessage adapterMessage;
	private AirMessage currentMessage;
	private AirSession session;
	private EditText etMsg;
	private Button btnSend;
	private Animation animRefresh;
	private MacRecordingView mvRecording;
	private boolean recordCancel = false;
	private float startY = 0;
	public static String menuArray[];

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		v = inflater.inflate(getLayout(), container, false);
		lvMessage = (PullToRefreshListView) findViewById(R.id.lv_message);
		textVoicePannel = findViewById(R.id.voic_text_pannel);
		textPannel = findViewById(R.id.text_pannel);
		voicePannel = findViewById(R.id.voice_pannel);
		etMsg = (EditText) findViewById(R.id.et_msg);
		btnSend = (Button) findViewById(R.id.send);
		btnVoice = (ImageView) findViewById(R.id.btn_voice);

		btnVoice.setOnTouchListener(this);
		btnSend.setOnClickListener(this);
		findViewById(R.id.btn_text_close).setOnClickListener(this);
		findViewById(R.id.btn_voice_close).setOnClickListener(this);
		etMsg.addTextChangedListener(this);
		lvMessage.setAdapter(adapterMessage = new AdapterSessionMessage(getActivity(), this, this));
		lvMessage.setOnRefreshListener(this);
		lvMessage.setOnItemClickListener(this);

		animRefresh = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh);

		mvRecording = (MacRecordingView) findViewById(R.id.mac_talking);
		mvRecording.initChild();

		return v;
	}

	@Override
	public int getLayout()
	{
		// TODO Auto-generated method stub
		return R.layout.frag_im_layout;
	}

	@Override
	public void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();

		setVoicePannelVisiblity(View.GONE);
		setTextPannelVisiblity(View.GONE);
	}

	@Override
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		setSession(getSession());
		AirMessageTransaction.getInstance().setOnMessageListener(this);
	}

	@Override
	public void dispatchBarClickEvent(int page, int id)
	{
		// TODO Auto-generated method stub
		if (page == HomeActivity.PAGE_IM)
		{
			switch (id)
			{
				case R.id.bar_left:
					setVoicePannelVisiblity(View.VISIBLE);
					break;
				case R.id.bar_mid:
					String status = Environment.getExternalStorageState();
					if (!status.equals(Environment.MEDIA_MOUNTED))
					{
						Util.Toast(getActivity(), getActivity().getString(R.string.insert_sd_card));
						return;
					}
					Intent localIntent = new Intent("android.intent.action.GET_CONTENT", null);
					localIntent.setType("image/*");
					startActivityForResult(localIntent, REQUEST_CODE_BROWSE_IMAGE);
					break;
				case R.id.bar_right:
					setTextPannelVisiblity(View.VISIBLE);
					break;
			}
		}
	}

	@Override
	public boolean onLongClick(View v)
	{
		// TODO Auto-generated method stub

		if (v.getId() == R.id.body_content)
		{
			if (v.getTag() != null)
			{
				currentMessage = (AirMessage) v.getTag();
				if (currentMessage != null)
				{
					if (currentMessage.getType() == AirMessage.TYPE_CUSTOM_RELATION)
					{
						return false;
					}
					boolean isPic = (currentMessage.getType() == AirMessage.TYPE_PICTURE || currentMessage.getType() == AirMessage.TYPE_RECORD) ? true : false;
					menuArray = getResources().getStringArray(!isPic ? R.array.handle_message_txt : R.array.handle_message_txt1);
					int dialogId = R.id.talk_dialog_message_txt;
					if (currentMessage.getIpocidFrom().equals(AccountController.getUserInfo().getIpocId()))
					{
						dialogId = R.id.talk_dialog_message_txt_send_fail;
						menuArray = getResources().getStringArray(!isPic ? R.array.handle_message_send_fail : R.array.handle_message_send_fail1);
					}
					getActivity().removeDialog(dialogId);
					getActivity().showDialog(dialogId);
				}
			}
		}

		return false;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.btn_text_close:
				setTextPannelVisiblity(View.GONE);
				etMsg.setText("");
				break;
			case R.id.btn_voice_close:
				setVoicePannelVisiblity(View.GONE);
				break;
			case R.id.send:
				messageSend();
				break;
			case R.id.body_content:
			{
				if (session != null && v.getTag() != null)
				{
					currentMessage = (AirMessage) v.getTag();
					if (currentMessage != null)
					{
						if (currentMessage.getType() == AirMessage.TYPE_RECORD)
						{
							messageRecordPlay(v);
						}
						else if (currentMessage.getType() == AirMessage.TYPE_PICTURE)
						{
							if (getActivity() != null)
							{
								try
								{
									Intent intent = new Intent(getActivity(), ActivityImagePager.class);
									String[] position = new String[] { currentMessage.getImageUri() };
									ArrayList<String> images = adapterMessage.getPicUrls(position);
									Bundle b = new Bundle();
									b.putStringArrayList("images", images);
									b.putInt("position", Integer.parseInt(position[0]));
									intent.putExtras(b);
									startActivity(intent);
								}
								catch (Exception e)
								{
									// TODO: handle exception
								}
							}
						}
					}
				}
				break;
			}
		}
	}

	private void messageRecordPlay(View view)
	{
		if (session != null)
		{
			view.findViewById(R.id.record_layout).setTag(currentMessage.getMessageCode());
			if (!currentMessage.isRecordPlaying() && currentMessage.getImageUri() != null)
			{
				if (currentMessage.getRecordType() == AirMessage.RECORD_TYPE_PTT)
				{
					if (AirtalkeeMessage.getInstance().MessageRecordPlayStartLocal(currentMessage) == false)
					{
						Util.Toast(getActivity(), getString(R.string.talk_msg_no_local_file), R.drawable.ic_error);
					}
				}
				else
				{
					AirtalkeeMessage.getInstance().MessageRecordPlayStart(session, currentMessage);
				}
			}
			else
			{
				if (currentMessage.getImageUri() != null)
				{
					AirtalkeeMessage.getInstance().MessageRecordPlayStop();
				}
				else
				{
					if (currentMessage.getRecordType() == AirMessage.RECORD_TYPE_PTT)
					{
						if (AirtalkeeMessage.getInstance().MessageRecordPlayStartLocal(currentMessage) == false)
						{
							Util.Toast(getActivity(), getString(R.string.talk_msg_no_local_file), R.drawable.ic_error);
						}
					}
					else
					{
						AirtalkeeMessage.getInstance().MessageRecordPlayStart(session, currentMessage);
					}
				}
			}
		}
	}

	protected void setTextPannelVisiblity(int visiblility)
	{
		if (visiblility == View.GONE)
		{
			if (textVoicePannel != null)
				textVoicePannel.setVisibility(View.GONE);
			if (textPannel != null)
				textPannel.setVisibility(View.GONE);
			if (mediaStatusBar != null)
				mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);
		}
		else
		{

			if (textVoicePannel != null)
				textVoicePannel.setVisibility(View.VISIBLE);
			if (textPannel != null)
				textPannel.setVisibility(View.VISIBLE);
			if (mediaStatusBar != null)
				mediaStatusBar.setMediaStatusBarVisibility(View.GONE);

			if (etMsg != null)
			{
				etMsg.requestFocus();
				etMsg.performClick();
			}
		}
	}

	private void setVoicePannelVisiblity(int visiblility)
	{
		if (visiblility == View.GONE)
		{
			if (textVoicePannel != null)
				textVoicePannel.setVisibility(View.GONE);
			if (voicePannel != null)
				voicePannel.setVisibility(View.GONE);
			if (mediaStatusBar != null)
				mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);
		}
		else
		{
			if (textVoicePannel != null)
				textVoicePannel.setVisibility(View.VISIBLE);
			if (voicePannel != null)
				voicePannel.setVisibility(View.VISIBLE);
			if (mediaStatusBar != null)
				mediaStatusBar.setMediaStatusBarVisibility(View.GONE);
		}
	}

	public void setSession(AirSession s)
	{

		if ((s != null && session != null && !s.getSessionCode().equals(session.getSessionCode())) || (session == null && s != null))
		{
			if (lvMessage == null)
				return;
			lvMessage.setHaveMore(s.isMessageMore());
			mHandler.sendEmptyMessageDelayed(1, 100);
		}
		this.session = s;
		Log.d(SessionBoxMessage.class, "SessionBoxMessage - setSession");
		if (s != null && s.getMessageTextDraft() != null)
		{
			Spannable spannable = Util.buildPlainMessageSpannable(getActivity(), s.getMessageTextDraft().getBytes());
			etMsg.setText(spannable);
		}
		adapterMessage.setSession(s);

	}

	public void refreshMessages()
	{
		adapterMessage.notifyDataSetChanged();
	}

	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what)
			{
				case 1:
				{
					AirtalkeeMessage.getInstance().MessageListMoreLoad(session, IMFragment.this);
					break;
				}
			}

		}
	};

	private void messageSend()
	{
		if (session != null)
		{
			String msg = etMsg.getText().toString();
			if (msg != null && !msg.trim().equals(""))
			{
				AirtalkeeMessage.getInstance().MessageSend(session, Smilify.smilifFilter(msg), false, true);
				etMsg.setText("");
				adapterMessage.notifyDataSetChanged();
			}
		}
		else
		{
			Util.Toast(getActivity(), getString(R.string.talk_channel_idle));
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		// TODO Auto-generated method stub
		btnSend.setEnabled(!TextUtils.isEmpty(etMsg.getText().toString().trim()));
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPullToRefresh(int firstVisibleItem, int visibleCount)
	{
		// TODO Auto-generated method stub
		if (session != null && !Utils.isEmpty(session.getSessionCode()))
		{
			if (session.isMessageMore())
			{
				AirtalkeeMessage.getInstance().MessageListMoreLoad(session, this);
			}
			else
			{
				lvMessage.onRefreshComplete();
			}
		}
	}

	public void onListItemLongClick(int id, int selectedItem)
	{
		if (currentMessage == null)
			return;
		switch (id)
		{
			case R.id.talk_dialog_message_txt:
			{
				switch (selectedItem)
				{
					case 0:
					{
						if (session != null)
						{
							// showRemoveAllDialog(session.getSessionCode());
						}
						break;
					}
					case 1:
					{
						if (session != null)
						{
							AirtalkeeMessage.getInstance().MessageRemove(session.getSessionCode(), currentMessage);
							adapterMessage.notifyDataSetChanged();
						}
						break;
					}
					case 2:
					{
						Util.textClip(getActivity(), currentMessage.getBody());
						break;
					}
				}
				break;
			}
			case R.id.talk_dialog_message_txt_send_fail:
			{
				switch (selectedItem)
				{
					case 0:
					{
						if (session != null)
						{
							// showRemoveAllDialog(session.getSessionCode());
						}
						break;
					}
					case 1:
					{
						try
						{
							if (session != null)
							{
								AirtalkeeMessage.getInstance().MessageRemove(session.getSessionCode(), currentMessage);
								adapterMessage.notifyDataSetChanged();
							}
						}
						catch (Exception e)
						{}
						break;
					}
					case 2:
					{
						try
						{
							if (session != null)
							{
								if (currentMessage.getType() == AirMessage.TYPE_PICTURE)
								{
									AirtalkeeMessage.getInstance().MessageImageSend(session, currentMessage.getImage(), true);
								}
								else if (currentMessage.getType() == AirMessage.TYPE_RECORD)
								{
									AirtalkeeMessage.getInstance().MessageRecordResend(session, currentMessage.getMessageCode(), currentMessage.getImageUri(), currentMessage.getImageLength(), true);
								}
								else
								{
									AirtalkeeMessage.getInstance().MessageSend(session, Smilify.smilifFilter(currentMessage.getBody()), false, true);
								}
								adapterMessage.notifyDataSetChanged();
							}

						}
						catch (Exception e)
						{}
						break;
					}
					case 3:
					{
						Util.textClip(getActivity(), currentMessage.getBody());
						break;
					}
				}
			}
		}
	}

	/************************************
     * 
     * 
     * 
     ************************************/
	@Override
	public void onMessageIncomingRecv(List<AirMessage> messageList)
	{
		// TODO Auto-generated method stub
		if (messageList != null)
		{
			boolean hasNew = false;
			for (int i = 0; i < messageList.size(); i++)
			{
				AirMessage message = messageList.get(i);
				if (session != null && TextUtils.equals(message.getSessionCode(), session.getSessionCode()))
				{
					hasNew = true;
					break;
				}
			}
			if (hasNew)
			{
				adapterMessage.notifyDataSetChanged();
				// refreshMessageNewCount(false);
			}
		}
	}

	@Override
	public boolean onMessageIncomingRecv(boolean isCustom, AirMessage message)
	{
		boolean isHandled = false;
		if (!isCustom && message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
		{
			boolean toClean = false;
			// if (sessionBox.tabIndex() == SessionBox.PAGE_MSG)
			// {
			// toClean = true;
			// }
			adapterMessage.notifyDataSetChanged();
			// refreshMessageNewCount(toClean);
			isHandled = true;
		}
		return isHandled;
	}

	@Override
	public void onMessageOutgoingSent(boolean isCustom, AirMessage message, boolean isSent)
	{
		// TODO Auto-generated method stub
		if (!isCustom && message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
		{
			if (isSent)
			{
				Sound.vibrate(20, getActivity());
				Sound.playSound(Sound.PLAYER_MSG_SENT, getActivity());
			}
			adapterMessage.notifyDataSetChanged();
			if (adapterMessage.getCount() > 0)
				lvMessage.setSelection(adapterMessage.getCount() - 1);
		}
	}

	@Override
	public void onMessageUpdated(AirMessage message)
	{
		// TODO Auto-generated method stub
		if (message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
		{
			adapterMessage.notifyDataSetChanged();
		}
	}

	@Override
	public void onMessageRecordPlayLoaded(boolean isOk, String msgCode, String resId)
	{
		View view = lvMessage.findViewWithTag(msgCode);
		if (view != null)
		{
			View pro = view.findViewById(R.id.loading);
			if (pro != null)
			{
				pro.clearAnimation();
				pro.setAnimation(null);
				pro.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onMessageRecordPlayLoading(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		View view = lvMessage.findViewWithTag(msgCode);
		if (view != null)
		{
			View record = view.findViewById(R.id.record_pic);
			View pro = view.findViewById(R.id.loading);
			if (record != null)
			{
				pro.clearAnimation();
				record.setVisibility(View.INVISIBLE);
			}

			if (pro != null)
			{
				pro.setVisibility(View.VISIBLE);
				pro.setAnimation(animRefresh);
			}
		}
	}

	@Override
	public void onMessageRecordPlayStart(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (currentMessage != null)
		{
			currentMessage.setState(AirMessage.STATE_NONE);
			View view = lvMessage.findViewWithTag(msgCode);
			View unRead = lvMessage.findViewWithTag(msgCode + "unRead");
			if (unRead != null)
				unRead.setVisibility(View.GONE);
			if (view != null)
			{
				View pro = view.findViewById(R.id.loading);
				if (pro != null)
				{
					pro.clearAnimation();
					pro.setAnimation(null);
					pro.setVisibility(View.GONE);
				}
				ImageView record = (ImageView) view.findViewById(R.id.record_pic);
				if (record != null)
				{
					record.setVisibility(View.VISIBLE);
					// record.setSelected(true);
					record.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_stop, getActivity()));
				}
				currentMessage.setRecordTimer(currentMessage.getImageLength());
				// AirMmiTimer.getInstance().TimerRegister(this, this, false,
				// true, 1000, true, null);
			}
			sessionSp.edit().putInt(SESSION_EVENT_KEY, sessionSp.getInt(SESSION_EVENT_KEY, 1) + 1).commit();
		}
	}

	@Override
	public void onMessageRecordPlayStop(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		Log.i(SessionBoxMessage.class, "onMessageRecordPlayStop");
		// AirMmiTimer.getInstance().TimerUnregister(this, this);
		// if (SetRecordPlayState(msgCode, false))
		{
			Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_STOP, false, getActivity());
			View view = lvMessage.findViewWithTag(msgCode);
			if (view != null)
			{
				View pro = view.findViewById(R.id.loading);
				pro.clearAnimation();
				pro.setAnimation(null);
				pro.setVisibility(View.GONE);

				ImageView record = (ImageView) view.findViewById(R.id.record_pic);
				if (record != null)
				{
					record.setVisibility(View.VISIBLE);
					record.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, getActivity()));
				}
				TextView text = (TextView) view.findViewById(R.id.record_time);
				if (text != null)
				{
					AirMessage msg = adapterMessage.getMessageByCode(msgCode);
					if (msg != null)
						text.setText(msg.getImageLength() + "''");
				}
			}
			sessionSp.edit().putInt(SESSION_EVENT_KEY, sessionSp.getInt(SESSION_EVENT_KEY, 1) + 1).commit();
		}
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		// TODO Auto-generated method stub
		if (currentMessage != null)
		{
			currentMessage.minusRecordTimer();
			View v = lvMessage.findViewWithTag(currentMessage.getMessageCode());
			if (v != null)
			{
				TextView text = (TextView) v.findViewById(R.id.record_time);
				if (text != null)
				{
					text.setText(currentMessage.getRecordTimer() + "''");
				}
			}
			if (session.getMessagePlayback() != null && TextUtils.equals(session.getMessagePlayback().getImageUri(), currentMessage.getImageUri()))
			{
				// recPlaybackSeconds.setText(currentMessage.getRecordTimer() +
				// "''");
			}

		}
	}

	@Override
	public void onMessageRecordPtt(AirSession session, AirMessage message, String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
		{
			boolean toClean = false;
			// if (sessionBox.tabIndex() == SessionBox.PAGE_MSG)
			{
				toClean = true;
			}
			// refreshMessageNewCount(toClean);
			// refreshPlayback();
			adapterMessage.notifyDataSetChanged();
		}
	}

	@Override
	public void onMessageRecordStart()
	{
		// TODO Auto-generated method stub
		Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_START, false, getActivity());
		mvRecording.registerMessage(MacRecordingView.START_TIME, null);
	}

	@Override
	public void onMessageRecordStop(int seconds, String msgCode)
	{
		// TODO Auto-generated method stub
		mvRecording.registerMessage(MacRecordingView.STOP_TIME, recordCancel);
		switch (seconds)
		{
			case AirtalkeeMessage.REC_RESULT_OK:
				adapterMessage.notifyDataSetChanged();
				break;
			case AirtalkeeMessage.REC_RESULT_ERR_SMALL:
				Util.Toast(getActivity(), getString(R.string.talk_rec_result_err_small));
				break;
			case AirtalkeeMessage.REC_RESULT_ERROR:
				Util.Toast(getActivity(), getString(R.string.talk_rec_result_error));
				break;
			case AirtalkeeMessage.REC_RESULT_CANCEL:
				Util.Toast(getActivity(), getString(R.string.talk_rec_result_cancel_str));
				break;
		}
		adapterMessage.notifyDataSetChanged();
	}

	@Override
	public void onMessageRecordTransfered(String msgCode, String resId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageListLoad(String sessionCode, List<AirMessage> messages)
	{
		// TODO Auto-generated method stub
		int position = 10;
		if (messages != null)
		{
			position = messages.size();
		}
		lvMessage.onRefreshComplete();
		lvMessage.setHaveMore(session.isMessageMore());
		lvMessage.setSelectionFromTop(position, 0);
		adapterMessage.notifyDataSetInvalidated();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btn_voice)
		{
			if (session != null)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					recordCancel = false;
					startY = event.getY();
					Sound.vibrate(20, getActivity());
					btnVoice.setImageResource(R.drawable.ic_voice_talk);
					if (session.getType() == AirSession.TYPE_DIALOG)
						AirtalkeeMessage.getInstance().MessageRecordStart(session, true);
					else
						AirtalkeeMessage.getInstance().MessageRecordStart(session.getSessionCode(), true);
				}
				else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
				{
					btnVoice.setImageResource(R.drawable.ic_voice_idle);
					mvRecording.registerMessage(MacRecordingView.STOP_TIME, recordCancel);
					AirtalkeeMessage.getInstance().MessageRecordStop(recordCancel);
				}
				else if (event.getAction() == MotionEvent.ACTION_MOVE)
				{
					// Log.e(SessionDialogActivity.class, "Y="+event.getY()
					// +"  Height="+v.getHeight()+"startY="+startY);
					if (Math.abs(event.getY() - v.getHeight()) > v.getHeight() + 100)
					{
						if (!recordCancel)
						{
							mvRecording.registerMessage(MacRecordingView.RECORD_CANCEL, null);
							v.setPressed(false);
							recordCancel = true;
						}
					}
					else if (event.getY() >= startY - 10)
					{
						if (recordCancel)
						{
							mvRecording.registerMessage(MacRecordingView.RECORD_OK, null);
							recordCancel = false;
						}
					}
					if (event.getY() >= startY - 10)
					{
						v.setPressed(true);
					}
				}
			}
			else
			{
				Util.Toast(getActivity(), getString(R.string.talk_channel_idle));
			}
		}

		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case REQUEST_CODE_BROWSE_IMAGE:
				if (resultCode == MainActivity.RESULT_OK)
				{
					try
					{
						System.gc();
						Uri originalUri = data.getData();
						String[] proj = { MediaStore.Images.Media.DATA };
						String path = null;
						@SuppressWarnings("deprecation")
						Cursor cursor = getActivity().managedQuery(originalUri, proj, null, null, null);
						if (cursor != null)
						{
							int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
							cursor.moveToFirst();
							path = cursor.getString(column_index);
						}
						else
						{
							path = originalUri.getPath();
						}
						Bitmap tempBitmap = null;
						try
						{
							byte[] bitmapData = AirServices.iOperator.readByteFile("", path, true);
							tempBitmap = PicFactory.getNormalMaxImage(bitmapData);
						}
						catch (OutOfMemoryError e)
						{
							// TODO: handle exception
							return;
						}
						byte bphoto[] = null;
						ByteArrayOutputStream streamOut = new ByteArrayOutputStream();

						tempBitmap.compress(CompressFormat.JPEG, 80, streamOut);
						tempBitmap.recycle();
						tempBitmap = null;
						bphoto = streamOut.toByteArray();
						streamOut.reset();
						streamOut.close();
						streamOut = null;
						AirtalkeeMessage.getInstance().MessageImageSend(session, bphoto, true);
						bphoto = null;
						System.gc();
					}
					catch (Exception e)
					{
						// Log.e(ChannelActivity.class,
						// String.format("Exception  Bitmap Error [%s]",
						// e.toString()));
					}
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		// TODO Auto-generated method stub

	}

}

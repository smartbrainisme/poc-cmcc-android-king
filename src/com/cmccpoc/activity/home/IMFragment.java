package com.cmccpoc.activity.home;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.OnMessageListListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.PicFactory;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.Util.AirMmiTimerListener;
import com.cmccpoc.Util.Sound;
import com.cmccpoc.Util.ThemeUtil;
import com.cmccpoc.Util.Util;
import com.cmccpoc.activity.ActivityImagePager;
import com.cmccpoc.activity.AlbumChooseActivity;
import com.cmccpoc.activity.home.adapter.AdapterSessionMessage;
import com.cmccpoc.activity.home.widget.SessionAndChannelView;
import com.cmccpoc.control.AirMessageTransaction;
import com.cmccpoc.listener.OnMmiMessageListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.widget.MacRecordingView;
import com.cmccpoc.widget.PullToRefreshListView;
import com.cmccpoc.widget.PullToRefreshListView.OnPullToRefreshListener;

/**
 * 三大Fragment之一：IM消息Fragment，主要显示IM消息，可以发送语音、文字已经图片等消息
 * @author Yao
 */
public class IMFragment extends BaseFragment implements OnClickListener,
		OnMessageListListener, OnLongClickListener, TextWatcher,
		OnMmiMessageListener, OnPullToRefreshListener, OnItemClickListener,
		AirMmiTimerListener, OnTouchListener
{
	private static final int REQUEST_CODE_BROWSE_IMAGE = 111;

	public View textVoicePannel, textPannel, voicePannel, toolsPannel;
	private ImageView btnVoice, btnClose, btnImage, btnCamera, btnVideo;
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
	private static IMFragment mInstance;

	public static IMFragment getInstance()
	{
		return mInstance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mInstance = this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		v = inflater.inflate(getLayout(), container, false);

		textVoicePannel = findViewById(R.id.voic_text_pannel);
		textPannel = findViewById(R.id.text_pannel);
		voicePannel = findViewById(R.id.voice_pannel);
		toolsPannel = findViewById(R.id.tools_pannel);
		etMsg = (EditText) findViewById(R.id.et_msg);
		etMsg.addTextChangedListener(this);
		btnSend = (Button) findViewById(R.id.send);
		btnSend.setOnClickListener(this);
		btnVoice = (ImageView) findViewById(R.id.btn_voice);
		btnVoice.setOnTouchListener(this);
		btnClose = (ImageView) findViewById(R.id.tools_btn_close);
		btnClose.setOnClickListener(this);
		btnImage = (ImageView) findViewById(R.id.tools_btn_image);
		btnImage.setOnClickListener(this);
		btnCamera = (ImageView) findViewById(R.id.tools_btn_camera);
		btnCamera.setOnClickListener(this);
		btnVideo = (ImageView) findViewById(R.id.tools_btn_video);
		btnVideo.setOnClickListener(this);
		findViewById(R.id.btn_text_close).setOnClickListener(this);
		findViewById(R.id.btn_voice_close).setOnClickListener(this);

		adapterMessage = new AdapterSessionMessage(getActivity(), this, this);
		adapterMessage.notifyDataSetChanged();
		lvMessage = (PullToRefreshListView) findViewById(R.id.lv_message);
		lvMessage.setAdapter(adapterMessage);
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
		super.onPause();
		if (BaseActivity.getInstance().pageIndex == BaseActivity.PAGE_IM)
		{
			setVoicePannelVisiblity(View.GONE);
			setTextPannelVisiblity(View.GONE);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		setSession(getSession());
		AirMessageTransaction.getInstance().setOnMessageListener(this);
		lvMessage.setSelection(adapterMessage.getCount());
	}

	@Override
	public void dispatchBarClickEvent(int page, int id)
	{
		if (page == HomeActivity.PAGE_IM)
		{
			switch (id)
			{
				case R.id.bar_left:
					setVoicePannelVisiblity(View.VISIBLE);
					break;
				case R.id.bar_mid:
					setToolsPannelVisiblity(View.VISIBLE);
					break;
				case R.id.bar_right:
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm != null)
					{
						imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					}
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
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null)
				{
					imm.hideSoftInputFromWindow(mediaStatusBar.getBottomBarParent().getWindowToken(), 0);
				}
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
			case R.id.tools_btn_close:
				setToolsPannelVisiblity(View.GONE);
				break;
			case R.id.tools_btn_image:
			{
				String status = Environment.getExternalStorageState();
				if (!status.equals(Environment.MEDIA_MOUNTED))
				{
					Util.Toast(getActivity(), getActivity().getString(R.string.insert_sd_card));
					return;
				} 
				// 系统相册 
				// Intent localIntent = new Intent("android.intent.action.GET_CONTENT", null); 
				// localIntent.setType("image/*"); 
				// 自定义相册 
				Intent localIntent = new Intent(getActivity(), AlbumChooseActivity.class);
				localIntent.putExtra("type", AlbumChooseActivity.TYPE_IM);
				startActivityForResult(localIntent, REQUEST_CODE_BROWSE_IMAGE);
				break;
			}
			case R.id.tools_btn_camera:
			{
				
				break;
			}
			case R.id.tools_btn_video:
			{
				
				break;
			}
		}
	}

	/**
	 * 录音消息播放
	 * @param view 当前录音消息所在view
	 */
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

	/**
	 * 设置底部发送文字消息区域是否可见
	 * @param visiblility 是否可见
	 */
	protected void setTextPannelVisiblity(int visiblility)
	{
		if (visiblility == View.GONE)
		{
			if (textVoicePannel != null)
				textVoicePannel.setVisibility(View.GONE);
			if (textPannel != null)
				textPannel.setVisibility(View.GONE);
			if (toolsPannel != null)
				toolsPannel.setVisibility(View.GONE);
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
			etMsg.setFocusable(true);
			etMsg.setFocusableInTouchMode(true);
			etMsg.requestFocus();
		}
	}

	/**
	 * 设置底部发送录音消息区域是否可见
	 * @param visiblility 是否可见
	 */
	private void setVoicePannelVisiblity(int visiblility)
	{
		if (visiblility == View.GONE)
		{
			if (textVoicePannel != null)
				textVoicePannel.setVisibility(View.GONE);
			if (voicePannel != null)
				voicePannel.setVisibility(View.GONE);
			if (toolsPannel != null)
				toolsPannel.setVisibility(View.GONE);
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

	/**
	 * 设置底部发送其他类型消息区域是否可见
	 * @param visiblility 是否可见
	 */
	private void setToolsPannelVisiblity(int visiblility)
	{
		if (visiblility == View.GONE)
		{
			if (textVoicePannel != null)
				textVoicePannel.setVisibility(View.GONE);
			if (voicePannel != null)
				voicePannel.setVisibility(View.GONE);
			if (toolsPannel != null)
				toolsPannel.setVisibility(View.GONE);
			if (mediaStatusBar != null)
				mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);
		}
		else
		{
			if (textVoicePannel != null)
				textVoicePannel.setVisibility(View.VISIBLE);
			if (toolsPannel != null)
				toolsPannel.setVisibility(View.VISIBLE);
			if (mediaStatusBar != null)
				mediaStatusBar.setMediaStatusBarVisibility(View.GONE);
		}
	}

	/**
	 * 设置Session会话
	 * @param s 会话Entity
	 */
	public void setSession(AirSession s)
	{
		if ((s != null && session != null && !s.getSessionCode().equals(session.getSessionCode())) || (session == null && s != null))
		{
			if (lvMessage == null)
				return;
			refreshMessages();
			lvMessage.setHaveMore(s.isMessageMore());
			try
			{
				mHandler.sendEmptyMessageDelayed(1, 10);
			}
			catch (Exception e)
			{}
		}
		this.session = s;
		if (s != null && s.getMessageTextDraft() != null)
		{
			etMsg.setText(s.getMessageTextDraft());
		}
		adapterMessage.setSession(s);
	}

	/**
	 * 刷新消息列表
	 */
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

	/**
	 * 发送消息
	 */
	private void messageSend()
	{
		if (session != null)
		{
			String msg = etMsg.getText().toString();
			if (msg != null && !msg.trim().equals(""))
			{
				AirtalkeeMessage.getInstance().MessageSend(session, msg, false, true);
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

	@Override
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
							AirtalkeeMessage.getInstance().MessageRemoveAll(session.getSessionCode());
							adapterMessage.notifyDataSetChanged();
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
							AirtalkeeMessage.getInstance().MessageRemoveAll(session.getSessionCode());
							adapterMessage.notifyDataSetChanged();
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
									AirtalkeeMessage.getInstance().MessageSend(session, currentMessage.getBody(), false, true);
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
				HomeActivity.getInstance().checkNewIM(false);
				SessionAndChannelView.getInstance().refreshChannelAndDialog();
				getStatusBarTitle().refreshNewMsg();
			}
		}
	}

	@Override
	public boolean onMessageIncomingRecv(boolean isCustom, AirMessage message)
	{
		boolean isHandled = false;
		boolean toClean = false;
		if (HomeActivity.getInstance().pageIndex == HomeActivity.getInstance().PAGE_IM)
		{
			toClean = true;
			if (!isCustom && message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
			{
				adapterMessage.notifyDataSetChanged();
				HomeActivity.getInstance().checkNewIM(toClean);
				SessionAndChannelView.getInstance().refreshChannelAndDialog();
				isHandled = true;
			}
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
			ImageView record = (ImageView) view.findViewById(R.id.record_pic);
			if (record != null)
			{
				record.setVisibility(View.VISIBLE);
				// record.setSelected(true);
				record.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, getActivity()));
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
			if (session.getMessagePlayback() != null && TextUtils.equals(session.getMessagePlayback().getImageUri(), resId))
			{
				PTTFragment.getInstance().refreshPlayback();
			}
		}
	}

	@Override
	public void onMessageRecordPlayStop(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
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
			PTTFragment.getInstance().refreshPlayback();
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
			// if (session.getMessagePlayback() != null &&
			// TextUtils.equals(session.getMessagePlayback().getImageUri(),
			// currentMessage.getImageUri()))
			// {
			// recPlaybackSeconds.setText(currentMessage.getRecordTimer()
			// +"''");
			// }

		}
	}

	@Override
	public void onMessageRecordPtt(AirSession session, AirMessage message, String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
		{
			boolean toClean = false;
			if (HomeActivity.getInstance().pageIndex == HomeActivity.getInstance().PAGE_IM)
			{
				toClean = true;
			}
			HomeActivity.getInstance().checkNewIM(toClean);
			SessionAndChannelView.getInstance().refreshChannelAndDialog();
			// refreshMessageNewCount(toClean);
			PTTFragment.getInstance().refreshPlayback();
			adapterMessage.notifyDataSetChanged();
		}
	}

	@Override
	public void onMessageRecordStart()
	{
		Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_START, false, getActivity());
		mvRecording.registerMessage(MacRecordingView.START_TIME, null);
	}

	@Override
	public void onMessageRecordStop(int seconds, String msgCode)
	{
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

	}

	@Override
	public void onMessageListLoad(String sessionCode, List<AirMessage> messages)
	{
		int position = 10;
		adapterMessage.notifyDataSetChanged();
		adapterMessage.notifyDataSetInvalidated();
		if (messages != null)
		{
			position = messages.size();
		}
		lvMessage.onRefreshComplete();
		lvMessage.setHaveMore(session.isMessageMore());
		lvMessage.setSelectionFromTop(position, 0);
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

	/**
	 * 主要处理发送图片消息
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case REQUEST_CODE_BROWSE_IMAGE:
				if (resultCode == HomeActivity.RESULT_OK)
				{
					try
					{
						System.gc();
						Bundle bundleData = data.getExtras();
						List<String> pathList = bundleData.getStringArrayList("picPath");
						if (pathList != null && pathList.size() > 0)
						{
							for (int i = 0; i < pathList.size(); i++)
							{
								String path = pathList.get(i);
								Bitmap tempBitmap = null;
								try
								{
									byte[] bitmapData = AirServices.iOperator.readByteFile("", path, true);
									tempBitmap = PicFactory.getNormalMaxImage(bitmapData);
								}
								catch (OutOfMemoryError e)
								{
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
						}
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

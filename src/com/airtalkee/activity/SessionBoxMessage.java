package com.airtalkee.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.AirMmiTimerListener;
import com.airtalkee.Util.Sound;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterSessionMessage;
import com.airtalkee.adapter.AdapterTools;
import com.airtalkee.listener.OnMmiMessageListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.OnMessageListListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.PicFactory;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.MacRecordingView;
import com.airtalkee.widget.PullToRefreshListView;
import com.airtalkee.widget.PullToRefreshListView.OnPullToRefreshListener;

public class SessionBoxMessage extends View implements OnClickListener, OnTouchListener, TextWatcher, OnItemClickListener, OnLongClickListener, OnPullToRefreshListener,
	OnMmiMessageListener, OnMessageListListener, AirMmiTimerListener/*, MyRelativeLayout.OnResizeListener*/
{

	private static final int REQUEST_CODE_BROWSE_IMAGE = 111;
	private static final int REQUEST_CODE_CREATE_IMAGE = 112;

	private SessionBox sessionBox = null;
	private AirSession session;
	private Activity contextMain = null;

//	private MyRelativeLayout talkWindow;
	private TextView tvMessageNewCount;
	private Animation animRefresh;
	private PullToRefreshListView lvMessage;
	private AdapterSessionMessage adapterMessage;
	private AirMessage currentMessage;
	private EditText etMsg;
	private ImageView ivBtnRecord;
	private ImageView ivSend;
	private MacRecordingView mvRecording;
	private boolean recordCancel = false;
	private LinearLayout lLayoutEmotion;
	private float startY = 0;
	private Animation animBotomIn;
	private GridView gvSmiley, gvTools;
	private AdapterTools adapterTools;
	private PopupWindow pwTools = null, pwImage = null;
	private LinearLayout talkPannel;
	public String menuArray[];

	private LinearLayout recPlayback;
	private ImageView recPlaybackIcon;
	private TextView recPlaybackUser;
	private TextView recPlaybackSeconds;
	private TextView recPlaybackTime;
	private TextView recPlaybackNone;
	private ImageView recPlaybackNew;

	public SessionBoxMessage(Context context, View parentView, SessionBox box)
	{
		super(context);
		// TODO Auto-generated constructor stub
		contextMain = (Activity) context;
		sessionBox = box;
		loadView(parentView);
	}

	private void loadView(View parentView)
	{
//		talkWindow = (MyRelativeLayout) parentView.findViewById(R.id.talk_msg_window);
//		talkWindow.setOnResizeListener(this);

		parentView.findViewById(R.id.menu_btn).setOnClickListener(this);
		ivSend = (ImageView) parentView.findViewById(R.id.send);
		ivSend.setOnClickListener(this);
		etMsg = (EditText) parentView.findViewById(R.id.EditText);
		ivBtnRecord = (ImageView) parentView.findViewById(R.id.talk_btn_record);
		ivBtnRecord.setOnTouchListener(this);
		etMsg.setOnTouchListener(this);
		etMsg.addTextChangedListener(this);
		mvRecording = (MacRecordingView) parentView.findViewById(R.id.mac_talking);
		mvRecording.initChild();
		lLayoutEmotion = (LinearLayout) parentView.findViewById(R.id.emotion);
		animRefresh = AnimationUtils.loadAnimation(contextMain, R.anim.refresh);
		gvSmiley = (GridView) parentView.findViewById(R.id.gridview);
		gvSmiley.setOnItemClickListener(this);
		talkPannel = (LinearLayout) parentView.findViewById(R.id.talk_pannel);
		adapterMessage = new AdapterSessionMessage(contextMain, this, this);
		lvMessage = (PullToRefreshListView) parentView.findViewById(R.id.lv_message);
		lvMessage.setOnRefreshListener(this);
		lvMessage.setOnItemClickListener(this);
		lvMessage.setAdapter(adapterMessage);
		tvMessageNewCount = (TextView) parentView.findViewById(R.id.talk_label_msg);

		parentView.findViewById(R.id.talk_playback).setOnClickListener(this);
		recPlayback = (LinearLayout) parentView.findViewById(R.id.talk_playback_panel);
		recPlaybackIcon = (ImageView) parentView.findViewById(R.id.talk_playback_icon);
		recPlaybackUser = (TextView) parentView.findViewById(R.id.talk_playback_user);
		recPlaybackSeconds = (TextView) parentView.findViewById(R.id.talk_playback_seconds);
		recPlaybackTime = (TextView) parentView.findViewById(R.id.talk_playback_time);
		recPlaybackNone = (TextView) parentView.findViewById(R.id.talk_playback_none);
		recPlaybackNew = (ImageView) parentView.findViewById(R.id.talk_playback_user_unread);
	}

	/*************************
	 * 
	 * API
	 * 
	 *************************/

	public void setSession(AirSession s)
	{
		if ((s != null && session != null && !s.getSessionCode().equals(session.getSessionCode())) || (session == null && s != null))
		{
			lvMessage.setHaveMore(s.isMessageMore());
			mHandler.sendEmptyMessageDelayed(1, 100);
		}
		this.session = s;
		Log.d(SessionBoxMessage.class, "SessionBoxMessage - setSession");
		adapterMessage.setSession(s);

	}

	public void refreshMessages()
	{
		adapterMessage.notifyDataSetChanged();
	}

	public void refreshMessageNewCount(boolean toClean)
	{
		int count = 0;
		if (session != null)
		{
			if (sessionBox.getSessionType() == AirSession.TYPE_CHANNEL)
			{
				AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(session.getSessionCode());
				if (channel != null)
				{
					if (toClean)
					{
						channel.msgUnReadCountClean();
					}
					count = channel.getMsgUnReadCount();
				}
			}
			else if (sessionBox.getSessionType() == AirSession.TYPE_DIALOG)
			{
				if (toClean)
				{
					session.setMessageUnreadCount(0);
				}
				count = session.getMessageUnreadCount();
			}
		}
		if (count == 0)
		{
			tvMessageNewCount.setText("");
			int resid = ThemeUtil.getResourceId(R.attr.theme_label_msg, contextMain);
			tvMessageNewCount.setBackgroundResource(resid);
		}
		else
		{
			tvMessageNewCount.setText(count + "");
			int resid = ThemeUtil.getResourceId(R.attr.theme_label_msg_new, contextMain);
			tvMessageNewCount.setBackgroundResource(resid);
		}
	}

	public void refreshPlayback()
	{
		if (session != null && session.getMessagePlayback() != null)
		{
			AirMessage msg = session.getMessagePlayback();
			if (msg.isRecordPlaying())
			{
				int resid = ThemeUtil.getResourceId(R.attr.theme_msg_audio_stop, contextMain);
				recPlaybackIcon.setImageResource(resid);
			}
			else
			{
				int resid = ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, contextMain);
				recPlaybackIcon.setImageResource(resid);  
			}
			if (TextUtils.equals(msg.getIpocidFrom(), AirtalkeeAccount.getInstance().getUserId()))
				recPlaybackUser.setText(contextMain.getString(R.string.talk_me));
			else
				recPlaybackUser.setText(msg.getInameFrom());
			recPlaybackSeconds.setText(msg.getImageLength() + "''");
			recPlaybackTime.setText(msg.getTime());
			recPlayback.setVisibility(View.VISIBLE);
			recPlaybackNone.setVisibility(View.GONE);
			if (msg.getState() == AirMessage.STATE_NEW)
			{
				recPlaybackNew.setVisibility(View.VISIBLE);
			}
			else
			{
				recPlaybackNew.setVisibility(View.GONE);
			}
		}
		else
		{
			int resid = ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, contextMain);
			recPlaybackIcon.setImageResource(resid);  
			recPlaybackUser.setText("");
			recPlaybackSeconds.setText("");
			recPlaybackTime.setText("");
			recPlayback.setVisibility(View.GONE);
			recPlaybackNone.setVisibility(View.VISIBLE);
			recPlaybackNew.setVisibility(View.GONE);
		}
	}

	/*************************
	 * 
	 * Handler
	 * 
	 *************************/

	private void messageSend()
	{
		if (session != null)
		{
			String msg = etMsg.getText().toString();
			if (msg != null && !msg.trim().equals(""))
			{
				etMsg.setText("");
				adapterMessage.notifyDataSetChanged();
			}
		}
		else
		{
			Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_idle));
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
						Util.Toast(contextMain, contextMain.getString(R.string.talk_msg_no_local_file));
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
							Util.Toast(contextMain, contextMain.getString(R.string.talk_msg_no_local_file));
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

	@SuppressWarnings("deprecation")
	private void showToolsPw()
	{
		Util.hideSoftInput(contextMain);
		lLayoutEmotion.setVisibility(View.GONE);
		if (pwTools == null)
		{
			LayoutInflater mLayoutInflater = (LayoutInflater) contextMain.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View actionView = mLayoutInflater.inflate(R.layout.layout_popup_window_tools, null);
			pwTools = new PopupWindow(actionView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			gvTools = (GridView) actionView.findViewById(R.id.tools_gridview);
			gvTools.setOnItemClickListener(this);
			gvTools.setAdapter(adapterTools);
		}
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		p.height = (int) (contextMain.getResources().getDimension(R.dimen.pop_menu_tools_height));
		p.width = (int) ((contextMain.getResources().getDimension(R.dimen.pop_menu_tools_width) + 5) * adapterTools.getCount());
		gvTools.setLayoutParams(p);

		pwTools.setAnimationStyle(R.style.menudialog);
		pwTools.setOutsideTouchable(true);
		pwTools.setFocusable(true);
		pwTools.setBackgroundDrawable(new BitmapDrawable());
		pwTools.showAtLocation(talkPannel, Gravity.BOTTOM, talkPannel.getLeft() - 200, talkPannel.getBottom());
	}

	/*
	private void showImagePW(final Bitmap bm)
	{
		if (pwImage == null)
		{
			LayoutInflater mLayoutInflater = (LayoutInflater) contextMain.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View actionView = mLayoutInflater.inflate(R.layout.layout_popup_window_image, null);
			pwImage = new PopupWindow(actionView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			ivPic = (ImageView) actionView.findViewById(R.id.image_pic);
			ivPic.setOnClickListener(this);
		}
		ivPic.setImageBitmap(bm);
		pwImage.setAnimationStyle(R.style.PopupAnimation);
		pwImage.setOutsideTouchable(true);
		pwImage.setFocusable(true);
		pwImage.setBackgroundDrawable(new BitmapDrawable());
		pwImage.showAtLocation(contextParentView.findViewById(R.id.bottom_pannel), Gravity.CENTER, 0, 0);
		pwImage.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss()
			{
				// TODO Auto-generated method stub
				if (ivPic != null)
					ivPic.setImageBitmap(null);
				if (bm != null)
					bm.recycle();
			}
		});
	}
	*/

	private void showRemoveAllDialog(final String sessionCode)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(contextMain);
		builder.setMessage(contextMain.getString(R.string.talk_msg_remove_all_confirm));
		builder.setPositiveButton(contextMain.getString(R.string.talk_ok), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				AirtalkeeMessage.getInstance().MessageRemoveAll(sessionCode);
				adapterMessage.notifyDataSetChanged();
			}
		});

		builder.setNegativeButton(contextMain.getString(R.string.talk_no), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.cancel();
			}
		});
		builder.show();
	}

	private void emotionPanelController()
	{
		if (lLayoutEmotion.getVisibility() == View.GONE)
		{
			Util.hideSoftInput(contextMain);
			if (animBotomIn == null)
			{
				animBotomIn = AnimationUtils.loadAnimation(contextMain, R.anim.menu_in);
			}
			lLayoutEmotion.startAnimation(animBotomIn);
			lLayoutEmotion.setVisibility(View.VISIBLE);
		}
		else
		{
			lLayoutEmotion.setVisibility(View.GONE);
		}
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
					AirtalkeeMessage.getInstance().MessageListMoreLoad(session, SessionBoxMessage.this);
					break;
				}
			}

		}
	};

	/*************************
	 * 
	 * Event
	 * 
	 *************************/

	public boolean onKeyEvent(KeyEvent event)
	{
		boolean isHandled = false;
		if (event.getAction() == KeyEvent.ACTION_DOWN)
		{
			if (lLayoutEmotion.getVisibility() == View.VISIBLE)
			{
				lLayoutEmotion.setVisibility(View.GONE);
				isHandled = true;
			}
		}
		return isHandled;
	}

	@Override
	public void onClick(View v)
	{
		if (sessionBox.isMenuShowing())
		{
			return;
		}

		switch (v.getId())
		{
			case R.id.send:
			{
				String msg = etMsg.getText().toString();
				lLayoutEmotion.setVisibility(View.GONE);
				if (!TextUtils.isEmpty(msg))
				{
					Util.hideSoftInput(contextMain);
					ivBtnRecord.setVisibility(View.VISIBLE);
					messageSend();
				}
				break;
			}
			case R.id.menu_btn:
			{
				if (session != null)
				{
					showToolsPw();
				}
				else
				{
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_idle));
				}
				break;
			}
			case R.id.image_pic:
			{
				if (pwImage != null)
					pwImage.dismiss();
				break;
			}
			case R.id.talk_playback:
			{
				if (session != null && session.getMessagePlayback() != null)
				{
					currentMessage = session.getMessagePlayback();
					if (currentMessage.isRecordPlaying())
					{
						AirtalkeeMessage.getInstance().MessageRecordPlayStop();
					}
					else
					{
						AirtalkeeMessage.getInstance().MessageRecordPlayStart(currentMessage);
						if (currentMessage.getState() == AirMessage.STATE_NEW)
						{
							session.setMessageUnreadCount(session.getMessageUnreadCount() - 1);
							refreshMessageNewCount(false);
						}
					}
				}
				break;
			}
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
							if (contextMain != null)
							{
								try
								{
									Intent intent = new Intent(contextMain, ActivityImagePager.class);
									String[] position = new String[] { currentMessage.getImageUri() };
									ArrayList<String> images = adapterMessage.getPicUrls(position);
									Bundle b = new Bundle();
									b.putStringArrayList("images", images);
									b.putInt("position", Integer.parseInt(position[0]));
									intent.putExtras(b);
									contextMain.startActivity(intent);
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
			default:
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (sessionBox.isMenuShowing())
		{
			return false;
		}
		else
		{
			Log.i(MainSessionView.class, "MainSessionPage - onTouchEvent action=" + event.getAction());
			if (v.getId() == R.id.EditText)
			{
				lLayoutEmotion.setVisibility(View.GONE);
				etMsg.setFocusableInTouchMode(true);
				return false;
			}
			else if (v.getId() == R.id.talk_btn_record)
			{
				if (session != null)
				{
					if (event.getAction() == MotionEvent.ACTION_DOWN)
					{
						recordCancel = false;
						startY = event.getY();
						Sound.vibrate(20, contextMain);
						ivBtnRecord.setImageResource( ThemeUtil.getResourceId(R.attr.theme_talk_button_pressed, contextMain) );
						if (session.getType() == AirSession.TYPE_DIALOG)
							AirtalkeeMessage.getInstance().MessageRecordStart(session, true);
						else
							AirtalkeeMessage.getInstance().MessageRecordStart(session.getSessionCode(), true);
					}
					else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
					{
						ivBtnRecord.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_button, contextMain));
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
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_idle));
				}
			}
			return true;
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		// TODO Auto-generated method stub
		if (session != null)
		{
			session.setMessageTextDraft(s.toString());
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
		if (!TextUtils.isEmpty(s))
		{
			ivBtnRecord.setVisibility(View.GONE);
		}
		else
		{
			ivBtnRecord.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (sessionBox.isMenuShowing())
		{
			return;
		}

		switch (parent.getId())
		{
			case R.id.lv_message:
			{
				Util.hideSoftInput(contextMain);
				break;
			}
			case R.id.gridview:
			{
				int cursor = etMsg.getSelectionStart();
				break;
			}
			case R.id.tools_gridview:
			{
				switch (position)
				{
					case AdapterTools.MENU_EMOTION:
						emotionPanelController();
						break;
					case AdapterTools.MENU_IMAGE:
					{
						String status = Environment.getExternalStorageState();
						if (!status.equals(Environment.MEDIA_MOUNTED))
						{
							Util.Toast(contextMain, contextMain.getString(R.string.insert_sd_card));
							return;
						}
						Intent localIntent = new Intent("android.intent.action.GET_CONTENT", null);
						localIntent.setType("image/*");
						contextMain.startActivityForResult(localIntent, REQUEST_CODE_BROWSE_IMAGE);
						break;
					}
					case AdapterTools.MENU_CAREMA:
					{
						Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						currentFilePath = Util.getImageTempFileName();
						i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(currentFilePath)));
						contextMain.startActivityForResult(i, REQUEST_CODE_CREATE_IMAGE);
						break;
					}
				}
				if (pwTools != null)
					pwTools.dismiss();
				break;
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onLongClick(View v)
	{
		// TODO Auto-generated method stub
		if (sessionBox.isMenuShowing())
		{
			return false;
		}
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
					menuArray = contextMain.getResources().getStringArray(!isPic ? R.array.handle_message_txt : R.array.handle_message_txt1);
					int dialogId = R.id.talk_dialog_message_txt;
					if (currentMessage.getIpocidFrom().equals(AccountController.getUserInfo().getIpocId()))
					{
						dialogId = R.id.talk_dialog_message_txt_send_fail;
						menuArray = contextMain.getResources().getStringArray(!isPic ? R.array.handle_message_send_fail : R.array.handle_message_send_fail1);
					}
					contextMain.removeDialog(dialogId);
					contextMain.showDialog(dialogId);
				}
			}
		}
		return false;
	}

	private String currentFilePath = "";
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_CREATE_IMAGE:
				if (resultCode == MainActivity.RESULT_OK)
				{
					byte[] bitmapData = AirServices.iOperator.readByteFile("", currentFilePath, true);
					Bitmap tempBitmap = PicFactory.getNormalMaxImage(bitmapData);
					if (tempBitmap != null)
					{
						byte bphoto[] = null;
						ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
						// Bitmap bim = PicFactory.imageScale(tempBitmap,
						// PicFactory.MSG_IMAGE_MAX);
						tempBitmap.compress(CompressFormat.JPEG, 80, streamOut);
						bphoto = streamOut.toByteArray();
						try
						{
							streamOut.reset();
							streamOut.close();
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						AirtalkeeMessage.getInstance().MessageImageSend(session, bphoto, true);
						tempBitmap.recycle();
					}
				}
				break;
			case REQUEST_CODE_BROWSE_IMAGE:
				if (resultCode == MainActivity.RESULT_OK)
				{
					try
					{
						System.gc();
						Uri originalUri = data.getData();
						String[] proj = { MediaStore.Images.Media.DATA };
						String path= null;
						@SuppressWarnings("deprecation")
						Cursor cursor = contextMain.managedQuery(originalUri, proj, null, null, null);
						if(cursor != null)
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
							showRemoveAllDialog(session.getSessionCode());
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
						Util.textClip(contextMain, currentMessage.getBody());
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
							showRemoveAllDialog(session.getSessionCode());
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
						{
						}
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
									AirtalkeeMessage.getInstance().MessageRecordResend(session, currentMessage.getMessageCode(), currentMessage.getImageUri(),
										currentMessage.getImageLength(), true);
								}
								adapterMessage.notifyDataSetChanged();
							}

						}
						catch (Exception e)
						{
						}
						break;
					}
					case 3:
					{
						Util.textClip(contextMain, currentMessage.getBody());
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
				refreshMessageNewCount(false);
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
			if (sessionBox.tabIndex() == SessionBox.PAGE_MSG)
			{
				toClean = true;
			}
			adapterMessage.notifyDataSetChanged();
			refreshMessageNewCount(toClean);
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
				Sound.vibrate(20, contextMain);
				Sound.playSound(Sound.PLAYER_MSG_SENT, contextMain);
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
					record.setImageResource( ThemeUtil.getResourceId(R.attr.theme_msg_audio_stop, contextMain) );
				}
				currentMessage.setRecordTimer(currentMessage.getImageLength());
				AirMmiTimer.getInstance().TimerRegister(contextMain, this, false, true, 1000, true, null);
			}
			if (session.getMessagePlayback() != null && TextUtils.equals(session.getMessagePlayback().getImageUri(), resId))
			{
				refreshPlayback();
			}
		}
	}

	@Override
	public void onMessageRecordPlayStop(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		Log.i(SessionBoxMessage.class, "onMessageRecordPlayStop");
		AirMmiTimer.getInstance().TimerUnregister(contextMain, this);
		// if (SetRecordPlayState(msgCode, false))
		{
			Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_STOP, false, contextMain);
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
					record.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, contextMain) );
				}
				TextView text = (TextView) view.findViewById(R.id.record_time);
				if (text != null)
				{
					AirMessage msg = adapterMessage.getMessageByCode(msgCode);
					if (msg != null)
						text.setText(msg.getImageLength() + "''");
				}
			}
			if (session.getMessagePlayback() != null && TextUtils.equals(session.getMessagePlayback().getImageUri(), resId))
			{
				refreshPlayback();
			}
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
				recPlaybackSeconds.setText(currentMessage.getRecordTimer() + "''");
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
			if (sessionBox.tabIndex() == SessionBox.PAGE_MSG)
			{
				toClean = true;
			}
			refreshMessageNewCount(toClean);
			refreshPlayback();
			adapterMessage.notifyDataSetChanged();
		}
	}

	@Override
	public void onMessageRecordStart()
	{
		// TODO Auto-generated method stub
		Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_START, false, contextMain);
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
				Util.Toast(contextMain, contextMain.getString(R.string.talk_rec_result_err_small));
				break;
			case AirtalkeeMessage.REC_RESULT_ERROR:
				Util.Toast(contextMain, contextMain.getString(R.string.talk_rec_result_error));
				break;
			case AirtalkeeMessage.REC_RESULT_CANCEL:
				Util.Toast(contextMain, contextMain.getString(R.string.talk_rec_result_cancel_str));
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

}

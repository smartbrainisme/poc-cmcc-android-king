package com.airtalkee.activity;

import java.net.URLEncoder;
import org.apache.http.util.EncodingUtils;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Language;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;

public class MenuNoticeActivity extends ActivityBase implements OnClickListener
{

	private final String NOTICE_LIST_BASE = "textAnnouncementAction_listTextUI.action?userId=<UID>&lang=<LANG>";
	private final String NOTICE_LIST = "textAnnouncementAction_listTextUI.action";
	private final String NOTICE_CONTENT = "textAnnouncementAction_commentTextUI.action";
	private final String NOTICE_CONTENT_PARAM_LANG = "&lang=";
	private final String NOTICE_CONTENT_PARAM_COMMENT = "comment=";

	private MenuNoticeActivity mInstance;
	private RelativeLayout layoutContent;
	private WebView webViewList, webViewContent;
	private ProgressBar webViewProgress;
	private EditText mComment;
	private TextView mCommentSend;

	private boolean isWebListLoaded = false;
	private boolean isWebContentShowing = false;

	private Animation animIn, animOut;

	private String contentUrl = "";
	private String contentComment = "";

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		mInstance = this;
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_notice);
		doInitView();

		String url = "";
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			url = bundle.getString("url");
		}

		Util.closeNotification(Util.NOTIFI_ID_NOTICE);
		AirtalkeeAccount.getInstance().SystemBroadcastNumberClean();
		openWeb(url);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_notice);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_refresh, this) );
		ivRightLay.setOnClickListener(this);

		layoutContent = (RelativeLayout) findViewById(R.id.talk_layout_notice_content_panel);
		webViewProgress = (ProgressBar) findViewById(R.id.talk_layout_notice_progress);

		webViewList = (WebView) findViewById(R.id.talk_layout_notice_list);
		webViewList.getSettings().setJavaScriptEnabled(true);
		webViewList.setBackgroundColor(0x1f1f1f);

		webViewContent = (WebView) findViewById(R.id.talk_layout_notice_content);
		webViewContent.getSettings().setJavaScriptEnabled(true);
		webViewContent.setBackgroundColor(0x1f1f1f);

		mComment = (EditText) findViewById(R.id.talk_layout_notice_comment_text);
		mCommentSend = (TextView) findViewById(R.id.talk_layout_notice_comment_send);
		findViewById(R.id.talk_layout_notice_comment_send).setOnClickListener(this);

		animIn = AnimationUtils.loadAnimation(this, R.anim.push_in_left);
		animIn.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				// TODO Auto-generated method stub
				try
				{
					layoutContent.setVisibility(View.VISIBLE);
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				webViewList.setVisibility(View.GONE);
				webLoadContent();
			}
		});
		animOut = AnimationUtils.loadAnimation(this, R.anim.push_out_right);
		animOut.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				webViewList.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{}

			@SuppressWarnings("deprecation")
			@Override
			public void onAnimationEnd(Animation animation)
			{
				// TODO Auto-generated method stub
				try
				{
					webViewContent.clearView();
					webViewContent.clearHistory();
					webViewContent.clearCache(true);
					webViewContent.stopLoading();
					layoutContent.setVisibility(View.GONE);
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
			}
		});
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		try
		{
			webViewList.clearHistory();
			webViewList.clearCache(true);
			webViewList.stopLoading();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		super.finish();
		webViewList.destroy();
		webViewContent.destroy();
	}

	private void openWeb(String url)
	{
		if (!Utils.isEmpty(url))
		{
			if (url.endsWith(AccountController.getDmWebNoticeUrl()))
			{
				url = AccountController.getDmWebNoticeUrl() + NOTICE_LIST_BASE;
			}

			if (url.contains(NOTICE_LIST))
			{
				url = url.replace("<UID>", AirtalkeeAccount.getInstance().getUserId());
				url = url.replace("<LANG>", Language.getLocalLanguageZH(this));
				if (!isWebListLoaded)
					webLoadList(url);
				if (isWebContentShowing)
				{
					layoutContent.startAnimation(animOut);
				}
				isWebContentShowing = false;
			}
			else if (url.contains(NOTICE_CONTENT))
			{
				if (!url.contains(NOTICE_CONTENT_PARAM_LANG))
				{
					url += NOTICE_CONTENT_PARAM_LANG + Language.getLocalLanguageZH(this);
				}
				contentUrl = url;
				if (!isWebContentShowing)
				{
					layoutContent.startAnimation(animIn);
				}
				else
				{
					webLoadContent();
				}
				isWebContentShowing = true;
			}
		}
	}

	private void closeWeb()
	{
		if (isWebContentShowing)
		{
			layoutContent.startAnimation(animOut);
			webViewProgress.setVisibility(View.GONE);
			openWeb(AccountController.getDmWebNoticeUrl());
			isWebContentShowing = false;
		}
		else
		{
			finish();
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				closeWeb();
				break;
			case R.id.talk_menu_right_button:
			case R.id.bottom_right_icon:
				if (isWebContentShowing)
				{
					if (!Utils.isEmpty(contentUrl))
					{
						contentComment = "";
						openWeb(contentUrl);
					}
				}
				else
				{
					webViewList.reload();
				}
				break;

			case R.id.talk_layout_notice_comment_send:
			{
				String url = webViewContent.getUrl();
				if (!Utils.isEmpty(mComment.getText().toString()) && !Utils.isEmpty(url))
				{
					try
					{
						contentComment = NOTICE_CONTENT_PARAM_COMMENT + URLEncoder.encode(mComment.getText().toString(), "UTF-8");
						mCommentSend.setEnabled(false);
						openWeb(url);
						Util.hideSoftInput(this);
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_DOWN)
		{
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			{
				closeWeb();
				return true;
			}
			else if (event.getKeyCode() == KeyEvent.KEYCODE_HOME)
			{
				finish();
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private void webLoadList(String url)
	{
		if (!Utils.isEmpty(url))
		{
			webViewList.setWebChromeClient(new WebChromeClient()
			{
				@Override
				public void onProgressChanged(WebView view, int progress)
				{
					webViewProgress.setProgress(progress * 1000);
				}

			});

			webViewList.setWebViewClient(new WebViewClient()
			{

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{
					// TODO Auto-generated method stub
					boolean isHandled = false;
					if (isWebContentShowing)
					{
						isHandled = true;
					}
					else
					{
						if (url != null && url.contains(NOTICE_CONTENT))
						{
							openWeb(url);
							isHandled = true;
						}
					}
					return isHandled;
				}

				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
				{
					Util.Toast(mInstance, description);
				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon)
				{
					// TODO Auto-generated method stub
					super.onPageStarted(view, url, favicon);
					webViewProgress.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url)
				{
					// TODO Auto-generated method stub
					super.onPageFinished(view, url);
					webViewProgress.setVisibility(View.GONE);
					isWebListLoaded = true;
				}
			});

			webViewList.loadUrl(url);
		}
	}

	@SuppressWarnings("deprecation")
	private void webLoadContent()
	{
		if (!Utils.isEmpty(contentUrl))
		{
			webViewContent.setWebChromeClient(new WebChromeClient()
			{
				@Override
				public void onProgressChanged(WebView view, int progress)
				{
					webViewProgress.setProgress(progress * 1000);
				}

			});

			webViewContent.setWebViewClient(new WebViewClient()
			{

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{
					// TODO Auto-generated method stub
					Log.i(MenuNoticeActivity.class, "webViewContent url=" + url);
					return super.shouldOverrideUrlLoading(view, url);
				}

				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
				{
					Util.Toast(mInstance, description);
					contentComment = "";
					mCommentSend.setEnabled(true);
				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon)
				{
					// TODO Auto-generated method stub
					super.onPageStarted(view, url, favicon);
					webViewProgress.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url)
				{
					// TODO Auto-generated method stub
					super.onPageFinished(view, url);
					webViewProgress.setVisibility(View.GONE);
					contentComment = "";
					mComment.setText("");
					mCommentSend.setEnabled(true);
				}
			});

			if (!Utils.isEmpty(contentComment))
			{
				webViewContent.postUrl(contentUrl, EncodingUtils.getBytes(contentComment, "BASE64"));
			}
			else
			{
				webViewContent.clearView();
				webViewContent.loadUrl(contentUrl);
			}
		}
	}
}

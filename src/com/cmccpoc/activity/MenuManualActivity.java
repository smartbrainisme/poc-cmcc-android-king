package com.cmccpoc.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cmccpoc.R;
import com.cmccpoc.Util.Language;
import com.cmccpoc.Util.ThemeUtil;
import com.cmccpoc.Util.Util;
import com.cmccpoc.config.Config;

@SuppressLint("SetJavaScriptEnabled")
public class MenuManualActivity extends ActivityBase implements OnClickListener
{

	// private static final String MANUAL_URL =
	// "http://www.haoliantech.com/atk/client_manual.php?market=%s&vplatform=%s&vtype=%s&vcode=%s&lang=%s";
	private static final String MANUAL_URL = "file:///android_asset/manual/page_help/help.html";
	private static String manual_url = MANUAL_URL;

	private MenuManualActivity mInstance;
	private WebView webView;
	private ProgressBar webViewProgress;
	private boolean isShowAll = false;
	private ImageView ivRight;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		mInstance = this;
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_manual);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		webLoad();
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_manual);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(R.drawable.btn_muanul_show);
		ivRight.setVisibility(View.VISIBLE);
		ivRight.setOnClickListener(this);
		ivRightLay.setVisibility(View.VISIBLE);

		webView = (WebView) findViewById(R.id.talk_layout_webbrowser);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setBackgroundColor(0x1f1f1f);
		webViewProgress = (ProgressBar) findViewById(R.id.talk_layout_webbrowser_progress);
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		webView.stopLoading();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.bottom_right_icon:
			{
				loadJS();
				break;
			}
		}
	}
	
	private void loadJS()
	{
		if (!isShowAll)
		{
			webView.loadUrl("javascript:showAll()");
			isShowAll = true;
			ivRight.setImageResource(R.drawable.btn_muanul_close);
		}
		else
		{
			webView.loadUrl("javascript:closeAll()");
			isShowAll = false;
			ivRight.setImageResource(R.drawable.btn_muanul_show);
		}
	}

	private void webLoad()
	{
		String lang = Language.getLocalLanguageZH(this);
		String url = String.format(manual_url, Config.marketCode, Config.VERSION_PLATFORM, Config.VERSION_TYPE, Config.VERSION_CODE, lang);
		webView.setWebChromeClient(new WebChromeClient()
		{
			@Override
			public void onProgressChanged(WebView view, int progress)
			{
				webViewProgress.setProgress(progress * 1000);
			}
		});

		webView.setWebViewClient(new WebViewClient()
		{
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
			}
		});

		webView.loadUrl(url);
	}

}

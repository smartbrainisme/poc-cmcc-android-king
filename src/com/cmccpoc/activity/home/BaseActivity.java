package com.cmccpoc.activity.home;

import java.util.LinkedHashMap;
import java.util.List;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.SessionAndChannelView;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.listener.OnMmiAccountListener;
import com.cmccpoc.listener.OnMmiChannelListener;
import com.cmccpoc.widget.PageIndicator;

/**
 * Activity的基类，在这定义了一些基本的方法和通用的变量
 * @author Yao
 */
public class BaseActivity extends FragmentActivity implements OnMmiAccountListener, OnMmiChannelListener
{
	public static final int PAGE_MEMBER = 0;
	public static final int PAGE_PTT = 1;
	public static final int PAGE_IM = 2;
	protected final FragmentManager fm = getSupportFragmentManager();

	protected static final Class<?>[] TABS = {
	/* 0 */MemberFragment.class,
	/* 1 */PTTFragment.class,
	/* 2 */IMFragment.class, };

	protected ViewPager viewPager;
	protected PageIndicator mPageIndicator;

	private static BaseActivity mInstance;

	/**
	 * 获取Activity实例
	 * @return
	 */
	public static BaseActivity getInstance()
	{
		return mInstance;
	}

	public int pageIndex = PAGE_PTT;
	protected int actionType;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		AirAccountManager.getInstance().setAccountListener(this);
		mInstance = this;
	}

	/**
	 * 是否需要隐藏软键盘
	 * @param v 
	 * @param event
	 * @return
	 */
	public boolean isShouldHideInput(View v, MotionEvent event)
	{
		if (v != null)
		{
			int[] leftTop = { 0, 0 };
			v.getLocationInWindow(leftTop);
			int left = leftTop[0];
			int top = leftTop[1];
			int bottom = top + v.getHeight();
			int right = left + v.getWidth();
			if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		AirAccountManager.getInstance().setAccountListener(null);
	}

	@Override
	public void onMmiHeartbeatLogin(int result)
	{
		if (result != 0)
		{
			if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE)
			{
				new AlertDialog(this, getString(R.string.talk_account_other), getString(R.string.talk_exit), new DialogListener()
				{
					@Override
					public void onClickOk(int id, boolean isChecked)
					{
						// TODO Auto-generated method stub
					}

					@Override
					public void onClickOk(int id, Object obj)
					{
						System.exit(0);
					}

					@Override
					public void onClickCancel(int id)
					{
						System.exit(0);
					}
				}, false).show();
			}
			else
			{

			}
		}
	}

	@Override
	public void onMmiHeartbeatLogout()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMmiHeartbeatException(int result)
	{
		if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE)
		{
			new AlertDialog(this, getString(R.string.talk_account_other), getString(R.string.talk_exit), new DialogListener()
			{
				@Override
				public void onClickOk(int id, boolean isChecked)
				{
					// TODO Auto-generated method stub
				}

				@Override
				public void onClickOk(int id, Object obj)
				{
					System.exit(0);
				}

				@Override
				public void onClickCancel(int id)
				{
					System.exit(0);
				}
			}, false).show();
		}
	}

	@Override
	public void onChannelListGet(boolean isOk, List<AirChannel> channels)
	{
		// TODO Auto-generated method stub
		SessionAndChannelView.getInstance().refreshChannelAndDialog();
	}

	@Override
	public void onChannelMemberListGet(String channelId, List<AirContact> members)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelOnlineCount(LinkedHashMap<String, Integer> online)
	{

	}

	@Override
	public void onChannelPersonalCreateNotify(AirChannel ch)
	{
		MemberFragment.getInstance().refreshMembers();
		MemberFragment.getInstance().refreshAllMembers();
	}

	@Override
	public void onChannelPersonalDeleteNotify(AirChannel ch)
	{
		// TODO Auto-generated method stub
		MemberFragment.getInstance().refreshMembers();
		MemberFragment.getInstance().refreshAllMembers();
	}

}

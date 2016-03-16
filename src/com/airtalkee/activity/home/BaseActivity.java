package com.airtalkee.activity.home;

import java.util.LinkedHashMap;
import java.util.List;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import com.airtalkee.R;
import com.airtalkee.activity.home.widget.AlertDialog;
import com.airtalkee.activity.home.widget.SessionAndChannelView;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.listener.OnMmiAccountListener;
import com.airtalkee.listener.OnMmiChannelListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.widget.PageIndicator;

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

	//	protected static final Class<?>[] TABS_ADD = {
	//	/* 0 */MemberTempFragment.class,
	//	/* 1 */PTTFragment.class,
	//	/* 2 */IMFragment.class, };

	protected ViewPager viewPager;
	protected PageIndicator mPageIndicator;
	
	private static BaseActivity mInstance;
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

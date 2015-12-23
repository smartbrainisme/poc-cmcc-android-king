package com.airtalkee.activity.home;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.airtalkee.R;
import com.airtalkee.activity.home.widget.AlertDialog;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.listener.OnMmiAccountListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.widget.PageIndicator;

public class BaseActivity extends FragmentActivity implements
		OnMmiAccountListener
{

	public static final int PAGE_MEMBER = 0;
	public static final int PAGE_PTT = 1;
	public static final int PAGE_IM = 2;

	protected static final Class<?>[] TABS = {
	/* 0 */MemberFragment.class,
	/* 1 */PTTFragment.class,
	/* 2 */IMFragment.class, };

	protected static final Class<?>[] TABS_ADD = {
	/* 0 */MemberTempFragment.class,
	/* 1 */PTTFragment.class,
	/* 2 */IMFragment.class, };

	protected ViewPager viewPager;
	protected PageIndicator mPageIndicator;

	protected int pageIndex = PAGE_PTT;
	protected int actionType;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		AirAccountManager.getInstance().setAccountListener(this);
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

	
}

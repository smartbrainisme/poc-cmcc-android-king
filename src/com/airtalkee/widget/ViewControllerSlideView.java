package com.airtalkee.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.airtalkee.widget.ViewControllerCoverView.OnLKCoverViewListener;

public class ViewControllerSlideView extends FrameLayout implements OnLKCoverViewListener, Runnable
{

	private static final String TAG = "LKSlideView";
	private Context mContext;
	private ViewControllerSlideScrollListener listener = null;

	public interface ViewControllerSlideScrollListener
	{
		public void onViewControllerSlideScrollFinished(boolean isMenuShowing);
	}

	@SuppressWarnings("deprecation")
	public ViewControllerSlideView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.setLayoutParams(layoutParams);
//		this.setForeground(getResources().getDrawable(R.drawable.main_foreground));
		leftMenu = new FrameLayout(context);
		LayoutParams leftlp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		leftMenu.setLayoutParams(leftlp);
		leftMenu.setVisibility(View.INVISIBLE);
//		leftMenu.setBackgroundColor(0xff1f1f1f);
		addView(leftMenu);

		rightMenu = new FrameLayout(context);
		LayoutParams rightlp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		rightMenu.setLayoutParams(rightlp);
		rightMenu.setVisibility(View.INVISIBLE);
		addView(rightMenu);
//		rightMenu.setBackgroundColor(Color.BLACK);

		mainView = new ViewControllerCoverView(mContext);
		/**
		 * 当界面运行时 执行事件 要不然 getWidth() 为0
		 */
		this.post(this);
	}

	public void run()
	{
		// TODO Auto-generated method stub
		mainView.initLKCoverView(getWidth());
		mainView.setOnCoverViewListener(this);
		addView(mainView);
	}

	private FrameLayout leftMenu;
	private FrameLayout rightMenu;

	public FrameLayout getLeftMenu()
	{
		return leftMenu;
	}

	public ViewControllerCoverView mainView;

	public FrameLayout getMainView()
	{
		return mainView.getContentView();
	}

	public FrameLayout getRightMenu()
	{
		return rightMenu;
	}

	/**
	 * 把主界面 移动到右边 然后 执行事件 再返回 .用于切换界面
	 */
	public void startViewSwitcher(int direction, Runnable runnable)
	{
		switch (direction)
		{
			case Direction_PushLeft:
				mainView.executeOnScroll = true;
				mainView.smoothScrollTo(0, 0);
				break;
			case Direction_PushRight:
				mainView.executeOnScroll = true;
				mainView.smoothScrollTo(mainView.getPageWidth() * 2, 0);
				break;
		}
		this.switcheEvent = runnable;
	}

	private Runnable switcheEvent;
	public static final int Direction_PushLeft = 1;
	public static final int Direction_PushRight = 2;

	public void refreshShow()
	{
		mainView.adjustView();
	}

	public void setListener(ViewControllerSlideScrollListener listener)
	{
		this.listener = listener;
	}

	public boolean isShowMenu()
	{
		return mainView.isShowMenu();
	}

	public boolean isShowMenuLeft()
	{
		return mainView.isShowMenuLeft();
	}

	public boolean isShowMenuRight()
	{
		return mainView.isShowMenuRight();
	}

	public void resetShow()
	{
		Log.i("m", "ViewControllerSlideView resetShow");
		mainView.executeOnScroll = true;
		mainView.resetView();
	}
	
	public void transLeftShow()
	{
		Log.i("m", "ViewControllerSlideView transLeftShow");
		mainView.executeOnScroll = true;
		if (mainView.isShowMenu())
		{
			mainView.resetView();
		}
		else
		{
			rightMenu.setVisibility(View.INVISIBLE);
			leftMenu.setVisibility(View.VISIBLE);
			mainView.showLeftPage();
		}
	}
	
	public void transRightShow()
	{
		Log.i("m", "ViewControllerSlideView transRightShow");
		mainView.executeOnScroll = true;
		if (mainView.isShowMenu())
		{
			mainView.resetView();
		}
		else
		{
			rightMenu.setVisibility(View.VISIBLE);
			leftMenu.setVisibility(View.INVISIBLE);
			mainView.showRightPage();
		}
	}

	public void hideMenu()
	{
		leftMenu.setVisibility(View.INVISIBLE);
		rightMenu.setVisibility(View.INVISIBLE);
	}

	public void onScrollFinished(ViewControllerCoverView view)
	{
		// TODO Auto-generated method stub
		Log.i("m", "ViewControllerSlideView onScrollFinished");
		if (!mainView.isShowMenu())
		{
			Log.d(TAG, " Hide Menu");
			hideMenu();
		}
		if (switcheEvent != null)
		{
			this.switcheEvent.run();
			this.mainView.resetView();
			this.switcheEvent = null;
		}
		mainView.executeOnScroll = false;

		if (listener != null)
		{
			listener.onViewControllerSlideScrollFinished(mainView.isShowMenu());
		}
	}

}

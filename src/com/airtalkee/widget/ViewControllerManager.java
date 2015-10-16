package com.airtalkee.widget;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.airtalkee.R;
import com.airtalkee.widget.ViewControllerAnimation.OnAnimationDelegate;

public class ViewControllerManager implements OnAnimationDelegate
{

	HashMap<String, ViewController> controllerMap = new HashMap<String, ViewController>();
	ArrayList<ViewController> controllerList = new ArrayList<ViewController>();

	ViewController currentViewController;
	ViewControllerAnimation pushLeftAnimation;
	ViewControllerAnimation pushRightAnimation;

	public ViewControllerManager(Context context)
	{
		this.mContext = context;
		pushLeftAnimation = new ViewControllerAnimation(context, R.anim.push_in_left, R.anim.push_out_left);
		pushRightAnimation = new ViewControllerAnimation(context, R.anim.push_in_right, R.anim.push_out_right);
	}

	Context mContext;

	/**
	 * 启动一个新界面
	 */
	public void startViewController(ViewController controller, boolean hasAnimation)
	{
		if (controller == null)
		{
			return;
		}
		if (controller.equals(currentViewController))
		{
			return;
		}
		controller.controllerManager = this;
		int index = controllerList.indexOf(controller);
		if (index < 0)
		{
			controllerList.add(controller);
		}
		else
		{
			controllerList.remove(index);
			controllerList.add(controller);
		}

		currentViewController.onStop();

		this.rootView.addView(controller.view);
		if (hasAnimation)
		{
			pushLeftAnimation.startAnimation(controller, currentViewController, this);
		}
		else
		{
			this.rootView.removeView(currentViewController.view);
		}
		controller.onStart();
		currentViewController = controller;
	}

	public void dismissController(boolean hasAnimation)
	{
		int controllerSize = controllerList.size();
		if (controllerSize == 0)
		{
			return;
		}
		ViewController controller;
		if (controllerSize == 1)
		{
			ViewController listroot = controllerList.get(0);
			if (listroot.equals(this.rootController))
			{
				return;
			}
			else
			{
				controller = this.rootController;
				controllerMap.put(controller.getClass().getName(), controller);
				controllerList.add(controller);
			}

		}
		else
		{
			controller = controllerList.get(controllerList.size() - 2);
		}

		this.rootView.addView(controller.view);
		if (hasAnimation)
		{
			pushRightAnimation.startAnimation(controller, currentViewController, this);
		}
		else
		{
			this.rootView.removeView(currentViewController.view);
		}
		removeContoller(currentViewController);
		currentViewController = controller;
	}

	/**
	 * 启动一个新界面 如果当前栈中 已有该类视图 就弹出该视图
	 */
	public void startViewController(Class<? extends ViewController> clazz, boolean hasAnimation)
	{
		String className = clazz.getName();
		ViewController viewController = controllerMap.get(className);
		if (viewController == null)
		{
			viewController = newInstance(clazz);
			controllerMap.put(className, viewController);
		}
		startViewController(viewController, hasAnimation);
	}

	private ViewController newInstance(Class<? extends ViewController> clazz)
	{
		try
		{
			Constructor<? extends ViewController> constructor = clazz.getConstructor(Context.class);
			return constructor.newInstance(this.mContext);
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 启动一个新界面 不管当前栈中有没有改视图
	 */
	public void startNewViewController(Class<ViewController> clazz)
	{
		String className = clazz.getName();
		ViewController viewController = controllerMap.get(className);
		if (viewController == null)
		{
			viewController = newInstance(clazz);
			controllerMap.put(className, viewController);
		}
		else
		{
			viewController = newInstance(clazz);
		}
		startViewController(viewController, true);
	}

	/**
	 * 退出当前ViewController 返回上一个ViewController
	 */
	public void dismissController()
	{
		dismissController(true);
	}

	private void removeContoller(ViewController controller)
	{
		if (controller != null)
		{
			controllerList.remove(controller);
			String className = controller.getClass().getName();
			if (controller.equals(controllerMap.get(className)))
			{
				controllerMap.remove(className);
			}
			controller.onFinished();
		}
	}

	/**
	 * 返回到指定ViewController
	 */
	public void dismissToController(ViewController controller)
	{
		int index = controllerList.indexOf(controller);
		if (index >= 0)
		{
			List<ViewController> array = controllerList.subList(index, controllerList.size() - 1);
			for (int i = 0; i < array.size(); i++)
			{
				ViewController controller2 = array.get(i);
				removeContoller(controller2);
			}
		}
		startViewController(controller, true);
	}

	/**
	 * 退出到根视图
	 */
	public void dismissToHomeController()
	{
		controllerMap.clear();
		controllerList.clear();
		ViewController controller = this.rootController;
		controllerMap.put(controller.getClass().getName(), controller);
		startViewController(controller, true);
	}

	ViewGroup rootView;

	/**
	 * 设置根视图
	 */
	public void setRootView(ViewGroup rootView)
	{
		this.rootView = rootView;
	}

	private ViewController rootController;

	public void setRootController(ViewController viewController)
	{
		controllerMap.put(viewController.getClass().getName(), viewController);
		controllerList.add(viewController);
		this.rootView.addView(viewController.view);
		viewController.onStart();
		currentViewController = viewController;
		this.rootController = viewController;
	}

	// View切换动画结束 移除退出的View
	public void onAnimationEnd(ViewControllerAnimation animation)
	{
		// TODO Auto-generated method stub
		final View outView = animation.outViewController.view;
		this.rootView.post(new Runnable()
		{

			public void run()
			{
				// TODO Auto-generated method stub
				rootView.removeView(outView);
			}
		});

	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return currentViewController.onKeyDown(keyCode, event);
	}
}

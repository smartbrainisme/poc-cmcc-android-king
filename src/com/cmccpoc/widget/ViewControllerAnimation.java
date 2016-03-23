package com.cmccpoc.widget;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class ViewControllerAnimation implements AnimationListener
{

	public Animation inAnimation, outAnimation;
	public int what;
	public boolean isInEnd, isOutEnd;

	public ViewControllerAnimation(Context context, int inAnimID, int outAnimID)
	{
		this.inAnimation = AnimationUtils.loadAnimation(context, inAnimID);
		this.outAnimation = AnimationUtils.loadAnimation(context, outAnimID);
		this.inAnimation.setAnimationListener(this);
		this.outAnimation.setAnimationListener(this);

	}

	OnAnimationDelegate delegate;

	public void startAnimation(ViewController inController, ViewController outController)
	{
		startAnimation(inController, outController, null);
	}

	public void startAnimation(ViewController inController, ViewController outController, OnAnimationDelegate delegate)
	{
		this.inViewController = inController;
		this.outViewController = outController;

		this.isInEnd = false;
		;
		this.isOutEnd = false;
		this.delegate = delegate;

		this.inViewController.view.startAnimation(inAnimation);
		this.outViewController.view.startAnimation(outAnimation);

	}

	public void onAnimationEnd(Animation animation)
	{
		// TODO Auto-generated method stub
		if (animation.equals(inAnimation))
		{
			isInEnd = true;
		}
		if (animation.equals(outAnimation))
		{
			isOutEnd = true;
		}
		if (isInEnd && isOutEnd)
		{
			if (delegate != null)
			{
				delegate.onAnimationEnd(this);
			}
			inViewController = null;
			outViewController = null;
		}
	}

	public void onAnimationRepeat(Animation animation)
	{
	// TODO Auto-generated method stub

	}

	public void onAnimationStart(Animation animation)
	{
	// TODO Auto-generated method stub

	}

	public ViewController inViewController;
	public ViewController outViewController;

	public interface OnAnimationDelegate
	{
		void onAnimationEnd(ViewControllerAnimation animation);
	}
}

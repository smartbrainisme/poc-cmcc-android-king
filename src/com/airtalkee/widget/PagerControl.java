package com.airtalkee.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import com.airtalkee.R;

public class PagerControl extends View
{

	private int DEFAULT_BAR_COLOR = 0xffdcdcdc;
	private int DEFAULT_HIGHLIGHT_COLOR = 0xff4acdff;
	private static final int DEFAULT_FADE_DURATION = 500;
	private int numPages, currentPage, position;
	private Paint barPaint, highlightPaint;
	private int fadeDuration;
	private float ovalRadius;
	private int default_bar_color = -1;
	private Animation fadeOutAnimation;

	public PagerControl(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public PagerControl(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.com_airtalkee_widget_PagerControl);
		int barColor = a.getColor(R.styleable.com_airtalkee_widget_PagerControl_barColor, DEFAULT_BAR_COLOR);
		int highlightColor = a.getColor(R.styleable.com_airtalkee_widget_PagerControl_highlightColor, DEFAULT_HIGHLIGHT_COLOR);
		fadeDuration = a.getInteger(R.styleable.com_airtalkee_widget_PagerControl_fadeDuration, DEFAULT_FADE_DURATION);
		ovalRadius = a.getDimension(R.styleable.com_airtalkee_widget_PagerControl_roundRectRadius, 0f);
		a.recycle();

		barPaint = new Paint();
		setBarPaintColor(barColor);

		highlightPaint = new Paint();
		highlightPaint.setColor(highlightColor);

		fadeOutAnimation = new AlphaAnimation(1f, 0f);
		fadeOutAnimation.setDuration(fadeDuration);
		fadeOutAnimation.setRepeatCount(0);
		fadeOutAnimation.setInterpolator(new LinearInterpolator());
		fadeOutAnimation.setFillEnabled(true);
		fadeOutAnimation.setFillAfter(true);
	}

	/**
	 * 
	 * @return current number of pages
	 */
	public int getNumPages()
	{
		return numPages;
	}

	/**
	 * 
	 * @param numPages
	 *            must be positive number
	 */
	public void setNumPages(int numPages)
	{
		if (numPages <= 0)
		{
			throw new IllegalArgumentException("numPages must be positive");
		}
		this.numPages = numPages;
		invalidate();
	}

	public void setBarPaintColor(int color)
	{
		barPaint.setColor(color);
	}

	public void setHighLightColor(int color)
	{
		highlightPaint.setColor(color);
	}

	/**
	 * 0 to numPages-1
	 * 
	 * @return
	 */
	public int getCurrentPage()
	{
		return currentPage;
	}

	/**
	 * 
	 * @param currentPage
	 *            0 to numPages-1
	 */
	public void setCurrentPage(int currentPage)
	{
		if (currentPage < 0 || currentPage >= numPages)
		{
			throw new IllegalArgumentException("currentPage parameter out of bounds");
		}
		if (this.currentPage != currentPage)
		{
			this.currentPage = currentPage;
			this.position = currentPage * getPageWidth();
			invalidate();
		}
	}

	/**
	 * Equivalent to the width of the view divided by the current number of
	 * pages.
	 * 
	 * @return page width, in pixels
	 */
	public int getBarWidth()
	{
		int width = getWidth();
		if (default_bar_color != -1)
		{
			return default_bar_color;
		}
		return width;
	}

	public void setBarWidth(int width)
	{
		this.default_bar_color = width;
	}

	public int getPageWidth()
	{
		if (numPages == 0)
			return 0;
		return getBarWidth() / numPages;
	}

	/**
	 * 
	 * @param position
	 *            can be -pageWidth to pageWidth*(numPages+1)
	 */
	public void setPosition(int position)
	{
		if (this.position != position)
		{
			this.position = position;
			invalidate();
		}
	}

	/**
	 * 
	 * @param canvas
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		if (numPages > 0)
		{
			canvas.drawRoundRect(new RectF(0, 0, getBarWidth(), getHeight()), ovalRadius, ovalRadius, barPaint);
			canvas.drawRoundRect(new RectF(position, 0, position + (getBarWidth() / numPages), getHeight()), ovalRadius, ovalRadius, highlightPaint);
		}
	}
}
